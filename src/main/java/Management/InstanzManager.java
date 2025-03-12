package Management;

import Merkmalsextraktion.Merkmal_Speicher;
import Merkmalsextraktion.Merkmalsextraktion_Manager;
import Normalisierung.Normalisierung_Manager;
import Segmentation.Zyklenmanager;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;

public class InstanzManager {
    // Objekte
    private CreateCSV createCSV ; // CSV-Datei-Klasse
    private Normalisierung_Manager normalisierungManager; // Datenspeicher-Klasse
    private Zyklenmanager zyklenmanager; // Zyklenmanager-Klasse
    private Merkmalsextraktion_Manager merkmalsextraktionManager; // Merkmalsextraktion-Manager-Klasse

    // Variablen
    private List<String> allFeatures = new ArrayList<>(); // Array für alle Merkmale
     ArrayList<Double> rawData = new ArrayList<>();
    private boolean createCsvFile; // Soll eine CSV-Datei erstellt werden
    private int anzahlSensoren; // Anzahl der Sensoren
    private ArrayBlockingQueue<Object> liveDataQueue; // Queue für die Live-Daten der Klassifizierung
    private int startZyklenerkennungIndex = 0;  // Startindex für die Zyklenerkennung

    // Konstruktor
    public InstanzManager(CreateCSV createCSV, Normalisierung_Manager normalisierungManager, Zyklenmanager zyklenmanager, Merkmalsextraktion_Manager merkmalsextraktionManager,
                          List<Merkmal_Speicher> merkmalSpeicherList, Set<Integer> accessedMerkmalsSpeicherInstances,
                          int anzahlSensoren, ArrayBlockingQueue<Object> liveDataQueue, boolean createCsvFile) {
        this.createCSV = createCSV;
        this.normalisierungManager = normalisierungManager;
        this.zyklenmanager = zyklenmanager;
        this.merkmalsextraktionManager = merkmalsextraktionManager;

        this.anzahlSensoren = anzahlSensoren;
        this.liveDataQueue = liveDataQueue;
        this.createCsvFile = createCsvFile;
    }

    // Start-Methode
    public List<List<List>> startPipeline() {
        // Überprüfe, ob rawData gültig ist
        if (rawData == null || rawData.isEmpty()) {
            System.out.println("Fehler: rawData ist null oder leer!");
            return null;
        }

        // Start der Normalisierung
        ArrayList<Double> ergebnisNormalisierung = normalisierungManager.startNormalisierung(rawData);

        // Überprüfe, ob die Normalisierung ein Ergebnis hat
        if (ergebnisNormalisierung == null || ergebnisNormalisierung.isEmpty()) {
//            System.out.println("Fehler: Ergebnis Normalisierung ist leer!");
            return null;
        }
//        System.out.println("Ergebnis Normalisierung: " + ergebnisNormalisierung);

        // Start der Zyklenerkennung
        List<List<List>> ergebnisZyklen = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            List<List<List>> tempErgebnis = zyklenmanager.startSegmentation(startZyklenerkennungIndex, startZyklenerkennungIndex,
                    7.0, 5, 30.0, 120, 120,
                    ergebnisNormalisierung, rawData);

            if (tempErgebnis != null && !tempErgebnis.isEmpty()) {
                ergebnisZyklen.addAll(tempErgebnis);
            } else {
//                System.out.println("Fehler: startSegmentation() hat in Durchlauf " + i + " keine Werte geliefert.");
            }
            startZyklenerkennungIndex++;
        }

        // Falls keine Zyklen erkannt wurden, gebe `null` zurück
        if (ergebnisZyklen.isEmpty()) {
//            System.out.println("Fehler: Keine Zyklen erkannt.");
            return null;
        }
//        System.out.println(ergebnisZyklen);
        return ergebnisZyklen;
    }










    // Methode zum Hinzufügen von Merkmalen aus der jeweiligen Instanz
//    public void setFeaturesFromInstanz(int merkmalsSpeicherID) {
//        // Instanze beim Aufruf hinterlegen
//        if (accessedMerkmalsSpeicherInstances.contains(merkmalsSpeicherID) ||
//                ((accessedMerkmalsSpeicherInstances.size() == anzahlSensoren-1) && !accessedMerkmalsSpeicherInstances.contains(merkmalsSpeicherID))) {
//            // Bisheriger Speicher und Instanzliste löschen
//            accessedMerkmalsSpeicherInstances.clear();
//            allFeatures.clear();
//
//            // Hinzufügen der Merkmale der unterschiedlichen Sensoren/Instanzen zu einem Array
//            for (int i = 0; i < anzahlSensoren; i++) {
//                synchronized (merkmalSpeicherList){
//                    allFeatures.add(merkmalSpeicherList.get(i).getAlleMerkmale());
//                }
//            }
//            // System.out.println("Alle Merkmale: " + allFeatures);
//
//            // Start der Klassifikation oder CSV-Erstellung
//            //startClassification();
//
//        } else {
//            accessedMerkmalsSpeicherInstances.add(merkmalsSpeicherID);
//        }
//    }

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

    // Setter und Getter
    public void setInputData(List<Double> inputDataValue) {
        this.rawData.addAll(inputDataValue);
    }
}
