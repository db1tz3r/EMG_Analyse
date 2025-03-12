package Normalisierung;

import UI.UpdatePlotter;

import java.util.ArrayList;

public class Normalisierung_Manager {
    private UpdatePlotter updatePlotter;
    private Rms rms;
    private PeakNormalisierung peakNormalisierung;

    private final double[] rmsArrayValuesInput = new double[5];
    private ArrayList<Double> rmsArrayValuesErgebnis = new ArrayList<>();
    private double[] peakNormaisierungArrayValuesInput = new double[5];
    private ArrayList<Double> peakNormalisierungArrayErgebnis = new ArrayList<>();

    public Normalisierung_Manager(UpdatePlotter updatePlotter, Rms rms, PeakNormalisierung peakNormalisierung) {
        this.updatePlotter = updatePlotter;
        this.rms = rms;
        this.peakNormalisierung = peakNormalisierung;
    }

    private int startIndex = 0, startPeakNormalisierungIndex = 0;

    public ArrayList<Double> startNormalisierung(ArrayList<Double> rawData) {
//        System.out.println("Rohdaten: " + rawData.get(startIndex));
        fillRMSArray(Math.abs(rawData.get(startIndex))); // Nur positive Werte für RMS und Peak-Normalisierung --> Vollgleichrichtung
        if (startIndex > 4) {
            startRMSCalculation(rmsArrayValuesInput);
            fillPeakNormalisierungArray(rmsArrayValuesErgebnis.get(startPeakNormalisierungIndex));
            startPeakNormalisierungIndex++;

            if ((startPeakNormalisierungIndex % 5) == 0) {
                startPeakNormalisierung();
                startIndex++;
                return peakNormalisierungArrayErgebnis;
            }
        }


        startIndex++;

        return new ArrayList<>();
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
}