import Management.SystemManager;
import Merkmalsextraktion.Merkmal_Speicher;
import Merkmalsextraktion.Merkmalsextraktion_Manager;
import RandomForest.ModellManager;
import Management.CreateCSV;
import Management.InitPipeline;
import Segmentation.Zyklen_Speicher;

import java.util.concurrent.ArrayBlockingQueue;

public class Main {
    public static void main(String[] args) {
        int port = 12345; // Port, auf dem der Server lauscht
        int maxWertPeakNormalisierung = 2000;   // maximaler Wert, der vom Arduino/Sensor erreicht werden kann
        int hz = 2000;    // Zahl der Hz in dem die Daten übertragen werden
        boolean createCsvFile = false; // Soll eine CSV-Datei erstellt werden
        String csvFileName = "src/Data/Merkmale_2"; // Name der CSV-Datei, in der die Merkmale gespeichert werden
        boolean useRamdomForest = true; // Soll das Random Forest Modell verwendet werden
        int anzahlSensoren = 2; // Anzahl der Sensoren
        // Varianblen Zyklenerkennung
        double schwelleSteigungPorzent = 7.0; // Schwelle für die Steigung
        int minBeteiligteWerteSteigung = 5; // Mindestanzahl an beteiligten Werten für die Steigung
        int minAmplitudeSteigung = 25; // Mindestamplitude für die Steigung
        int toleranzZwischenZyklen = 120; // Toleranz für die Zwischenzyklen
        int maxWerteOhneZyklus = 120; // Maximale Anzahl an Werten ohne Zyklus
        // Variablen zyklenSpeicher
        int toleranceZwischenStartpunkt = 200; // Toleranz für den Startpunkt
        int maxUnmatchedTries = 150; // Maximale Anzahl an Versuchen, um einen Zyklusmatch zu finden


        // Starten des Random Forest Modells
        // Starten der LiveQue
        ArrayBlockingQueue<Object> liveDataQueue = new ArrayBlockingQueue<>(10);
        // Starten des eigentlichen Programms
        if (!createCsvFile && useRamdomForest){
            ModellManager modellManager = new ModellManager(csvFileName, liveDataQueue);
            Thread modellThread = new Thread(modellManager);
            modellThread.start();
        }else if (createCsvFile && useRamdomForest){
            System.out.println("Bitte entweder CSV-Generieren oder Modell verwenden");
            System.exit(0);
        }



        // Initalisierung
        // Initalisierung der Erstellung von CSV-Dateien
        CreateCSV createCSV = new CreateCSV(csvFileName, createCsvFile);

        //Starten des Merkmalspeichers
        Merkmal_Speicher merkmalSpeicher = new Merkmal_Speicher();

        // Starten der Merkmalsextraktion
        Merkmalsextraktion_Manager merkmalsextraktionManager = new Merkmalsextraktion_Manager(merkmalSpeicher);

        // Starten des Zyklenspeicher
        Zyklen_Speicher zyklenSpeicher = new Zyklen_Speicher(anzahlSensoren, toleranceZwischenStartpunkt, maxUnmatchedTries);

        // Starten der Initalisierung der Pipeline
        InitPipeline initPipeline = new InitPipeline(anzahlSensoren, maxWertPeakNormalisierung, zyklenSpeicher,
                schwelleSteigungPorzent, minBeteiligteWerteSteigung, minAmplitudeSteigung, toleranzZwischenZyklen, maxWerteOhneZyklus);

        // Starten des Systemmanagers
        SystemManager systemManager = new SystemManager(initPipeline, merkmalsextraktionManager, zyklenSpeicher, anzahlSensoren, createCSV,createCsvFile, liveDataQueue);

        // Starten der Übertragung des Clients/Sensors
        ReceiveData receiveData = new ReceiveData(systemManager, port);
        receiveData.receiveDatafromClient();

    }
}