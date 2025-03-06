package Merkmalsextraktion;

import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;
import Sensormanagement.Manager;

public class Merkmalsextraktion_Manager implements Runnable {

    private int zyklusArrayWertErgebnisSeizeOld = 0;
    private Merkmal_Speicher merkmalSpeicher;
    private int merkmalsSpeicherID;
    private Manager manager;

    private ArrayList<Double> rawData = new ArrayList<>();  //Speicher für die Rohdaten aus dem Imput für Fast Fourier Transformation
    private ArrayList<Double> zyklusArrayWertErgebnis = new ArrayList<Double>(); //Speicher für die Werte der Zykluserkennung
    private ArrayList<Integer> zyklusArrayZeitErgebnis = new ArrayList<Integer>();  //Speicher für die Zeitwerte der Zykluserkennung
    private ArrayList<Double> zyklusArrayInput = new ArrayList<Double>();

    public Merkmalsextraktion_Manager(Merkmal_Speicher merkmalSpeicher, Manager manager, int merkmalsSpeicherID) {
        this.merkmalSpeicher = merkmalSpeicher;
        this.merkmalsSpeicherID = merkmalsSpeicherID;
        this.manager = manager;
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

                    // CountdownLatch für die 8 Threads
                    CountDownLatch latch = new CountDownLatch(9); // 1 Klassische + 4 Polynomial + 4 FFT

                    // Starte Thread für Klassische Merkmale
                    new Thread(() -> {
                        KlassischeSignalMerkmale thread = starteKlassischeSignalMerkmale(startSteigung, endeSenkung, merkmalSpeicher);
                        thread.start();
                        latch.countDown();
                    }).start();

                    // Starte Threads für polynomiale Approximation
                    new Thread(() -> {
                        PolynomialeApproximation thread = startePolynomialeApproximationAnfang(startSteigung, endeSteigung, 0);
                        thread.start();
                        latch.countDown();
                    }).start();

                    new Thread(() -> {
                        PolynomialeApproximation thread = startePolynomialeApproximationMitte(endeSteigung, startSenkung, 1);
                        thread.start();
                        latch.countDown();
                    }).start();

                    new Thread(() -> {
                        PolynomialeApproximation thread = startePolynomialeApproximationEnde(startSenkung, endeSenkung, 2);
                        thread.start();
                        latch.countDown();
                    }).start();

                    new Thread(() -> {
                        PolynomialeApproximation thread = startePolynomialeApproximationGesamterZyklus(startSteigung, endeSenkung, 3);
                        thread.start();
                        latch.countDown();
                    }).start();

                    // Starte FFT-Threads
                    new Thread(() -> {
                        FastFourierTransformation thread = starteFFTAnfang(startSteigung, endeSteigung, 0);
                        thread.start(); // Wichtig: Starten!
                        latch.countDown();
                    }).start();

                    new Thread(() -> {
                        FastFourierTransformation thread = starteFFTMitte(endeSteigung, startSenkung, 1);
                        thread.start(); // Wichtig: Starten!
                        latch.countDown();
                    }).start();

                    new Thread(() -> {
                        FastFourierTransformation thread = starteFFTEnde(startSenkung, endeSenkung, 2);
                        thread.start(); // Wichtig: Starten!
                        latch.countDown();
                    }).start();

                    new Thread(() -> {
                        FastFourierTransformation thread = starteFFTGesamt(startSteigung, endeSenkung, 3);
                        thread.start(); // Wichtig: Starten!
                        latch.countDown();
                    }).start();

                    // Warten, bis alle Threads fertig sind
                    try {
                        latch.await();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    // Jetzt sind alle Threads fertig --> Füge alle Instanzen global hinzu
                    manager.setFeaturesFromInstanz(merkmalsSpeicherID);

                } else {
                    //System.out.println("Unvollständiger Zyklus oder Rauschen erkannt.");
                }
            }
        }
    }

    private PolynomialeApproximation startePolynomialeApproximationAnfang(double value1, double value2, int formelTyp) {
        // Erstelle ein neues Objekt der Klasse PolynomialeApproximation für die Threadverwaltung
        PolynomialeApproximation polynomialeApproximation = new PolynomialeApproximation(merkmalSpeicher);

        //System.out.println("Steigungsformel");
        // Starte die Polynomiale Approximation der Steigung mit den Werten aus der Peak Normalisierung
        polynomialeApproximation.setBeginningValue(value1);
        for (int i = zyklusArrayZeitErgebnis.get(zyklusArrayWertErgebnis.size() - 4); i < (zyklusArrayZeitErgebnis.get(zyklusArrayWertErgebnis.size() - 3)) - 1; i++) {
            //System.out.println(zyklusArrayInput.get(i + 1));
            polynomialeApproximation.setMiddleValues(zyklusArrayInput.get(i + 1));
        }
        polynomialeApproximation.setEndValue(value2);
        polynomialeApproximation.setFormelTyp(formelTyp);
        return polynomialeApproximation;
    }

    private PolynomialeApproximation startePolynomialeApproximationMitte(double value1, double value2, int formelTyp) {
        // Erstelle ein neues Objekt der Klasse PolynomialeApproximation für die Threadverwaltung
        PolynomialeApproximation polynomialeApproximation = new PolynomialeApproximation(merkmalSpeicher);

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
            polynomialeApproximation.setFormelTyp(formelTyp);
            return polynomialeApproximation;
        }
        return polynomialeApproximation;
    }

    private PolynomialeApproximation startePolynomialeApproximationEnde(double value1, double value2, int formelTyp) {
        // Erstelle ein neues Objekt der Klasse PolynomialeApproximation für die Threadverwaltung
        PolynomialeApproximation polynomialeApproximation = new PolynomialeApproximation(merkmalSpeicher);

        //System.out.println("Senkungsformel");
        // Starte die Polynomiale Approximation der Steigung mit den Werten aus der Peak Normalisierung
        polynomialeApproximation.setBeginningValue(value1);
        for (int i = zyklusArrayZeitErgebnis.get(zyklusArrayWertErgebnis.size() - 2); i < (zyklusArrayZeitErgebnis.get(zyklusArrayWertErgebnis.size() - 1) - 1); i++) {
            polynomialeApproximation.setMiddleValues(zyklusArrayInput.get(i + 1));
        }
        polynomialeApproximation.setEndValue(value2);
        polynomialeApproximation.setFormelTyp(formelTyp);
        return polynomialeApproximation;
    }

    private PolynomialeApproximation startePolynomialeApproximationGesamterZyklus(double value1, double value4, int formelTyp) {
        // Erstelle ein neues Objekt der Klasse PolynomialeApproximation für die Threadverwaltung
        PolynomialeApproximation polynomialeApproximation = new PolynomialeApproximation(merkmalSpeicher);

        //System.out.println("Formel gesamter Zyklus");
        // Starte die Polynomiale Approximation des gesamten Zyklus mit den Werten aus der Peak Normalisierung
        polynomialeApproximation.setBeginningValue(value1);
        for (int i = zyklusArrayZeitErgebnis.get(zyklusArrayWertErgebnis.size() - 4); i < (zyklusArrayZeitErgebnis.get(zyklusArrayWertErgebnis.size() - 1) - 1); i++) {
            polynomialeApproximation.setMiddleValues(zyklusArrayInput.get(i + 1));
        }
        polynomialeApproximation.setEndValue(value4);
        polynomialeApproximation.setFormelTyp(formelTyp);
        return polynomialeApproximation;
    }


    // Starten der FFT
    private  FastFourierTransformation starteFFTAnfang(double value1, double value4, int formelTyp) {
        // Erstelle ein neues Objekt der Klasse FastFourierTransformation für die Threadverwaltung
        FastFourierTransformation fftThread = new FastFourierTransformation(merkmalSpeicher);

        //Arraylist für das Senden der fft Werte
        ArrayList<Double> fftInputArrayList = new ArrayList<>();

        //System.out.println("FFT: ");

        fftInputArrayList.add(value1);
        for (int i = zyklusArrayZeitErgebnis.get(zyklusArrayWertErgebnis.size() - 4); i < (zyklusArrayZeitErgebnis.get(zyklusArrayWertErgebnis.size() - 3)) - 1; i++) {
            fftInputArrayList.add(rawData.get(i + 1));
            //System.out.println(rawData.get(i + 1));
        }
        fftInputArrayList.add(value4);

        fftThread.setFormelTyp(formelTyp);
        fftThread.setInput(fftInputArrayList);

        return fftThread;
    }

    private FastFourierTransformation starteFFTMitte(double value1, double value4, int formelTyp) {
        // Objekt der Klasse FastFourierTransformation für die Threadverwaltung
        FastFourierTransformation fftThread = new FastFourierTransformation(merkmalSpeicher);

        //Arraylist für das Senden der fft Werte
        ArrayList<Double> fftInputArrayList = new ArrayList<>();

        //System.out.println("FFT: ");

        fftInputArrayList.add(value1);
        for (int i = zyklusArrayZeitErgebnis.get(zyklusArrayWertErgebnis.size() - 3); i < (zyklusArrayZeitErgebnis.get(zyklusArrayWertErgebnis.size() - 2)) - 1; i++) {
            fftInputArrayList.add(rawData.get(i + 1));
            //System.out.println(rawData.get(i + 1));
        }
        fftInputArrayList.add(value4);

        fftThread.setFormelTyp(formelTyp);
        fftThread.setInput(fftInputArrayList);

        return fftThread;
    }

    private FastFourierTransformation starteFFTEnde(double value1, double value4, int formelTyp) {
        // Objekt der Klasse FastFourierTransformation für die Threadverwaltung
        FastFourierTransformation fftThread = new FastFourierTransformation(merkmalSpeicher);

        //Arraylist für das Senden der fft Werte
        ArrayList<Double> fftInputArrayList = new ArrayList<>();

        //System.out.println("FFT: ");

        fftInputArrayList.add(value1);
        for (int i = zyklusArrayZeitErgebnis.get(zyklusArrayWertErgebnis.size() - 3); i < (zyklusArrayZeitErgebnis.get(zyklusArrayWertErgebnis.size() - 2)) - 1; i++) {
            fftInputArrayList.add(rawData.get(i + 1));
            //System.out.println(rawData.get(i + 1));
        }
        fftInputArrayList.add(value4);

        fftThread.setFormelTyp(formelTyp);
        fftThread.setInput(fftInputArrayList);

        return fftThread;
    }

    private FastFourierTransformation starteFFTGesamt(double value1, double value4, int formelTyp) {
        // Objekt der Klasse FastFourierTransformation für die Threadverwaltung
        FastFourierTransformation fftThread = new FastFourierTransformation(merkmalSpeicher);

        //Arraylist für das Senden der fft Werte
        ArrayList<Double> fftInputArrayList = new ArrayList<>();

        //System.out.println("FFT: ");

        fftInputArrayList.add(value1);
        for (int i = zyklusArrayZeitErgebnis.get(zyklusArrayWertErgebnis.size() - 4); i < (zyklusArrayZeitErgebnis.get(zyklusArrayWertErgebnis.size() - 1) - 1); i++) {
            fftInputArrayList.add(rawData.get(i + 1));
            //System.out.println(rawData.get(i + 1));
        }
        fftInputArrayList.add(value4);

        fftThread.setFormelTyp(formelTyp);
        fftThread.setInput(fftInputArrayList);

        return fftThread;
    }

    // Starten der Klassischen Signalmerkmale
    private KlassischeSignalMerkmale starteKlassischeSignalMerkmale(double value1, double value4, Merkmal_Speicher merkmalSpeicher) {
        //Arraylist
        ArrayList<Double> gerichtetesSignal = new ArrayList<>();

        gerichtetesSignal.add(value1);
        for (int i = zyklusArrayZeitErgebnis.get(zyklusArrayWertErgebnis.size() - 4); i < (zyklusArrayZeitErgebnis.get(zyklusArrayWertErgebnis.size() - 1) - 1); i++) {
            gerichtetesSignal.add(zyklusArrayInput.get(i + 1));
            //System.out.println(rawData.get(i + 1));
        }
        gerichtetesSignal.add(value4);

        ArrayList<Double> rohSignal = new ArrayList<>();

        rohSignal.add(value1);
        for (int i = zyklusArrayZeitErgebnis.get(zyklusArrayWertErgebnis.size() - 4); i < (zyklusArrayZeitErgebnis.get(zyklusArrayWertErgebnis.size() - 1) - 1); i++) {
            rohSignal.add(rawData.get(i + 1));
            //System.out.println(rawData.get(i + 1));
        }
        rohSignal.add(value4);


        // Erstelle ein neues Objekt der Klasse KlassischeSignalMerkmale für die Threadverwaltung
        KlassischeSignalMerkmale klassischeSignalMerkmale = new KlassischeSignalMerkmale(rohSignal, gerichtetesSignal, merkmalSpeicher);

        return klassischeSignalMerkmale;
    }


    // Setter und Getter
    // Befüllen der Arrays der Klasse
    public void setArraysZyklenerkennung (ArrayList<Double> zyklusArrayWertErgebnis, ArrayList<Integer> zyklusArrayZeitErgebnis,
                                          ArrayList<Double> zyklusArrayInput, ArrayList<Double> rawData){
        this.zyklusArrayWertErgebnis = zyklusArrayWertErgebnis;
        this.zyklusArrayZeitErgebnis = zyklusArrayZeitErgebnis;
        this.zyklusArrayInput = zyklusArrayInput;
        this.rawData = rawData;
    }
}