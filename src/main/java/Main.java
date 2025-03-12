import RandomForest.ModellManager;
import Management.CreateCSV;
import Management.InitPipeline;

import java.util.concurrent.ArrayBlockingQueue;

public class Main {
    public static void main(String[] args) {
        int port = 12345; // Port, auf dem der Server lauscht
        int maxWertPeakNormalisierung = 2000;   // maximaler Wert, der vom Arduino/Sensor erreicht werden kann
        int hz = 2000;    // Zahl der Hz in dem die Daten übertragen werden
        boolean createCsvFile = true; // Soll eine CSV-Datei erstellt werden
        String csvFileName = "src/Data/Merkmale"; // Name der CSV-Datei, in der die Merkmale gespeichert werden
        boolean useRamdomForest = false; // Soll das Random Forest Modell verwendet werden
        int anzahlSensoren = 3; // Anzahl der Sensoren


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

        // Starten der Initalisierung der Pipeline
        InitPipeline initPipeline = new InitPipeline(anzahlSensoren, maxWertPeakNormalisierung, liveDataQueue, createCSV);

        // Starten der Übertragung des Clients/Sensors
        ReceiveData receiveData = new ReceiveData(initPipeline, port);
        receiveData.receiveDatafromClient();

    }
}