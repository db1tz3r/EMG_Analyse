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
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class InitPipeline {
    // Variablen
    private List<InstanzManager> InstanzManagerList = new ArrayList<>(); // Liste zur Speicherung der Instanzen
    private Zyklen_Speicher zyklenSpeicher; // Zyklenspeicher

    // Konstruktor
    public InitPipeline(int anzahlSensoren, int maxWertPeakNormalisierung, Zyklen_Speicher zyklenSpeicher,
                        double schwelleSteigungPorzent, int minBeteiligteWerteSteigung, double minAmplitudeSteigung, int toleranzZwischenZyklen,int  maxWerteOhneZyklus) {
        // Starten des Zyklen_Speichers
        this.zyklenSpeicher = zyklenSpeicher;

        initDatenspeicher(anzahlSensoren, maxWertPeakNormalisierung, schwelleSteigungPorzent, minBeteiligteWerteSteigung, minAmplitudeSteigung, toleranzZwischenZyklen, maxWerteOhneZyklus);
    }

    //Initalisierung der Pipeline
    public void initDatenspeicher(int anzahlSensoren, int maxWertPeakNormalisierung,
                                  double schwelleSteigungPorzent, int minBeteiligteWerteSteigung,
                                  double minAmplitudeSteigung, int toleranzZwischenZyklen, int maxWerteOhneZyklus) {
        ExecutorService executor = Executors.newSingleThreadExecutor(); // Single Thread für synchrone Initialisierung

        // Definierte Reihenfolge: 0 → 1 → 2

        List<Callable<Void>> tasks = new ArrayList<>();
        for (int i = 0; i < anzahlSensoren; i++) {
            int index = i; // Lokale Variable für den Lambda-Ausdruck
            tasks.add(() -> {
//                System.out.println("Starte Pipeline-Initialisierung " + index);

                Rms rms = new Rms();
                PeakNormalisierung peakNormalisierung = new PeakNormalisierung(maxWertPeakNormalisierung);
                Normalisierung_Manager normalisierungManager = new Normalisierung_Manager(rms, peakNormalisierung);
                Zyklenerkennung zyklenerkennung = new Zyklenerkennung();
                Zyklenzusammenfassung zyklenzusammenfassung = new Zyklenzusammenfassung();
                Zyklenmanager zyklenmanager = new Zyklenmanager(zyklenerkennung, zyklenzusammenfassung, zyklenSpeicher, index);

                InstanzManager instanzManager = new InstanzManager(normalisierungManager, zyklenmanager,
                        schwelleSteigungPorzent, minBeteiligteWerteSteigung, minAmplitudeSteigung, toleranzZwischenZyklen, maxWerteOhneZyklus);

                synchronized (InstanzManagerList) {
                    InstanzManagerList.add(instanzManager);
                }

                System.out.println("Pipeline " + index + " gestartet!");
                return null;
            });
        }

        try {
            executor.invokeAll(tasks); // Wartet darauf, dass alle Tasks der Reihe nach abgeschlossen werden
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            executor.shutdown();
        }
    }

    // Setter und Getter
    public List<InstanzManager> getInstanzManagerList() {
        return InstanzManagerList;
    }
}