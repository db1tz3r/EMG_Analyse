import Merkmalsextraktion.Merkmalsextraktion_Manager;
import Normalisierung.PeakNormalisierung;
import Normalisierung.Rms;
import UI.UpdatePlotter;

import java.util.ArrayList;
import java.util.Arrays;

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
    private final double[] rmsArrayValuesInput = new double[5]; //Array für die Weitergabe der Eingabeergebnisse(unberechnet)
    private ArrayList<Double> rmsArrayValuesErgebnis = new ArrayList<Double>(); //Arraylist mit Ergebnis aus der RMS Berechnung (berechnet)
    //Peak-Normalisierung-Speicher
    private double[] peakNormaisierungArrayValuesInput = new double[5]; //Array für die Berechnung der Peak-Normalisierung
    private ArrayList<Double> peakNormalisierungArrayErgebnis = new ArrayList<Double>();    //Arraylist mit Ergebnissen der Peak-Normalisierung
    //Zykluserkennungs-Speicher
    private ArrayList<Double> zyklusArrayInput = new ArrayList<Double>();  //Array zum Berechnen eines Zyklus
    private ArrayList<Double> zyklusArrayWertErgebnis = new ArrayList<Double>();  //Arraylist mit Ergebnissen der Zyklengrenzen Wert
    private ArrayList<Integer> zyklusArrayZeitErgebnis = new ArrayList<Integer>();  //Arraylist mit Ergebnissen der Zyklengrenzen Zeit



    //Konstruktor
    public Datenspeicher(UpdatePlotter updatePlotter, Rms rms, PeakNormalisierung peakNormalisierung, Zyklenerkennung zyklenerkennung,
                         Merkmalsextraktion_Manager merkmalsextraktionManager) {
        this.updatePlotter = updatePlotter;
        this.rms = rms;
        this.peakNormalisierung = peakNormalisierung;
        this.zykluserkennung = zyklenerkennung;
        this.merkmalsextraktionManager = merkmalsextraktionManager;
    }


    //Control-Methode
    private int startIndex = 0, startPeakNormalisierungIndex = 0, startZyklenerkennungIndex = 0;

    public void start() {
        //System.out.println("Input: " + inputData.get(startIndex));
        fillRMSArray(inputData.get(startIndex));    //Füllen des RMS-Array
        if (startIndex > 4) {
            startRMSCalculation(rmsArrayValuesInput);   //Ausführen der RMS-Calculation, sobald der Array das erste mal gefüllt ist
            //System.out.println("RMS-Ergebnis: " + rmsArrayValuesErgebnis.get(startPeakNormalisierungIndex));

            fillPeakNormalisierungArray(rmsArrayValuesErgebnis.get(startPeakNormalisierungIndex));
            startPeakNormalisierungIndex++;

            if ((startPeakNormalisierungIndex % 5) == 0 && startPeakNormalisierungIndex != 0) {  //Normalisierungsberechnung und zyklusarray jede 5 Durchgänge neu befüllen
                startPeakNormalisierung();
                //System.out.println("Peak-Norm.-Ergebnis:" + peakNormalisierungArrayErgebnis.get(startZyklenerkennungIndex));

                for (int i = 0; i < 5; i++) {
                    zyklusArrayInput.add(peakNormalisierungArrayErgebnis.get(startZyklenerkennungIndex));
                    //System.out.println(zyklusArrayInput);
                    startZykluserkennung();
                    startZyklenerkennungIndex++;
                }
            }
        }

        merkmalsextraktionManager.setArraysZyklenerkennung(zyklusArrayWertErgebnis, zyklusArrayZeitErgebnis, zyklusArrayInput);
        merkmalsextraktionManager.run();

        startIndex++;
    }
//Ende der Control Methode


    //Start der Zyklusmethoden

    //Start der Zyklusberechnung
    public void startZykluserkennung() {
        double[] ergebnis = zykluserkennung.starteZykluserkennung(zyklusArrayInput,7.0, 10.0);
        if (ergebnis[0] != 0) {
            if (ergebnis[2] == 2) {
                //System.out.println("Zyklenwert: " + ergebnis[0]);
                zyklusArrayWertErgebnis.add(ergebnis[0]);
                //System.out.println("Zyklenzeitpunkt:" + ergebnis[1]);
                zyklusArrayZeitErgebnis.add((int) (ergebnis[1]));
            }
            //System.out.println("Zyklenwert: " + ergebnis[0]);
            zyklusArrayWertErgebnis.add(ergebnis[0]);
            //System.out.println("Zyklenzeitpunkt:" + ergebnis[1]);
            zyklusArrayZeitErgebnis.add((int) (ergebnis[1]));
        }
    }
