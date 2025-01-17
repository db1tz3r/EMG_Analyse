package Merkmalsextraktion;

import java.util.ArrayList;

public class Merkmalsextraktion_Manager implements Runnable {

    private int zyklusArrayWertErgebnisSeizeOld;
    private PolynomialeApproximation polynomialeApproximation;
    private FastFourierTransformation fastfouriertransformation;
    private Merkmal_Speicher merkmalSpeicher;

    private ArrayList<Double> zyklusArrayWertErgebnis = new ArrayList<Double>();
    private ArrayList<Integer> zyklusArrayZeitErgebnis = new ArrayList<Integer>();
    private ArrayList<Double> zyklusArrayInput = new ArrayList<Double>();

    public Merkmalsextraktion_Manager(PolynomialeApproximation polynomialeApproximation, FastFourierTransformation fft, Merkmal_Speicher merkmalSpeicher) {
        this.polynomialeApproximation = polynomialeApproximation;
        this.fastfouriertransformation = fft;
        this.merkmalSpeicher = merkmalSpeicher;
    }



    @Override
    public void run() {

        if (zyklusArrayWertErgebnis.size() >= 4 && zyklusArrayWertErgebnis.size() != zyklusArrayWertErgebnisSeizeOld) {
            if (zyklusArrayWertErgebnis.size() % 4 == 0) {
                // Extrahiere die letzten vier Werte
                double startSteigung = zyklusArrayWertErgebnis.get(zyklusArrayWertErgebnis.size() - 4);
                double endeSteigung = zyklusArrayWertErgebnis.get(zyklusArrayWertErgebnis.size() - 3);
                double startSenkung = zyklusArrayWertErgebnis.get(zyklusArrayWertErgebnis.size() - 2);
                double endeSenkung = zyklusArrayWertErgebnis.get(zyklusArrayWertErgebnis.size() - 1);

                // Überprüfe, ob die Werte gültige Bedingungen für einen Zyklus erfüllen
                if (endeSteigung > startSteigung && startSenkung > endeSteigung && endeSenkung < startSenkung && endeSteigung > 100) {
                    System.out.println("Kompletter Muskelzyklus erkannt");
                    System.out.printf("Start Steigung: %.2f, Ende Steigung: %.2f, Start Senkung: %.2f, Ende Senkung: %.2f%n",
                            startSteigung, endeSteigung, startSenkung, endeSenkung);

                    // Überprüfe zusätzliche Bedingungen
                    if (startSteigung < endeSteigung && startSenkung > endeSenkung) {
                        System.out.println("Zyklus erfüllt zusätzliche Bedingungen: Steigung und Senkung korrekt geordnet.");
                    } else {
                        System.out.println("Zyklus erfüllt nicht die zusätzlichen Bedingungen.");
                        return;
                    }

                    // Speichere die Werte im Merkmalspeicher
                    merkmalSpeicher.setMinMaxValues(startSteigung, endeSteigung, startSenkung, endeSenkung);

                    // Starte die polynomiale Approximation für verschiedene Phasen
                    startePolynomialeApproximationAnfang(startSteigung, endeSteigung);
                    startePolynomialeApproximationMitte(endeSteigung, startSenkung);
                    startePolynomialeApproximationEnde(startSenkung, endeSenkung);
                    startePolynomialeApproximationGesamterZyklus(startSteigung, endeSenkung);

                    // Starte die FFT (Fast Fourier Transformation) für den gesamten Zyklus
                    starteFFT(startSteigung, endeSenkung);
                } else {
                    System.out.println("Unvollständiger Zyklus oder Rauschen erkannt.");
                }
            }
        }


        /*if (zyklusArrayWertErgebnis.size() >= 1 && zyklusArrayWertErgebnis.size() != zyklusArrayWertErgebnisSeizeOld) {
            if (zyklusArrayWertErgebnis.size() % 4 == 0) {

                // Hole die 4 Werte ab dem Index i
                double val1 = zyklusArrayWertErgebnis.get(zyklusArrayWertErgebnis.size() - 4);
                double val2 = zyklusArrayWertErgebnis.get(zyklusArrayWertErgebnis.size() - 3);
                double val3 = zyklusArrayWertErgebnis.get(zyklusArrayWertErgebnis.size() - 2);
                double val4 = zyklusArrayWertErgebnis.get(zyklusArrayWertErgebnis.size() - 1);

                //System.out.println("val1: " + val1 + " val2: " + val2 + " val3: " + val3 + " val4: " + val4); //Zur Analyse der Ergebnisse

                // Überprüfe die Bedingungen und starte Berechnungen
                if (val1 < val2 && val3 > val4 &&
                        (((val1 - val1 * 0.1) <= val4) && ((val1 + val1 * 0.1) >= val4))) {
                    System.out.println("Muskelausschlag nach oben");
                    //System.out.println(zyklusArrayInput); //Analyse für Fehlerbehebung

                    //Setzen der Ersten Werte für die CSV und Merkmale in den Speicher
                    merkmalSpeicher.setMinMaxValues(val1, val2, val3, val4);

                    // Starte die Polynomiale Approximation der Steigung mit den Werten aus der Peak Normalisierung
                    startePolynomialeApproximationAnfang(val1, val2);

                    // Starte die Polynomiale Approximation dem Mittelpunktwerten zwischen Steigung und Senkung aus der Peak Normalisierung
                    startePolynomialeApproximationMitte(val2, val3);

                    // Starte die Polynomiale Approximation der Senkung mit den Werten aus der Peak Normalisierung
                    startePolynomialeApproximationEnde(val3, val4);

                    // Starte die Polynomiale Approximation des gesamten Zyklus mit den Werten aus der Peak Normalisierung
                    startePolynomialeApproximationGesamterZyklus(val1, val4);

                    //Starte die Merkmalsextraktion.Merkmalsextraktion.FastFourierTransformation
                    starteFFT(val1, val4);

                } else if (val1 > val2 && val3 > val4) {
                    //System.out.println("Konstante Muskelaktivität nach unten");
                    //System.out.println(val1 + " " + val2 + " " + val3 + " " + val4);
                } else if (val1 > val2 && val3 < val4) {
                    //System.out.println("Muskelausschlag nach unten");     //Wird nicht verwendet, da ein Muskelausschlag nur nach oben geht
                } else if (val1 < val2 && val3 < val4) {
                    //System.out.println("Konstante Muskelaktivität nach oben");
                } else {
                    //System.out.println("Keine der Bedingungen erfüllt.");
                }
            }
        }*/
        zyklusArrayWertErgebnisSeizeOld = zyklusArrayWertErgebnis.size();
    }

