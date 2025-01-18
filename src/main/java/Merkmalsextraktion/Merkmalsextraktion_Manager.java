package Merkmalsextraktion;

import java.util.ArrayList;

public class Merkmalsextraktion_Manager implements Runnable {

    private int zyklusArrayWertErgebnisSeizeOld = 0;
    private PolynomialeApproximation polynomialeApproximation;
    private FastFourierTransformation fastfouriertransformation;
    private Merkmal_Speicher merkmalSpeicher;

    private ArrayList<Double> rawData = new ArrayList<>();  //Speicher für die Rohdaten aus dem Imput für Fast Fourier Transformation
    private ArrayList<Double> zyklusArrayWertErgebnis = new ArrayList<Double>(); //Speicher für die Werte der Zykluserkennung
    private ArrayList<Integer> zyklusArrayZeitErgebnis = new ArrayList<Integer>();  //Speicher für die Zeitwerte der Zykluserkennung
    private ArrayList<Double> zyklusArrayInput = new ArrayList<Double>();

    public Merkmalsextraktion_Manager(PolynomialeApproximation polynomialeApproximation, FastFourierTransformation fft, Merkmal_Speicher merkmalSpeicher) {
        this.polynomialeApproximation = polynomialeApproximation;
        this.fastfouriertransformation = fft;
        this.merkmalSpeicher = merkmalSpeicher;
    }



    @Override
    public void run() {

        if (zyklusArrayWertErgebnis.size() >= 4 && (zyklusArrayWertErgebnis.size() != zyklusArrayWertErgebnisSeizeOld)) {
            zyklusArrayWertErgebnisSeizeOld = zyklusArrayWertErgebnis.size();
            if (zyklusArrayWertErgebnis.size() % 4 == 0) {
                // Extrahiere die letzten vier Werte
                double startSteigung = zyklusArrayWertErgebnis.get(zyklusArrayWertErgebnis.size() - 4);
                double endeSteigung = zyklusArrayWertErgebnis.get(zyklusArrayWertErgebnis.size() - 3);
                double startSenkung = zyklusArrayWertErgebnis.get(zyklusArrayWertErgebnis.size() - 2);
                double endeSenkung = zyklusArrayWertErgebnis.get(zyklusArrayWertErgebnis.size() - 1);

                // Überprüfe, ob die Werte gültige Bedingungen für einen Zyklus erfüllen (Zweite Sicherheitsprüfung)
                if (endeSteigung > startSteigung && endeSenkung < startSenkung) {
                    System.out.println("Kompletter Muskelzyklus erkannt");
                    System.out.printf("Start Steigung: %.2f, Ende Steigung: %.2f, Start Senkung: %.2f, Ende Senkung: %.2f%n",
                            startSteigung, endeSteigung, startSenkung, endeSenkung);

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
                    //System.out.println("Unvollständiger Zyklus oder Rauschen erkannt.");
                }
            }
        }
    }

    private void startePolynomialeApproximationAnfang(double value1, double value2) {
        //System.out.println("Steigungsformel");
        // Starte die Polynomiale Approximation der Steigung mit den Werten aus der Peak Normalisierung
        polynomialeApproximation.setBeginningValue(value1);
        for (int i = zyklusArrayZeitErgebnis.get(zyklusArrayWertErgebnis.size() - 4); i < (zyklusArrayZeitErgebnis.get(zyklusArrayWertErgebnis.size() - 3)) - 1; i++) {
            //System.out.println(zyklusArrayInput.get(i + 1));
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
            fftInputArrayList.add(rawData.get(i + 1));
            //System.out.println(rawData.get(i + 1));
        }
        fftInputArrayList.add(value4);

        fastfouriertransformation.setInput(fftInputArrayList);
        fastfouriertransformation.run();
    }


    // Befüllen der Arrays der Klasse
    public void setArraysZyklenerkennung (ArrayList<Double> zyklusArrayWertErgebnis, ArrayList<Integer> zyklusArrayZeitErgebnis,
                                          ArrayList<Double> zyklusArrayInput, ArrayList<Double> rawData){
        this.zyklusArrayWertErgebnis = zyklusArrayWertErgebnis;
        this.zyklusArrayZeitErgebnis = zyklusArrayZeitErgebnis;
        this.zyklusArrayInput = zyklusArrayInput;
        this.rawData = rawData;
    }
}