//Ende der Zyklus Methoden


    //Peak-Normalisierung Methoden:
    //Füllen das Input Arrays
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
        //System.out.println("Eingang Peak-Norm.: " + Arrays.toString(peakNormaisierungArrayValuesInput));  //Input für die Peak-Normalisierung
    }

    //Start Peak-Normalisierung Berechnung
    public void startPeakNormalisierung() {
        //System.out.println(Arrays.toString(peakNormaisierungArrayValuesInput));
        double[] peakNormalisierungErgebnisse = peakNormalisierung.normalizePeak(peakNormaisierungArrayValuesInput);
        for (int i = 0; i < peakNormalisierungErgebnisse.length; i++) {
            //System.out.println("Peak-Norm-Ergebnis: " + peakNormalisierungErgebnisse[i]);
            peakNormalisierungArrayErgebnis.add(peakNormalisierungErgebnisse[i]);

            //Plotter Raw-Daten befüllen und Thread ausführen
//            updatePlotter.setList3((int) peakNormalisierungErgebnisse[i]);
//            updatePlotter.run();
        }
    }
//Ende Peak-Normalisierungs Methoden


    //RMS-Berechnung Methoden:
    //Methode zum befüllen des RMS Arrays, der 3 Werte bekommt und anschließend den ältesten löscht und einen neuen reinschreibt
    int rmsArrayIndexInput = 0;
    int rmsPlotterIndex = 0;

    public void fillRMSArray(double value) {
        //Plotter Raw-Daten befüllen und Thread ausführen
        //updatePlotter.setList1((int) value);
        //updatePlotter.run();

        //Plotter RMS-Daten befüllen und Thread ausführen
//        if (rmsPlotterIndex == 0 || rmsPlotterIndex == 1) {
//            updatePlotter.setList2((int) value);
//            updatePlotter.run();
//            rmsPlotterIndex++;
//        }
        // Wenn das Array voll ist (3), löschen wir die älteste Zahl und schreiben einen neuen rein
        if (rmsArrayIndexInput >= rmsArrayValuesInput.length) {
            // Überprüfe die Länge des Arrays
            if (rmsArrayValuesInput.length == 3) {
                // Wenn das Array genau 3 Elemente hat, schiebe die Werte nach vorne
                rmsArrayValuesInput[0] = rmsArrayValuesInput[1];
                rmsArrayValuesInput[1] = rmsArrayValuesInput[2];
                rmsArrayValuesInput[2] = value;
            } else if (rmsArrayValuesInput.length > 3) {
                // Für Arrays mit mehr als 3 Elementen: Schiebe alle Werte um eins nach vorne
                System.arraycopy(rmsArrayValuesInput, 1, rmsArrayValuesInput, 0, rmsArrayValuesInput.length - 1);
                rmsArrayValuesInput[rmsArrayValuesInput.length - 1] = value;
            } else {
                // Falls das Array weniger als 3 Elemente hat, überschreibe einfach das älteste Element
                rmsArrayValuesInput[rmsArrayIndexInput % rmsArrayValuesInput.length] = value;
            }
        } else {
            // Füge den Wert hinzu, wenn der Index innerhalb der Array-Grenzen liegt
            rmsArrayValuesInput[rmsArrayIndexInput] = value;
            rmsArrayIndexInput++;
        }
    }

    //Starten der RMS-Berechnung und Rückgabe in dern Ergebnis Array
    public void startRMSCalculation(double[] rmsArrayValuesInput) {
        //System.out.println(Arrays.toString(rmsArrayValuesInput));
        double rmsErgenis = rms.rmsCalculation(rmsArrayValuesInput);
        //System.out.println("RMS-Ergebnis: " + rmsErgenis);
        rmsArrayValuesErgebnis.add(rmsErgenis);

        //Plotter befüllen und Thread ausführen
//        updatePlotter.setList2((int) rmsErgenis);
//        updatePlotter.run();

    }
// Ende der RMS-Berechnungs-Methoden

    //InputWerte in Arraylist schreiben:
    public void setInputData(Double inputDataValue) {
        this.inputData.add(inputDataValue);
    }

}