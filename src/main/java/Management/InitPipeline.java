package Management;

import Merkmalsextraktion.Merkmal_Speicher;
import Merkmalsextraktion.Merkmalsextraktion_Manager;
import Normalisierung.Normalisierung_Manager;
import Normalisierung.PeakNormalisierung;
import Normalisierung.Rms;
import Segmentation.Zyklen_Speicher;
import Segmentation.Zyklenerkennung;
import Segmentation.Zyklenmanager;
import Segmentation.Zyklenzusammenfassung;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class InitPipeline {
    // Variablen
    private List<InstanzManager> InstanzManagerList = new ArrayList<>(); // Liste zur Speicherung der Instanzen
    private Zyklen_Speicher zyklenSpeicher; // Zyklenspeicher

    // Konstruktor
    public InitPipeline(int anzahlSensoren, int maxWertPeakNormalisierung) {
        // Starten des Zyklen_Speichers
        this.zyklenSpeicher = new Zyklen_Speicher(anzahlSensoren, 100);

        initDatenspeicher(anzahlSensoren, maxWertPeakNormalisierung);
    }

    //Initalisierung der Pipeline
    public void initDatenspeicher(int anzahlSensoren, int maxWertPeakNormalisierung) {
        ExecutorService executor = Executors.newFixedThreadPool(anzahlSensoren); // Anzahl paralleler Initialisierungen

        for (int i = 0; i < anzahlSensoren; i++) {
            int index = i; // Verhindert Lambda-Variable-Probleme
            executor.submit(() -> {
                System.out.println("Starte Pipeline-Initialisierung " + index);

                // Starten der RMS-Klasse
                Rms rms = new Rms();

                // Starten der Peak-Normalisierung-Klasse
                PeakNormalisierung peakNormalisierung = new PeakNormalisierung(maxWertPeakNormalisierung);

                // Starten der allgemeinen Speicherklasse/Sensormanagement.Manager
                Normalisierung_Manager normalisierungManager = new Normalisierung_Manager(/*updatePlotter*/ null, rms, peakNormalisierung);

                // Starten der Zykluserkennung
                Zyklenerkennung zyklenerkennung = new Zyklenerkennung();

                // Starten der Zykluszusammenfassung
                Zyklenzusammenfassung zyklenzusammenfassung = new Zyklenzusammenfassung();

                // Starten des Zyklusmanagers
                Zyklenmanager zyklenmanager = new Zyklenmanager(zyklenerkennung, zyklenzusammenfassung, zyklenSpeicher, index);

                // Starten des InstanzManagers
                InstanzManager instanzManager = new InstanzManager(normalisierungManager, zyklenmanager);

                synchronized (InstanzManagerList) {
                    InstanzManagerList.add(instanzManager); // Speichern für späteren Zugriff
                }

                // Endlosschleife, um den Thread am Leben zu halten
                while (true) {
                    try {
                        Thread.sleep(1000); // Verhindert hohe CPU-Auslastung
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        System.out.println("Thread " + index + " wurde unterbrochen.");
                        break;
                    }
                }

                System.out.println("Pipeline " + index + " geschlossen!");
            });
        }
    }










    // Setter und Getter
    public List<InstanzManager> getInstanzManagerList() {
        return InstanzManagerList;
    }
}