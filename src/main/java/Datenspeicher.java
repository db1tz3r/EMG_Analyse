import Merkmalsextraktion.Merkmalsextraktion_Manager;
import Normalisierung.PeakNormalisierung;
import Normalisierung.Rms;
import Segmentation.Zyklenerkennung;
import Segmentation.Zyklenzusammenfassung;
import UI.UpdatePlotter;

import java.util.ArrayList;
import java.util.Arrays;

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
        fillRMSArray(Math.abs(rawData.get(startIndex))); // Nur positive Werte für RMS und Peak-Normalisierung --> Vollgleichrichtung
        if (startIndex > 4) {
            startRMSCalculation(rmsArrayValuesInput);
            fillPeakNormalisierungArray(rmsArrayValuesErgebnis.get(startPeakNormalisierungIndex));
            startPeakNormalisierungIndex++;

            if ((startPeakNormalisierungIndex % 5) == 0) {
                startPeakNormalisierung();
                for (int i = 0; i < 5; i++) {
                    startSegmentation(startZyklenerkennungIndex, startZyklenerkennungIndex,
                            7.0, 5, 20.0, 50, 50);
                    startZyklenerkennungIndex++;
                }
            }
        }
        //System.out.println(zyklusArrayWertErgebnis);
        merkmalsextraktionManager.setArraysZyklenerkennung(zyklusArrayWertErgebnis, zyklusArrayZeitErgebnis, peakNormalisierungArrayErgebnis, rawData);
        merkmalsextraktionManager.run();

        startIndex++;
    }

    private int zyklusAnschliessenNachZeitCount = 0;    // Zähler für die Anzahl an Werten ohne Zyklus
    private double[] letztesAusgegebenesErgebnis = new double[8];   // Speicher für das letzte ausgegebene Ergebnis

    public void startSegmentation(int startZyklenerkennungIndex, int globalerZeitpunkt, double schwelleProzent, int minPunkte, double minGesamtabweichung , int toleranzZwischenzyklen, int maxWerteOhneZyklus) {
        // Starte die Zykluserkennung mit dem aktuellen Index und globalem Zeitpunkt
        double[] ergebnis = zykluserkennung.starteZykluserkennung(
                peakNormalisierungArrayErgebnis.get(startZyklenerkennungIndex), schwelleProzent, globalerZeitpunkt, minPunkte, minGesamtabweichung);

        if (ergebnis[0] != 0) {
            zyklusAnschliessenNachZeitCount = 0;
            //System.out.println("Ergebnis: " + ergebnis[0] + " " + ergebnis[1] + " " + ergebnis[2] + " " + ergebnis[3]);
            //System.out.println("Zeit: " + ergebnis[4] + " " + ergebnis[5] + " " + ergebnis[6] + " " + ergebnis[7]);

            // Übergabe der erkannten Zyklen an die Zusammenfassungsmethode
            ergebnis = zyklenzusammenfassung.verarbeiteUndGebeZyklusZurueck(ergebnis, toleranzZwischenzyklen);

            // Überprüfe, ob ein vollständiger Zyklus erkannt wurde
            if (ergebnis[0] != 0 || ergebnis[1] != 0 || ergebnis[2] != 0 || ergebnis[3] != 0) {
                for (int i = 0; i < 4; i++) {
                    zyklusArrayWertErgebnis.add(ergebnis[i]);
                }
                for (int i = 4; i < 8; i++) {
                    zyklusArrayZeitErgebnis.add((int) ergebnis[i]);
                }
                // Speichere das letzte ausgegebene Ergebnis zur Vermeidung doppelter Ausgaben
                letztesAusgegebenesErgebnis = ergebnis.clone();
            }
        } else {
            zyklusAnschliessenNachZeitCount++;
            if (zyklusAnschliessenNachZeitCount >= maxWerteOhneZyklus) {
                double[] zusammengefassterZyklus = zyklenzusammenfassung.ausgabeZusammengefassterZyklus();
                if (!Arrays.equals(zusammengefassterZyklus, letztesAusgegebenesErgebnis)) {
                    zyklusAnschliessenNachZeitCount = 0;
                    for (int i = 0; i < 4; i++) {
                        zyklusArrayWertErgebnis.add(zusammengefassterZyklus[i]);
                    }
                    for (int i = 4; i < 8; i++) {
                        zyklusArrayZeitErgebnis.add((int) zusammengefassterZyklus[i]);
                    }
                    letztesAusgegebenesErgebnis = zusammengefassterZyklus.clone();
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