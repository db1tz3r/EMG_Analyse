import Merkmalsextraktion.Merkmal_Speicher;
import Merkmalsextraktion.Merkmalsextraktion_Manager;
import Merkmalsextraktion.PolynomialeApproximation;
import Merkmalsextraktion.FastFourierTransformation;
import Normalisierung.PeakNormalisierung;
import Normalisierung.Rms;
import RandomForest.ModellManager;
import Segmentation.Zyklenerkennung;
import Segmentation.Zyklenzusammenfassung;
import UI.RealTimePlotter;
import UI.UpdatePlotter;

import javax.swing.*;
import java.util.concurrent.ArrayBlockingQueue;

public class Main {
    public static void main(String[] args) {
        int port = 12345; // Port, auf dem der Server lauscht
        int maxWertPeakNormalisierung = 2000;   // maximaler Wert, der vom Arduino/Sensor erreicht werden kann
        int hz = 2000;    // Zahl der Hz in dem die Daten übertragen werden
        boolean createCsvFile = true; // Soll eine CSV-Datei erstellt werden
        String csvFileName = "src/Data/Merkmale"; // Name der CSV-Datei, in der die Merkmale gespeichert werden
        boolean useRamdomForest = false;


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




        //Starten des Merkmalsspeichers
        Merkmal_Speicher merkmalSpeicher = new Merkmal_Speicher(csvFileName ,createCsvFile, liveDataQueue);

        // Starten des Managers
        Manager manager = new Manager(3, maxWertPeakNormalisierung, merkmalSpeicher);

        // Starten der Übertragung des Clients/Sensors
        ReceiveData receiveData = new ReceiveData(manager, port);
        receiveData.receiveDatafromClient();

    }
}