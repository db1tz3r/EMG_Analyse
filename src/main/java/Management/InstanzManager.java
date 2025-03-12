package Management;

import Merkmalsextraktion.Merkmal_Speicher;
import Merkmalsextraktion.Merkmalsextraktion_Manager;
import Normalisierung.Normalisierung_Manager;
import Segmentation.Zyklenmanager;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.ArrayBlockingQueue;

public class InstanzManager {
    // Objekte
    private CreateCSV createCSV ; // CSV-Datei-Klasse
    private Normalisierung_Manager normalisierungManager; // Datenspeicher-Klasse
    private Zyklenmanager zyklenmanager; // Zyklenmanager-Klasse
    private Merkmalsextraktion_Manager merkmalsextraktionManager; // Merkmalsextraktion-Manager-Klasse
    private List<Merkmal_Speicher> merkmalSpeicherList; // Liste zur Speicherung der Merkmalspeicher

    // Variablen
    private Set<Integer> accessedMerkmalsSpeicherInstances = new CopyOnWriteArraySet<>(); // Set zur Speicherung der Instanzen
    private List<String> allFeatures = new ArrayList<>(); // Array für alle Merkmale
    private ArrayList<Double> rawData = new ArrayList<>();
    private boolean createCsvFile; // Soll eine CSV-Datei erstellt werden
    private String csvFileName; // Name der CSV-Datei, in der die Merkmale gespeichert werden
    private int anzahlSensoren; // Anzahl der Sensoren
    private ArrayBlockingQueue<Object> liveDataQueue; // Queue für die Live-Daten der Klassifizierung
    private int startZyklenerkennungIndex = 0;  // Startindex für die Zyklenerkennung

    // Konstruktor
    public InstanzManager(CreateCSV createCSV, Normalisierung_Manager normalisierungManager, Zyklenmanager zyklenmanager, Merkmalsextraktion_Manager merkmalsextraktionManager,
                          List<Merkmal_Speicher> merkmalSpeicherList,
                          int anzahlSensoren, ArrayBlockingQueue<Object> liveDataQueue) {
        this.createCSV = createCSV;
        this.normalisierungManager = normalisierungManager;
        this.zyklenmanager = zyklenmanager;
        this.merkmalsextraktionManager = merkmalsextraktionManager;
        this.merkmalSpeicherList = merkmalSpeicherList;

        this.anzahlSensoren = anzahlSensoren;
        this.liveDataQueue = liveDataQueue;
    }

    // Start-Methode
    public void start() {
        //Start der Normalisierung
        ArrayList<Double> ergebnisNormalisierung = normalisierungManager.startNormalisierung(rawData);

        //Start der Zyklenerkennung
        if (!ergebnisNormalisierung.isEmpty()){
//            System.out.println("Ergebnis Normalisierung: " + ergebnisNormalisierung);
            for (int i = 0; i < 5; i++) {
                List<List<List>> ergebnisZyklen =  zyklenmanager.startSegmentation(startZyklenerkennungIndex, startZyklenerkennungIndex,
                        7.0, 5, 30.0, 120, 120,
                        ergebnisNormalisierung, rawData);
//                System.out.println("Ergebnis Zyklenerkennung: " + ergebnisZyklen);
                startZyklenerkennungIndex++;

                //Start der Merkmalsextraktion
                if (ergebnisZyklen != null) {
//                    System.out.println("Ergebnis Zyklenerkennung: " + ergebnisZyklen);
                    //merkmalsextraktionManager.setArraysZyklenerkennung(zyklusArrayWertErgebnis, zyklusArrayZeitErgebnis, peakNormalisierungArrayErgebnis, rawData);
                    //merkmalsextraktionManager.run();
                }
            }
        }
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

    // Setter und Getter
    public void setInputData(List<Double> inputDataValue) {
        this.rawData.addAll(inputDataValue);
    }
}
