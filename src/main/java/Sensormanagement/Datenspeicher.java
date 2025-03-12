package Sensormanagement;

import Merkmalsextraktion.Merkmalsextraktion_Manager;
import Normalisierung.PeakNormalisierung;
import Normalisierung.Rms;
import Segmentation.Zyklenerkennung;
import Segmentation.Zyklenmanager;
import Segmentation.Zyklenzusammenfassung;
import UI.UpdatePlotter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Datenspeicher {
    private UpdatePlotter updatePlotter;
    private Rms rms;
    private PeakNormalisierung peakNormalisierung;
    private Merkmalsextraktion_Manager merkmalsextraktionManager;
    private Zyklenmanager zyklenmanager;

    private ArrayList<Double> rawData = new ArrayList<>();
    private final double[] rmsArrayValuesInput = new double[5];
    private ArrayList<Double> rmsArrayValuesErgebnis = new ArrayList<>();
    private double[] peakNormaisierungArrayValuesInput = new double[5];
    private ArrayList<Double> peakNormalisierungArrayErgebnis = new ArrayList<>();

    public Datenspeicher(UpdatePlotter updatePlotter, Rms rms, PeakNormalisierung peakNormalisierung, Merkmalsextraktion_Manager merkmalsextraktionManager, Zyklenmanager zyklenmanager) {
        this.updatePlotter = updatePlotter;
        this.rms = rms;
        this.peakNormalisierung = peakNormalisierung;
        this.merkmalsextraktionManager = merkmalsextraktionManager;
        this.zyklenmanager = zyklenmanager;
    }

    private int startIndex = 0, startPeakNormalisierungIndex = 0, startZyklenerkennungIndex = 0;

    public void start() {
        //System.out.println("Rohdaten: " + rawData.get(startIndex));
        fillRMSArray(Math.abs(rawData.get(startIndex))); // Nur positive Werte für RMS und Peak-Normalisierung --> Vollgleichrichtung
        if (startIndex > 4) {
            startRMSCalculation(rmsArrayValuesInput);
            fillPeakNormalisierungArray(rmsArrayValuesErgebnis.get(startPeakNormalisierungIndex));
            startPeakNormalisierungIndex++;

            if ((startPeakNormalisierungIndex % 5) == 0) {
                startPeakNormalisierung();
                for (int i = 0; i < 5; i++) {
                    zyklenmanager.startSegmentation(startZyklenerkennungIndex, startZyklenerkennungIndex,
                            7.0, 5, 30.0, 120, 120,
                            peakNormalisierungArrayErgebnis, rawData);
                    startZyklenerkennungIndex++;
                }
            }
        }
        //System.out.println(zyklusArrayWertErgebnis);
        //merkmalsextraktionManager.setArraysZyklenerkennung(zyklusArrayWertErgebnis, zyklusArrayZeitErgebnis, peakNormalisierungArrayErgebnis, rawData);
        //merkmalsextraktionManager.run();

        startIndex++;
    }




    int peakNormalisierungIndex = 0;

    public void fillPeakNormalisierungArray(double value) {
        if (peakNormalisierungIndex < 5) {
            peakNormaisierungArrayValuesInput[peakNormalisierungIndex] = value;
            peakNormalisierungIndex++;
        } else {
            peakNormaisierungArrayValuesInput = new double[5];
            peakNormaisierungArrayValuesInput[0] = value;
            peakNormalisierungIndex = 1;
        }
    }

    public void startPeakNormalisierung() {
        double[] peakNormalisierungErgebnisse = peakNormalisierung.normalizePeak(peakNormaisierungArrayValuesInput);
        for (double ergebnis : peakNormalisierungErgebnisse) {
            peakNormalisierungArrayErgebnis.add(ergebnis);
        }
    }

    int rmsArrayIndexInput = 0;

    public void fillRMSArray(double value) {
        if (rmsArrayIndexInput >= rmsArrayValuesInput.length) {
            System.arraycopy(rmsArrayValuesInput, 1, rmsArrayValuesInput, 0, rmsArrayValuesInput.length - 1);
            rmsArrayValuesInput[rmsArrayValuesInput.length - 1] = value;
        } else {
            rmsArrayValuesInput[rmsArrayIndexInput] = value;
            rmsArrayIndexInput++;
        }
    }

    public void startRMSCalculation(double[] rmsArrayValuesInput) {
        double rmsErgenis = rms.rmsCalculation(rmsArrayValuesInput);
        rmsArrayValuesErgebnis.add(rmsErgenis);
    }

    public void setInputData(List<Double> inputDataValue) {
        this.rawData.addAll(inputDataValue);
    }
}