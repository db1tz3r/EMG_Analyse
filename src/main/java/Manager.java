import Merkmalsextraktion.FastFourierTransformation;
import Merkmalsextraktion.Merkmal_Speicher;
import Merkmalsextraktion.Merkmalsextraktion_Manager;
import Merkmalsextraktion.PolynomialeApproximation;
import Normalisierung.PeakNormalisierung;
import Normalisierung.Rms;
import Segmentation.Zyklenerkennung;
import Segmentation.Zyklenzusammenfassung;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

public class Manager {
    // Variablen
    private Merkmal_Speicher merkmalSpeicher;
    List<Datenspeicher> datenspeicherList = new ArrayList<>(); // Liste zur Speicherung der Instanzen

    // Konstruktor
    public Manager(int anzahlSensoren, int maxWertPeakNormalisierung, Merkmal_Speicher merkmalSpeicher) {
        this.merkmalSpeicher = merkmalSpeicher;
        initDatenspeicher(anzahlSensoren, maxWertPeakNormalisierung);
    }

    //Initalisierung der Datenspeicher
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

                // Starten der PolynomialApproximation
                PolynomialeApproximation polynomialeApproximation = new PolynomialeApproximation(merkmalSpeicher);

                // Starten der FFT
                FastFourierTransformation fft = new FastFourierTransformation(merkmalSpeicher);

                // Starten der Merkmalsextraktion
                Merkmalsextraktion_Manager merkmalsextraktionManager = new Merkmalsextraktion_Manager(merkmalSpeicher);

                // Starten der allgemeinen Speicherklasse/Manager
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
            for (int i = 0; i < splitString(input).size(); i++) {
                synchronized (datenspeicherList){
                    datenspeicherList.get(i).setInputData(splitString(input).get(i));
                    datenspeicherList.get(i).start();
                }
            }
        } else {
            synchronized (datenspeicherList){
                datenspeicherList.get(0).setInputData(Double.parseDouble(input));
                datenspeicherList.get(0).start();
            }
        }
    }

    public static List<Double> splitString(String input) {
        if (input == null || input.isEmpty()) {
            return Collections.emptyList(); // Leere Liste zurückgeben, wenn der String null oder leer ist
        }

        return Arrays.stream(input.split("\\|")) // String aufteilen
                .map(Double::parseDouble)  // In Double umwandeln
                .collect(Collectors.toList()); // Als Liste sammeln
    }

}
