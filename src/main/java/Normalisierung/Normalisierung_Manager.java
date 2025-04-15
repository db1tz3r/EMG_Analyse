package Normalisierung;

import java.util.ArrayList;

public class Normalisierung_Manager {
    private Rms rms; // Instanz der RMS-Berechnungsklasse
    private PeakNormalisierung peakNormalisierung; // Instanz der Peak-Normalisierungsklasse

    // Arrays und Listen zur Speicherung von Eingabe- und Ergebniswerten für RMS und Peak-Normalisierung
    private final double[] rmsArrayValuesInput = new double[5]; // Eingabewerte für RMS-Berechnung
    private ArrayList<Double> rmsArrayValuesErgebnis = new ArrayList<>(); // Ergebniswerte der RMS-Berechnung
    private double[] peakNormaisierungArrayValuesInput = new double[5]; // Eingabewerte für Peak-Normalisierung
    private ArrayList<Double> peakNormalisierungArrayErgebnis = new ArrayList<>(); // Ergebniswerte der Peak-Normalisierung

    // Konstruktor zur Initialisierung der Normalisierungskomponenten
    public Normalisierung_Manager(Rms rms, PeakNormalisierung peakNormalisierung) {
        this.rms = rms;
        this.peakNormalisierung = peakNormalisierung;
    }

    // Indizes zur Steuerung der Verarbeitung
    private int startIndex = 0, startPeakNormalisierungIndex = 0;

    // Methode zur Durchführung der Normalisierung
    public ArrayList<Double> startNormalisierung(ArrayList<Double> rawData) {
        // Füllt das RMS-Array mit dem absoluten Wert der aktuellen Rohdaten
        fillRMSArray(Math.abs(rawData.get(startIndex)));

        // Wenn mindestens 5 Werte im RMS-Array vorhanden sind
        if (startIndex > 4) {
            // Startet die RMS-Berechnung
            startRMSCalculation(rmsArrayValuesInput);

            // Füllt das Peak-Normalisierungs-Array mit dem Ergebnis der RMS-Berechnung
            fillPeakNormalisierungArray(rmsArrayValuesErgebnis.get(startPeakNormalisierungIndex));
            startPeakNormalisierungIndex++;

            // Wenn 5 Werte für die Peak-Normalisierung gesammelt wurden
            if ((startPeakNormalisierungIndex % 5) == 0) {
                // Startet die Peak-Normalisierung
                startPeakNormalisierung();
                startIndex++;
                return peakNormalisierungArrayErgebnis; // Gibt die Ergebnisse der Peak-Normalisierung zurück
            }
        }

        // Erhöht den Index für die nächste Iteration
        startIndex++;

        // Gibt eine leere Liste zurück, wenn noch keine Ergebnisse vorliegen
        return new ArrayList<>();
    }

    int peakNormalisierungIndex = 0; // Index für das Peak-Normalisierungs-Array

    // Methode zum Füllen des Peak-Normalisierungs-Arrays
    public void fillPeakNormalisierungArray(double value) {
        if (peakNormalisierungIndex < 5) {
            // Fügt den Wert in das Array ein, wenn noch Platz vorhanden ist
            peakNormaisierungArrayValuesInput[peakNormalisierungIndex] = value;
            peakNormalisierungIndex++;
        } else {
            // Setzt das Array zurück und fügt den neuen Wert an die erste Position ein
            peakNormaisierungArrayValuesInput = new double[5];
            peakNormaisierungArrayValuesInput[0] = value;
            peakNormalisierungIndex = 1;
        }
    }

    // Methode zur Durchführung der Peak-Normalisierung
    public void startPeakNormalisierung() {
        // Führt die Peak-Normalisierung durch und speichert die Ergebnisse
        double[] peakNormalisierungErgebnisse = peakNormalisierung.normalizePeak(peakNormaisierungArrayValuesInput);
        for (double ergebnis : peakNormalisierungErgebnisse) {
            peakNormalisierungArrayErgebnis.add(ergebnis);
        }
    }

    int rmsArrayIndexInput = 0; // Index für das RMS-Array

    // Methode zum Füllen des RMS-Arrays
    public void fillRMSArray(double value) {
        if (rmsArrayIndexInput >= rmsArrayValuesInput.length) {
            // Verschiebt die Werte im Array, um Platz für den neuen Wert zu schaffen
            System.arraycopy(rmsArrayValuesInput, 1, rmsArrayValuesInput, 0, rmsArrayValuesInput.length - 1);
            rmsArrayValuesInput[rmsArrayValuesInput.length - 1] = value;
        } else {
            // Fügt den Wert in das Array ein, wenn noch Platz vorhanden ist
            rmsArrayValuesInput[rmsArrayIndexInput] = value;
            rmsArrayIndexInput++;
        }
    }

    // Methode zur Durchführung der RMS-Berechnung
    public void startRMSCalculation(double[] rmsArrayValuesInput) {
        // Berechnet den RMS-Wert und fügt ihn der Ergebnisliste hinzu
        double rmsErgenis = rms.rmsCalculation(rmsArrayValuesInput);
        rmsArrayValuesErgebnis.add(rmsErgenis);
    }
}