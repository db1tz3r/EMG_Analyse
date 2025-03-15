package Management;

import Merkmalsextraktion.Merkmalsextraktion_Manager;
import Segmentation.Zyklen_Speicher;

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
    private Zyklen_Speicher zyklusSpeicher; //Zyklen-Speicher-Klasse

    // Variablen
    private int anzahlSensoren; // Anzahl der Sensoren und Instanzen
    private boolean createCsvFile; // Soll eine CSV-Datei erstellt werden

    // Konstruktor
    public SystemManager(InitPipeline initPipeline, Merkmalsextraktion_Manager merkmalsextraktionManager, Zyklen_Speicher zyklenSpeicher, int anzahlSensoren, CreateCSV createCSV, boolean createCsvFile, ArrayBlockingQueue<Object> liveDataQueue) {
        this.merkmalsextraktionManager = merkmalsextraktionManager;
        this.createCSV = createCSV;
        this.liveDataQueue = liveDataQueue;
        this.zyklusSpeicher = zyklenSpeicher;

        this.anzahlSensoren = anzahlSensoren;
        this.createCsvFile = createCsvFile;

        this.instanzManagerList = initPipeline.getInstanzManagerList();
    }

    // Start-Methode
    public void start() {
        List<List<List>> ergebnisPipeline = null;
        List<List<ArrayList<Double>>> lokaleSpeicherung = new ArrayList<>(Collections.nCopies(anzahlSensoren, null)); // Lokale Speicherung

        // Starte die Pipeline für alle Sensoren
        for (int i = 0; i < anzahlSensoren; i++) {
            synchronized (instanzManagerList) {
                List<ArrayList<Double>> tempResult = instanzManagerList.get(i).startPipeline();

                // **Falls tempResult null ist, ersetze es durch eine leere Liste**
                lokaleSpeicherung.set(i, tempResult != null ? tempResult : new ArrayList<>());

                if (tempResult != null) {
//                    System.out.println("Ergebnis Pipeline " + i + ": " + tempResult);
                } else {
//                    System.out.println("⚠ WARNUNG: tempResult ist null für Sensor " + i);
                }

                // Falls letzte Iteration erreicht ist → `checkIntervallFromAllInstanzes()` durchführen
                if (i == anzahlSensoren - 1) {
//                    System.out.println("✅ Alle Pipeline-Ergebnisse gesammelt. Übergabe an checkIntervallFromAllInstanzes...");

                    List<List<List>> zyklusErgebnis = zyklusSpeicher.checkIntervallFromAllInstanzes(lokaleSpeicherung);

                    // Falls `zyklusErgebnis` nicht leer ist, weiterverarbeiten
                    if (zyklusErgebnis != null && !zyklusErgebnis.isEmpty()) {
                        System.out.println("✅ Erfolgreiche Rückgabe von checkIntervallFromAllInstanzes: " + zyklusErgebnis);
                        ergebnisPipeline = zyklusErgebnis;
                    }
                }
            }
        }

        // Falls `ergebnisPipeline` existiert, Merkmalsextraktion starten
        if (ergebnisPipeline != null && !ergebnisPipeline.isEmpty()) {
            processMerkmalsextraktion(ergebnisPipeline);
        } else {
//            System.out.println("Kein gültiges ergebnisPipeline für Merkmalsextraktion vorhanden.");
        }
    }










    //Startet die Merkmalsextraktion und verarbeitet das Ergebnis weiter.
    private void processMerkmalsextraktion(List<List<List>> ergebnisPipeline) {
        // Prüfe, ob ergebnisPipeline gültig ist
//        System.out.println("Ergebnis Pipeline: " + ergebnisPipeline);
        if (ergebnisPipeline != null && !ergebnisPipeline.isEmpty()) {
//            System.out.println("Ergebnis Pipeline: " + ergebnisPipeline);

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
//                    System.out.println("Instanz: " + i + " Input: " + splitString(input).get(i));
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
    public static List<Double> splitString(String input) {
        List<Double> result = new ArrayList<>(); // Speichert alle Zahlen in einer einzigen Liste

        if (input == null || input.isEmpty()) {
            return Collections.emptyList(); // Falls der Input leer oder null ist, gib eine leere Liste zurück
        }

        for (String s : input.split("\\|")) {
            s = s.trim(); // Entferne überflüssige Leerzeichen

            for (String v : s.split("\\.")) {
                try {
                    result.add(Double.parseDouble(v.replace(",", "."))); // Konvertiere zu Double und speichere
                } catch (NumberFormatException e) {
                    System.out.println("Ungültiger Wert ignoriert: " + v); // Fehlerausgabe für ungültige Werte
                }
            }
        }

        return result; // Gib die gesammelten Zahlen als Liste zurück
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
