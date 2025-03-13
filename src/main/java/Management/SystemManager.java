package Management;

import Merkmalsextraktion.Merkmalsextraktion_Manager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;

public class SystemManager {
    // Objekte
    private Merkmalsextraktion_Manager merkmalsextraktionManager; // Merkmalsextraktion-Manager-Klasse
    private List<InstanzManager> instanzManagerList; // Liste zur Speicherung der Instanzen
    private CreateCSV createCSV; // CSV-Datei-Klasse
    private ArrayBlockingQueue<Object> liveDataQueue; // Queue für die Live-Daten der Klassifizierung

    // Variablen
    private int anzahlSensoren; // Anzahl der Sensoren und Instanzen
    private boolean createCsvFile; // Soll eine CSV-Datei erstellt werden

    // Konstruktor
    public SystemManager(InitPipeline initPipeline, Merkmalsextraktion_Manager merkmalsextraktionManager, int anzahlSensoren, CreateCSV createCSV, boolean createCsvFile, ArrayBlockingQueue<Object> liveDataQueue) {
        this.merkmalsextraktionManager = merkmalsextraktionManager;
        this.createCSV = createCSV;
        this.liveDataQueue = liveDataQueue;

        this.anzahlSensoren = anzahlSensoren;
        this.createCsvFile = createCsvFile;

        this.instanzManagerList = initPipeline.getInstanzManagerList();
    }

    // Start-Methode
    public void start() {
        List<List<List>> ergebnisPipeline = null;
        List<List<List>> ersteGueltigeNachricht = null; // Speichert die erste gültige Nachricht

        // Starte die Pipeline für alle Sensoren
        for (int i = 0; i < anzahlSensoren; i++) {
            synchronized (instanzManagerList) {
                List<List<List>> tempResult = instanzManagerList.get(i).startPipeline();
//            System.out.println("Pipeline Ergebnis für Sensor " + i + ": " + tempResult);

                if (tempResult != null) {
                    // Speichere die erste gültige Nachricht, falls sie noch nicht existiert
                    if (ersteGueltigeNachricht == null) {
                        ersteGueltigeNachricht = tempResult;
                    }

                    ergebnisPipeline = tempResult;
//                    System.out.println("Erste gültige Nachricht von Sensor " + i + ": " + tempResult);

                    // Starte die Merkmalsextraktion sofort, sobald eine gültige Nachricht kommt
                    processMerkmalsextraktion(ergebnisPipeline);
                }

                // Falls die letzte Iteration erreicht ist, wird das letzte gültige Ergebnis überprüft
                if (i == anzahlSensoren - 1) {
                    // Wenn die letzte Nachricht der ersten gültigen Nachricht entspricht, wird sie nicht weitergegeben
                    if (ersteGueltigeNachricht != null && ergebnisPipeline != null && ersteGueltigeNachricht.equals(ergebnisPipeline)) {
//                        System.out.println("Letzte Pipeline-Nachricht entspricht der ersten gültigen Nachricht – wird nicht weitergegeben.");
                    } else {
//                        System.out.println("Letzte Pipeline-Iteration erreicht, endgültiges Ergebnis: " + ergebnisPipeline);
                        if (ergebnisPipeline != null) {
                            processMerkmalsextraktion(ergebnisPipeline);
                        }
                    }
                }
            }
        }
    }

    /**
     * Startet die Merkmalsextraktion und verarbeitet das Ergebnis weiter.
     */
    private void processMerkmalsextraktion(List<List<List>> ergebnisPipeline) {
        // Prüfe, ob ergebnisPipeline gültig ist
        if (ergebnisPipeline != null && !ergebnisPipeline.isEmpty()) {
            System.out.println("Ergebnis Pipeline: " + ergebnisPipeline);

            List<List<List<Double>>> ergebnisMerkmalsextraktion = merkmalsextraktionManager.startMerkmalsextraktion(ergebnisPipeline);

            // Prüfe, ob ergebnisMerkmalsextraktion gültig ist, bevor die Klassifikation gestartet wird
            if (ergebnisMerkmalsextraktion != null && !ergebnisMerkmalsextraktion.isEmpty()) {
                // Prüfe, ob eine CSV-Datei erstellt werden soll oder die Klassifikation gestartet werden soll
                if (createCsvFile) {
                    // CSV-Datei erstellen
                    createCSV.createCSVFile(convertResultToString(ergebnisMerkmalsextraktion), anzahlSensoren);
                } else {
                    // Klassifikation starten
                    try {
                        liveDataQueue.put(convertResultToString(ergebnisMerkmalsextraktion));
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }












    // Methode zum Hinzufügen von Rohdaten zur jeweiligen Instanz
    public void addRawData(String input) {
        if (input.contains("|")){
//            System.out.println(input.chars().filter(ch -> ch == '|').count());    //Schauen wie viele Datensensoren vorhanden sind
            for (int i = 0; i < splitString(input).size(); i++) {
                synchronized (instanzManagerList){
                    instanzManagerList.get(i).setInputData(splitString(input).get(i));
//                    System.out.println(i + " : " + instanzManagerList.get(i).rawData);
                }
            }
            start();
        } else {
            synchronized (instanzManagerList) {
                instanzManagerList.get(0).setInputData(splitString(input).get(0));
            }
            start();
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

    // Methode um aus dem ErgebisMerkmalen eine Liste von Strings zu erstellen
    public List<String> convertResultToString(List<List<List<Double>>> ergebnisMerkmalsextraktion) {
        List<String> result = new ArrayList<>();
        for (List<List<Double>> sensor : ergebnisMerkmalsextraktion) {
            List<String> sensorResult = new ArrayList<>();
            for (List<Double> merkmale : sensor) {
                sensorResult.add(merkmale.toString().replace("[", "").replace("]", "").replace(" ", ""));
            }
            result.add(String.join(",", sensorResult));
        }
        return result;
    }


}
