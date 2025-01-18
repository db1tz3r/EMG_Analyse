import Merkmalsextraktion.Merkmalsextraktion_Manager;
import Normalisierung.PeakNormalisierung;
import Normalisierung.Rms;
import UI.UpdatePlotter;

import java.util.ArrayList;
import java.util.Arrays;

public class Datenspeicher {
    private UpdatePlotter updatePlotter;
    private Rms rms;
    private PeakNormalisierung peakNormalisierung;
    private Zyklenerkennung zykluserkennung;
    private Merkmalsextraktion_Manager merkmalsextraktionManager;

    private ArrayList<Double> inputData = new ArrayList<>();
    private final double[] rmsArrayValuesInput = new double[5];
    private ArrayList<Double> rmsArrayValuesErgebnis = new ArrayList<>();
    private double[] peakNormaisierungArrayValuesInput = new double[5];
    private ArrayList<Double> peakNormalisierungArrayErgebnis = new ArrayList<>();
    private ArrayList<Double> zyklusArrayInput = new ArrayList<>();
    private ArrayList<Double> zyklusArrayWertErgebnis = new ArrayList<>();
    private ArrayList<Integer> zyklusArrayZeitErgebnis = new ArrayList<>();

    public Datenspeicher(UpdatePlotter updatePlotter, Rms rms, PeakNormalisierung peakNormalisierung, Zyklenerkennung zyklenerkennung,
                         Merkmalsextraktion_Manager merkmalsextraktionManager) {
        this.updatePlotter = updatePlotter;
        this.rms = rms;
        this.peakNormalisierung = peakNormalisierung;
        this.zykluserkennung = zyklenerkennung;
        this.merkmalsextraktionManager = merkmalsextraktionManager;
    }

    private int startIndex = 0, startPeakNormalisierungIndex = 0, startZyklenerkennungIndex = 0;

    public void start() {
        fillRMSArray(inputData.get(startIndex));
        if (startIndex > 4) {
            startRMSCalculation(rmsArrayValuesInput);
            fillPeakNormalisierungArray(rmsArrayValuesErgebnis.get(startPeakNormalisierungIndex));
            startPeakNormalisierungIndex++;

            if ((startPeakNormalisierungIndex % 5) == 0) {
                startPeakNormalisierung();
                for (int i = 0; i < 5; i++) {
                    startZykluserkennung(startZyklenerkennungIndex);
                    startZyklenerkennungIndex++;
                }
            }
        }

        //merkmalsextraktionManager.setArraysZyklenerkennung(zyklusArrayWertErgebnis, zyklusArrayZeitErgebnis, zyklusArrayInput);
        //merkmalsextraktionManager.run();

        startIndex++;
    }

    public void startZykluserkennung(int startZyklenerkennungIndex) {
        double[] ergebnis = zykluserkennung.starteZykluserkennung(peakNormalisierungArrayErgebnis.get(startZyklenerkennungIndex), 7.0);
        if (ergebnis[0] != 0) {
            for (int i = 0; i < ergebnis.length; i++) {
                zyklusArrayWertErgebnis.add(ergebnis[i]);
            }
        }
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

    public void setInputData(Double inputDataValue) {
        this.inputData.add(inputDataValue);
    }
}