    private void startePolynomialeApproximationAnfang(double value1, double value2) {
        //System.out.println("Steigungsformel");
        // Starte die Polynomiale Approximation der Steigung mit den Werten aus der Peak Normalisierung
        polynomialeApproximation.setBeginningValue(value1);
        for (int i = zyklusArrayZeitErgebnis.get(zyklusArrayWertErgebnis.size() - 4); i < (zyklusArrayZeitErgebnis.get(zyklusArrayWertErgebnis.size() - 3)) - 1; i++) {
            polynomialeApproximation.setMiddleValues(zyklusArrayInput.get(i + 1));
        }
        polynomialeApproximation.setEndValue(value2);
        polynomialeApproximation.setFormelTyp(0);
        polynomialeApproximation.run();
    }
    private void startePolynomialeApproximationMitte(double value1, double value2) {
        int schleifeAusgelöst = 0;
        //System.out.println("Mittelformel");
        // Starte die Polynomiale Approximation der Steigung mit den Werten aus der Peak Normalisierung
        polynomialeApproximation.setBeginningValue(value1);
        for (int i = zyklusArrayZeitErgebnis.get(zyklusArrayWertErgebnis.size() - 3); i < (zyklusArrayZeitErgebnis.get(zyklusArrayWertErgebnis.size() - 2)) - 1; i++) {
            polynomialeApproximation.setMiddleValues(zyklusArrayInput.get(i));
            schleifeAusgelöst++;
        }
        polynomialeApproximation.setEndValue(value2);
        if (schleifeAusgelöst == 0){
            System.out.println("Mittelformel nicht berechenbar, da nur 2 oder weniger Werte");
        }else {
            polynomialeApproximation.setFormelTyp(1);
            polynomialeApproximation.run();
        }
    }
    private void startePolynomialeApproximationEnde(double value1, double value2) {
        //System.out.println("Senkungsformel");
        // Starte die Polynomiale Approximation der Steigung mit den Werten aus der Peak Normalisierung
        polynomialeApproximation.setBeginningValue(value1);
        for (int i = zyklusArrayZeitErgebnis.get(zyklusArrayWertErgebnis.size() - 2); i < (zyklusArrayZeitErgebnis.get(zyklusArrayWertErgebnis.size() - 1) - 1); i++) {
            polynomialeApproximation.setMiddleValues(zyklusArrayInput.get(i + 1));
        }
        polynomialeApproximation.setEndValue(value2);
        polynomialeApproximation.setFormelTyp(2);
        polynomialeApproximation.run();
    }

    private void startePolynomialeApproximationGesamterZyklus(double value1, double value4) {
        //System.out.println("Formel gesamter Zyklus");
        // Starte die Polynomiale Approximation des gesamten Zyklus mit den Werten aus der Peak Normalisierung
        polynomialeApproximation.setBeginningValue(value1);
        for (int i = zyklusArrayZeitErgebnis.get(zyklusArrayWertErgebnis.size() - 4); i < (zyklusArrayZeitErgebnis.get(zyklusArrayWertErgebnis.size() - 1) - 1); i++) {
            polynomialeApproximation.setMiddleValues(zyklusArrayInput.get(i + 1));
        }
        polynomialeApproximation.setEndValue(value4);
        polynomialeApproximation.setFormelTyp(3);
        polynomialeApproximation.run();
    }


    // Starten der FFT
    private void starteFFT(double value1, double value4){
        //Arraylist für das Senden der fft Werte
        ArrayList<Double> fftInputArrayList = new ArrayList<>();

        //System.out.println("FFT: ");

        fftInputArrayList.add(value1);
        for (int i = zyklusArrayZeitErgebnis.get(zyklusArrayWertErgebnis.size() - 4); i < (zyklusArrayZeitErgebnis.get(zyklusArrayWertErgebnis.size() - 1) - 1); i++) {
            fftInputArrayList.add(zyklusArrayInput.get(i + 1));
        }
        fftInputArrayList.add(value4);

        fastfouriertransformation.setInput(fftInputArrayList);
        fastfouriertransformation.run();
    }


    // Befüllen der Arrays der Klasse
    public void setArraysZyklenerkennung (ArrayList<Double> zyklusArrayWertErgebnis, ArrayList<Integer> zyklusArrayZeitErgebnis, ArrayList<Double> zyklusArrayInput) {
        this.zyklusArrayWertErgebnis = zyklusArrayWertErgebnis;
        this.zyklusArrayZeitErgebnis = zyklusArrayZeitErgebnis;
        this.zyklusArrayInput = zyklusArrayInput;
    }
}