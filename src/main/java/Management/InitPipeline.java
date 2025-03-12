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
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class InitPipeline {
    // Variablen
    private List<InstanzManager> InstanzManagerList = new ArrayList<>(); // Liste zur Speicherung der Instanzen
    private ArrayBlockingQueue<Object> liveDataQueue;   // Queue für die Live-Daten der Klassifizierung
    private List<Merkmal_Speicher> merkmalSpeicherList = new ArrayList<>(); // Liste zur Speicherung der Merkmalspeicher
    private Zyklen_Speicher zyklenSpeicher; // Zyklenspeicher
    private CreateCSV createCSV; // CSV-Datei-Klasse

    private int anzahlSensoren; // Anzahl der Sensoren
    private boolean createCsvFile; // Soll eine CSV-Datei erstellt werden
    private String csvFileName; // Name der CSV-Datei, in der die Merkmale gespeichert werden

    // Konstruktor
    public InitPipeline(int anzahlSensoren, int maxWertPeakNormalisierung, ArrayBlockingQueue<Object> liveDataQueue, CreateCSV createCSV) {
        this.liveDataQueue = liveDataQueue;
        this.anzahlSensoren = anzahlSensoren;
        this.createCSV = createCSV;

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

                //Starten des Merkmalspeichers
                Merkmal_Speicher merkmalSpeicher = new Merkmal_Speicher();

                synchronized (merkmalSpeicherList) {
                    merkmalSpeicherList.add(merkmalSpeicher); // Speichern für späteren Zugriff
                }

                // Starten der Merkmalsextraktion
                Merkmalsextraktion_Manager merkmalsextraktionManager = new Merkmalsextraktion_Manager(merkmalSpeicher, index);

                // Starten des InstanzManagers
                InstanzManager instanzManager = new InstanzManager(createCSV, normalisierungManager, zyklenmanager, merkmalsextraktionManager, merkmalSpeicherList, anzahlSensoren, liveDataQueue);

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

    // Methode zum Hinzufügen von Rohdaten zur jeweiligen Instanz
    public void addRawData(String input) {
        if (input.contains("|")){
//            System.out.println(input.chars().filter(ch -> ch == '|').count());    //Schauen wie viele Datensensoren vorhanden sind
            for (int i = 0; i < splitString(input).size(); i++) {
                synchronized (InstanzManagerList){
                    InstanzManagerList.get(i).setInputData(splitString(input).get(i));
                    InstanzManagerList.get(i).start();
                }
            }
        } else {
            synchronized (InstanzManagerList) {
                InstanzManagerList.get(0).setInputData(splitString(input).get(0));
                InstanzManagerList.get(0).start();
            }
        }
    }

    // Methode zum Splitten eines Strings und Umwandeln in eine Liste von Listen für mehrere Sensoren
    public static List<List<Double>> splitString(String input) {

        List<List<Double>> arrays = new ArrayList<>();

        if (input == null || input.isEmpty()) {
            return Collections.emptyList(); // Leere Liste zurückgeben, wenn der String null oder leer ist
        }

        for (String s : input.split("\\|")) {
            s = s.trim(); // Korrekt trimmen

            List<Double> array = new ArrayList<>();

            for (String v : s.split("\\.")) {
//                System.out.println("v: " + v);
                try {
                    array.add(Double.parseDouble(v.replace(",", "."))); // Konvertierung in Double
                } catch (NumberFormatException e) {
//                    System.out.println("Ungültiger Wert: " + v); // Fehlerausgabe
                }
            }
//            System.out.println("Array: " + array);

            arrays.add(array); // Hinzufügen des Arrays zur Liste
        }

        return arrays;
    }
}