package Management;

import Merkmalsextraktion.Merkmalsextraktion_Manager;
import Segmentation.Zyklen_Speicher;

import java.util.ArrayList;
import java.util.Collection;
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
        List<List<ArrayList<Double>>> lokaleSpeicherung = new ArrayList<>(Collections.nCopies(anzahlSensoren, null)); // Lokale Speicherung

        // Starte die Pipeline für alle Sensoren
        for (int i = 0; i < anzahlSensoren; i++) {
            synchronized (instanzManagerList) {
                List<ArrayList<Double>> tempResult = instanzManagerList.get(i).startPipeline();

                // **Falls tempResult null ist, ersetze es durch eine leere Liste**
                lokaleSpeicherung.set(i, tempResult != null ? tempResult : new ArrayList<>());

                // Falls letzte Iteration erreicht ist → `checkIntervallFromAllInstanzes()` durchführen
                if (i == anzahlSensoren - 1) {
                    zyklusSpeicher.addLokaleDaten(lokaleSpeicherung);
                    List<List<List<List<Double>>>> zyklusErgebnis = zyklusSpeicher.startMatching();

                    if (zyklusErgebnis != null && !zyklusErgebnis.isEmpty()) {
//                        System.out.println("ZyklusErgebnis: " + zyklusErgebnis);

                        System.out.println("ZyklusErgebnis Größe: " + zyklusErgebnis.size());
//                        System.out.println("ZyklusErgebnis Instanz Größe: " + zyklusErgebnis.get(0).size());
                        for (List<List<List<Double>>> zyklusErgebnisInstanz : zyklusErgebnis) {
                            // Falls `ergebnisPipeline` existiert, Merkmalsextraktion starten
//                            System.out.println("ZyklusErgebnisInstanz: " + zyklusErgebnisInstanz);
                            if (!zyklusErgebnisInstanz.isEmpty() && zyklusErgebnisInstanz != null) {
                                processMerkmalsextraktion(zyklusErgebnisInstanz);
                            }
                        }
                    }
                }
            }
        }
    }

    //Startet die Merkmalsextraktion und verarbeitet das Ergebnis weiter.
    private void processMerkmalsextraktion(List<List<List<Double>>> ergebnisPipeline) {
        // Prüfe, ob ergebnisPipeline gültig ist
        if (ergebnisPipeline != null && !ergebnisPipeline.isEmpty()) {

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
            for (int i = 0; i < splitString(input).size(); i++) {
                synchronized (instanzManagerList){
                    instanzManagerList.get(i).setInputData(splitString(input).get(i));
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
