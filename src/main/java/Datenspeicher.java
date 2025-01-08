import Merkmalsextraktion.Merkmalsextraktion_Manager;
import Normalisierung.PeakNormalisierung;
import Normalisierung.Rms;
import UI.UpdatePlotter;

import java.util.ArrayList;

public class Datenspeicher {
    //Einbinden der unterschiedlichen Klassen/Analyseverfahren
    private UpdatePlotter updatePlotter;
    private Rms rms;
    private PeakNormalisierung peakNormalisierung;
    private Zyklenerkennung zykluserkennung;
    private Merkmalsextraktion_Manager merkmalsextraktionManager;

    //Imlementierung der Speicher/Arrays
    //private ArrayList<Double> ergebnisALl = new ArrayList<Double>();   //Array für engültiges Ergebnis/dauerhafte Erweiterung
//Input-Daten
    private ArrayList<Double> inputData = new ArrayList<Double>();
    //RMS-Berechungs-Speicher
    private final double[] rmsArrayValuesInput = new double[3]; //Array für die Weitergabe der Eingabeergebnisse(unberechnet)
    private ArrayList<Double> rmsArrayValuesErgebnis = new ArrayList<Double>(); //Arraylist mit Ergebnis aus der RMS Berechnung (berechnet)
    //Peak-Normalisierung-Speicher
    private double[] peakNormaisierungArrayValuesInput = new double[5]; //Array für die Berechnung der Peak-Normalisierung
    private ArrayList<Double> peakNormalisierungArrayErgebnis = new ArrayList<Double>();    //Arraylist mit Ergebnissen der Peak-Normalisierung
    //Zykluserkennungs-Speicher
    private ArrayList<Double> zyklusArrayInput = new ArrayList<Double>();  //Array zum Berechnen eines Zyklus
    private ArrayList<Double> zyklusArrayWertErgebnis = new ArrayList<Double>();  //Arraylist mit Ergebnissen der Zyklengrenzen Wert
    private ArrayList<Integer> zyklusArrayZeitErgebnis = new ArrayList<Integer>();  //Arraylist mit Ergebnissen der Zyklengrenzen Zeit


    //Konstruktor
    public Datenspeicher(UpdatePlotter updatePlotter, Rms rms, PeakNormalisierung peakNormalisierung, Zyklenerkennung zyklenerkennung, Merkmalsextraktion_Manager merkmalsextraktionManager) {
        this.updatePlotter = updatePlotter;
        this.rms = rms;
        this.peakNormalisierung = peakNormalisierung;
        this.zykluserkennung = zyklenerkennung;
        this.merkmalsextraktionManager = merkmalsextraktionManager;
    }


    //Control-Methode
    private int startIndex = 0, startPeakNormalisierungIndex = 0, startZyklenerkennungIndex = 0;

    public void start() {
        fillRMSArray(inputData.get(startIndex));    //Füllen des RMS-Array
        if (startIndex > 2) {
            startRMSCalculation(rmsArrayValuesInput);   //Ausführen der RMS-Calculation, sobald der Array das erste mal gefüllt ist
            fillPeakNormalisierungArray(rmsArrayValuesErgebnis.get(startPeakNormalisierungIndex));
            startPeakNormalisierungIndex++;
        }
        if ((startIndex % 5) == 0 && startIndex != 0) {  //Normalisierungsberechnung und zyklusarray jede 5 Durchgänge neu befüllen
            startPeakNormalisierung();

            for (int i = 0; i < 5; i++) {
                fillZyklusArray(peakNormalisierungArrayErgebnis.get(startZyklenerkennungIndex));
                //System.out.println(zyklusArrayInput);
                startZykluserkennung();
                startZyklenerkennungIndex++;
            }
        }

        merkmalsextraktionManager.setArraysZyklenerkennung(zyklusArrayWertErgebnis, zyklusArrayZeitErgebnis, zyklusArrayInput);
        merkmalsextraktionManager.run();

        startIndex++;
    }
//Ende der Control Methode


    //Start der Zyklusmethoden
    //Befüllen des Input Arrays der Zyklen
    public void fillZyklusArray(double value) {
        zyklusArrayInput.add(value);
    }

