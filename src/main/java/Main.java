import Merkmalsextraktion.Merkmal_Speicher;
import Merkmalsextraktion.Merkmalsextraktion_Manager;
import Merkmalsextraktion.PolynomialeApproximation;
import Merkmalsextraktion.FastFourierTransformation;
import Normalisierung.PeakNormalisierung;
import Normalisierung.Rms;
import RandomForest.ModellManager;
import UI.RealTimePlotter;
import UI.UpdatePlotter;

import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        int port = 12345; // Port, auf dem der Server lauscht
        int maxWertPeakNormalisierung = 1024;   // maximaler Wert, der vom Arduino/Sensor erreicht werden kann
        int hz = 77;    // Zahl der Hz in dem die Daten übertragen werden
        boolean createCsvFile = false; // Soll eine CSV-Datei erstellt werden
        String csvFileName = "src/Data/Merkmale"; // Name der CSV-Datei, in der die Merkmale gespeichert werden
        boolean useRamdomForest = true;


        // Starten des Random Forest Modells
        if (!createCsvFile && useRamdomForest){
            ModellManager modellManager = new ModellManager();
        }else {
            System.out.println("Bitte entweder CSV-Generieren oder Modell verwenden");
            System.exit(0);
        }

        // Starten des Plotters
        RealTimePlotter plotter = new RealTimePlotter(hz);
        plotter.pack();
        plotter.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        plotter.setVisible(true);
        UpdatePlotter updatePlotter = new UpdatePlotter(plotter);

        //Starten der RMS-Klasse
        Rms rms = new Rms();

        //Starten der Peak-Normalisierung-Klasse
        PeakNormalisierung peakNormalisierung = new PeakNormalisierung(maxWertPeakNormalisierung);

        //Starten der Zykluserkennung
        Zyklenerkennung zyklenerkennung = new Zyklenerkennung();

        //Starten des Merkmalsspeichers
        Merkmal_Speicher merkmalSpeicher = new Merkmal_Speicher(csvFileName ,createCsvFile);

        // Starten der PolynomialApproximation
        PolynomialeApproximation polynomialeApproximation = new PolynomialeApproximation(merkmalSpeicher);

        // starten der FFT
        FastFourierTransformation fft = new FastFourierTransformation(merkmalSpeicher);

        //Starten der Zykluseintilung
        Merkmalsextraktion_Manager merkmalsextraktionManager = new Merkmalsextraktion_Manager(polynomialeApproximation, fft, merkmalSpeicher);

        // Starten der allgemeinen Speicherklasse
        Datenspeicher datenspeicher = new Datenspeicher(updatePlotter, rms, peakNormalisierung, zyklenerkennung, merkmalsextraktionManager);

        // Starten der Übertragung des Clients/Sensors
        ReceiveData receiveData = new ReceiveData(datenspeicher, port);
        receiveData.receiveDatafromClient();

    }
}