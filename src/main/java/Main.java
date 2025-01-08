import Merkmalsextraktion.Merkmalsextraktion;
import Merkmalsextraktion.PolynomialeApproximation;
import Merkmalsextraktion.FastFourierTransformation;

import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        int port = 12345; // Port, auf dem der Server lauscht
        int maxWertPeakNormalisierung = 1024;   // maximaler Wert, der vom Arduino/Sensor erreicht werden kann
        int hz = 77;    // Zahl der Hz in dem die Daten übertragen werden



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

        // Starten der PolynomialApproximation
        PolynomialeApproximation polynomialeApproximation = new PolynomialeApproximation();

        // starten der FFT
        FastFourierTransformation fft = new FastFourierTransformation();

        //Starten der Zykluseintilung
        Merkmalsextraktion merkmalsextraktion = new Merkmalsextraktion(polynomialeApproximation, fft);

        // Starten der allgemeinen Speicherklasse
        Datenspeicher datenspeicher = new Datenspeicher(updatePlotter, rms, peakNormalisierung, zyklenerkennung, merkmalsextraktion);

        //Starten der Übertragung des Clients/Sensors
        ReceiveData receiveData = new ReceiveData(datenspeicher, port);
        receiveData.receiveDatafromClient();

    }
}