    //Start der Zyklusberechnung
    public void startZykluserkennung() {
        double[] ergebnis = zykluserkennung.starteZyklenerkennung(zyklusArrayInput);
        if (ergebnis[0] != 0) {
            if (ergebnis[2] == 2) {
                System.out.println("Zyklenwert: " + ergebnis[0]);
                zyklusArrayWertErgebnis.add(ergebnis[0]);
                System.out.println("Zyklenzeitpunkt:" + ergebnis[1]);
                zyklusArrayZeitErgebnis.add((int) (ergebnis[1]));
            }
            System.out.println("Zyklenwert: " + ergebnis[0]);
            zyklusArrayWertErgebnis.add(ergebnis[0]);
            System.out.println("Zyklenzeitpunkt:" + ergebnis[1]);
            zyklusArrayZeitErgebnis.add((int) (ergebnis[1]));
        }
    }
//Ende der Zyklus Methoden


    //Peak-Normalisierung Methoden:
    //Füllen das Input Arrays
    int peakNormalisierungIndex = 2, peakInitalisierungsIndex = 0;

    public void fillPeakNormalisierungArray(double value) {
        if (peakInitalisierungsIndex == 0) {
            for (int i = 0; i < 2; i++) {
                peakNormaisierungArrayValuesInput[i] = inputData.get(i);
            }
            peakInitalisierungsIndex++;
        }
        if (peakNormalisierungIndex < 5) {
            peakNormaisierungArrayValuesInput[peakNormalisierungIndex] = value;
            peakNormalisierungIndex++;
        } else {
            peakNormaisierungArrayValuesInput = new double[5];
            peakNormaisierungArrayValuesInput[0] = value;
            peakNormalisierungIndex = 1;
        }

    }

    //Start Peak-Normalisierung Berechnung
    public void startPeakNormalisierung() {
        double[] peakNormalisierungErgebnisse = peakNormalisierung.normalizePeak(peakNormaisierungArrayValuesInput);
        for (int i = 0; i < peakNormalisierungErgebnisse.length; i++) {
            //System.out.println("Peak-Norm-Ergebnis: " + peakNormalisierungErgebnisse[i]);
            peakNormalisierungArrayErgebnis.add(peakNormalisierungErgebnisse[i]);

            //Plotter Raw-Daten befüllen und Thread ausführen
            updatePlotter.setList3((int) peakNormalisierungErgebnisse[i]);
            updatePlotter.run();
        }
    }
//Ende Peak-Normalisierungs Methoden


    //RMS-Berechnung Methoden:
    //Methode zum befüllen des RMS Arrays, der 3 Werte bekommt und anschließend den ältesten löscht und einen neuen reinschreibt
    int rmsArrayIndexInput = 0;
    int rmsPlotterIndex = 0;

    public void fillRMSArray(double value) {
        //Plotter Raw-Daten befüllen und Thread ausführen
        updatePlotter.setList1((int) value);
        updatePlotter.run();

        //Plotter RMS-Daten befüllen und Thread ausführen
        if (rmsPlotterIndex == 0 || rmsPlotterIndex == 1) {
            updatePlotter.setList2((int) value);
            updatePlotter.run();
            rmsPlotterIndex++;
        }
        // Wenn das Array voll ist (3), löschen wir die älteste Zahl und schreiben einen neuen rein
        if (rmsArrayIndexInput >= rmsArrayValuesInput.length) {
            //Die Werte eins nach vorne schieben, sodass der letzte wert leer wird
            rmsArrayValuesInput[0] = rmsArrayValuesInput[1];
            rmsArrayValuesInput[1] = rmsArrayValuesInput[2];
            rmsArrayValuesInput[2] = value;
            // Setze den Index zurück und lösche die alten Werte
            //rmsArrayIndexInput = 2;
        } else {
            // Füge den Wert hinzu
            rmsArrayValuesInput[rmsArrayIndexInput] = value;
            rmsArrayIndexInput++;
        }
    }

    //Starten der RMS-Berechnung und Rückgabe in dern Ergebnis Array
    public void startRMSCalculation(double[] rmsArrayValuesInput) {
        double rmsErgenis = rms.rmsCalculation(rmsArrayValuesInput);
        //System.out.println("RMS-Ergebnis: " + rmsErgenis);
        rmsArrayValuesErgebnis.add(rmsErgenis);

        //Plotter befüllen und Thread ausführen
        updatePlotter.setList2((int) rmsErgenis);
        updatePlotter.run();

    }
// Ende der RMS-Berechnungs-Methoden

    //InputWerte in Arraylist schreiben:
    public void setInputData(Double inputDataValue) {
        this.inputData.add(inputDataValue);
    }

}