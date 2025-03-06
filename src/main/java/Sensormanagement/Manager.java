package Sensormanagement;

import Merkmalsextraktion.Merkmal_Speicher;
import Merkmalsextraktion.Merkmalsextraktion_Manager;
import Normalisierung.PeakNormalisierung;
import Normalisierung.Rms;
import Segmentation.Zyklenerkennung;
import Segmentation.Zyklenzusammenfassung;

import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

public class Manager {
    // Variablen
    List<Datenspeicher> datenspeicherList = new ArrayList<>(); // Liste zur Speicherung der Instanzen
    private ArrayBlockingQueue<Object> liveDataQueue;   // Queue für die Live-Daten der Klassifizierung
    List<Merkmal_Speicher> merkmalSpeicherList = new ArrayList<>(); // Liste zur Speicherung der Merkmalspeicher
    Set<Integer> accessedMerkmalsSpeicherInstances = new CopyOnWriteArraySet<>(); // Set zur Speicherung der Instanzen
    private List<String> allFeatures = new ArrayList<>(); // Array für alle Merkmale
    CreateCSV createCSV ; // CSV-Datei-Klasse

    private int anzahlSensoren; // Anzahl der Sensoren
    private boolean createCsvFile; // Soll eine CSV-Datei erstellt werden
    private String csvFileName; // Name der CSV-Datei, in der die Merkmale gespeichert werden

    // Konstruktor
    public Manager(int anzahlSensoren, int maxWertPeakNormalisierung, ArrayBlockingQueue<Object> liveDataQueue, boolean createCsvFile, String csvFileName) {
        this.liveDataQueue = liveDataQueue;
        this.anzahlSensoren = anzahlSensoren;
        this.createCsvFile = createCsvFile;
        this.csvFileName = csvFileName;
        initDatenspeicher(anzahlSensoren, maxWertPeakNormalisierung);

        // Initalisierung der Erstellung von CSV-Dateien
        CreateCSV createCSV = new CreateCSV(csvFileName, createCsvFile);
        this.createCSV = createCSV;
    }

    //Initalisierung der Sensormanagement.Datenspeicher
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

                // Starten der Zykluserkennung
                Zyklenerkennung zyklenerkennung = new Zyklenerkennung();

                // Starten der Zykluszusammenfassung
                Zyklenzusammenfassung zyklenzusammenfassung = new Zyklenzusammenfassung();

                //Starten des Merkmalspeichers
                Merkmal_Speicher merkmalSpeicher = new Merkmal_Speicher();

                synchronized (merkmalSpeicherList) {
                    merkmalSpeicherList.add(merkmalSpeicher); // Speichern für späteren Zugriff
                }

                // Starten der Merkmalsextraktion
                Merkmalsextraktion_Manager merkmalsextraktionManager = new Merkmalsextraktion_Manager(merkmalSpeicher, this, index);

                // Starten der allgemeinen Speicherklasse/Sensormanagement.Manager
                Datenspeicher datenspeicher = new Datenspeicher(/*updatePlotter*/ null, rms, peakNormalisierung, zyklenerkennung, merkmalsextraktionManager, zyklenzusammenfassung);

                synchronized (datenspeicherList) {
                    datenspeicherList.add(datenspeicher); // Speichern für späteren Zugriff
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
                synchronized (datenspeicherList){
                    datenspeicherList.get(i).setInputData(splitString(input).get(i));
                    datenspeicherList.get(i).start();
                }
            }
        } else {
            synchronized (datenspeicherList){
                datenspeicherList.get(0).setInputData(splitString(input).getFirst());
                datenspeicherList.get(0).start();
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

    // Methode zum Hinzufügen von Merkmalen aus der jeweiligen Instanz
    public void setFeaturesFromInstanz(int merkmalsSpeicherID) {
        // Instanze beim Aufruf hinterlegen
        if (accessedMerkmalsSpeicherInstances.contains(merkmalsSpeicherID) ||
                ((accessedMerkmalsSpeicherInstances.size() == anzahlSensoren-1) && !accessedMerkmalsSpeicherInstances.contains(merkmalsSpeicherID))) {
            // Bisheriger Speicher und Instanzliste löschen
            accessedMerkmalsSpeicherInstances.clear();
            allFeatures.clear();

            // Hinzufügen der Merkmale der unterschiedlichen Sensoren/Instanzen zu einem Array
            for (int i = 0; i < anzahlSensoren; i++) {
                synchronized (merkmalSpeicherList){
                    allFeatures.add(merkmalSpeicherList.get(i).getAlleMerkmale());
                }
            }
            // System.out.println("Alle Merkmale: " + allFeatures);

            // Start der Klassifikation oder CSV-Erstellung
            startClassification();

        } else {
            accessedMerkmalsSpeicherInstances.add(merkmalsSpeicherID);
        }
    }

    // Start der Klassifizierung oder der CSV-Erstellung
    public void startClassification() {
        // Start der Klassifizierung oder der CSV-Erstellung
        if (createCsvFile) {
            // CSV-Datei erstellen
            createCSV.createCSVFile(allFeatures, anzahlSensoren);
        } else {
            // Klassifikation starten
            try {
                liveDataQueue.put(allFeatures);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

}