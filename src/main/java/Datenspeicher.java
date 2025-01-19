import Merkmalsextraktion.Merkmalsextraktion_Manager;
import Normalisierung.PeakNormalisierung;
import Normalisierung.Rms;
import Segmentation.Zyklenerkennung;
import Segmentation.Zyklenzusammenfassung;
import UI.UpdatePlotter;

import java.util.ArrayList;

public class Datenspeicher {
    private UpdatePlotter updatePlotter;
    private Rms rms;
    private PeakNormalisierung peakNormalisierung;
    private Zyklenerkennung zykluserkennung;
    private Merkmalsextraktion_Manager merkmalsextraktionManager;
    private Zyklenzusammenfassung zyklenzusammenfassung;

    private ArrayList<Double> rawData = new ArrayList<>();
    private final double[] rmsArrayValuesInput = new double[5];
    private ArrayList<Double> rmsArrayValuesErgebnis = new ArrayList<>();
    private double[] peakNormaisierungArrayValuesInput = new double[5];
    private ArrayList<Double> peakNormalisierungArrayErgebnis = new ArrayList<>();
    private ArrayList<Double> zyklusArrayWertErgebnis = new ArrayList<>();
    private ArrayList<Integer> zyklusArrayZeitErgebnis = new ArrayList<>();

    public Datenspeicher(UpdatePlotter updatePlotter, Rms rms, PeakNormalisierung peakNormalisierung, Zyklenerkennung zyklenerkennung,
                         Merkmalsextraktion_Manager merkmalsextraktionManager, Zyklenzusammenfassung zyklenzusammenfassung) {
        this.updatePlotter = updatePlotter;
        this.rms = rms;
        this.peakNormalisierung = peakNormalisierung;
        this.zykluserkennung = zyklenerkennung;
        this.merkmalsextraktionManager = merkmalsextraktionManager;
        this.zyklenzusammenfassung = zyklenzusammenfassung;
    }

    private int startIndex = 0, startPeakNormalisierungIndex = 0, startZyklenerkennungIndex = 0;

    public void start() {
        //System.out.println("Rohdaten: " + rawData.get(startIndex));
        fillRMSArray(Math.abs(rawData.get(startIndex))); // Nur positive Werte für RMS und Peak-Normalisierung
        if (startIndex > 4) {
            startRMSCalculation(rmsArrayValuesInput);
            fillPeakNormalisierungArray(rmsArrayValuesErgebnis.get(startPeakNormalisierungIndex));
            startPeakNormalisierungIndex++;

            if ((startPeakNormalisierungIndex % 5) == 0) {
                startPeakNormalisierung();
                for (int i = 0; i < 5; i++) {
                    startSegmentation(startZyklenerkennungIndex, startZyklenerkennungIndex);
                    startZyklenerkennungIndex++;
                }
            }
        }
        //System.out.println(zyklusArrayWertErgebnis);
        merkmalsextraktionManager.setArraysZyklenerkennung(zyklusArrayWertErgebnis, zyklusArrayZeitErgebnis, peakNormalisierungArrayErgebnis, rawData);
        merkmalsextraktionManager.run();

        startIndex++;
    }

    public void startSegmentation(int startZyklenerkennungIndex, int globalerZeitpunkt) {
        double[] ergebnis = zykluserkennung.starteZykluserkennung(peakNormalisierungArrayErgebnis.get(startZyklenerkennungIndex), 7.0, globalerZeitpunkt);

        if (ergebnis[0] != 0) {
            //System.out.println("Ergebnis: " + ergebnis[0] + " " + ergebnis[1] + " " + ergebnis[2] + " " + ergebnis[3]);
            //System.out.println(ergebnis[4] + " " + ergebnis[5] + " " + ergebnis[6] + " " + ergebnis[7]);
            ergebnis  = zyklenzusammenfassung.verarbeiteUndGebeZyklusZurueck(ergebnis);
            if (ergebnis[0] != 0) {
                for (int i = 0; i < 4; i++) {
                    zyklusArrayWertErgebnis.add(ergebnis[i]);
                    //System.out.println("Zykluswert: " + ergebnis[i]);
                }
                for (int i = 4; i < 8; i++) {
                    zyklusArrayZeitErgebnis.add((int) ergebnis[i]);
                    //System.out.println("Zykluszeit: " + ergebnis[i]);
                }
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
        this.rawData.add(inputDataValue);
    }
}