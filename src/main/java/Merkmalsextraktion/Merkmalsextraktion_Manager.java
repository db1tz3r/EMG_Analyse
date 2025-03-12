package Merkmalsextraktion;

import Management.InstanzManager;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

public class Merkmalsextraktion_Manager {
    // Objekte
    private Merkmal_Speicher merkmalSpeicher;

    // Variablen
    private List<List<List<Double>>> allFeatures = new ArrayList<>(); // Array für alle Merkmale

    public Merkmalsextraktion_Manager(Merkmal_Speicher merkmalSpeicher) {
        this.merkmalSpeicher = merkmalSpeicher;
    }


    public List<List<List<Double>>> startMerkmalsextraktion(List<List<List>> zyklusErgebnis) {
        List<List<List<Double>>> allFeatures = new ArrayList<>();

        // Starte die Merkmalsextraktion für jede Instanz
        for (int i = 0; i < zyklusErgebnis.size(); i++) {
            int instanzID = i;  // Instanz-ID
            if (!zyklusErgebnis.isEmpty() && zyklusErgebnis.get(instanzID) != null) {
//                System.out.println(zyklusErgebnis);
                merkmalSpeicher = new Merkmal_Speicher();

                // Speichere die Werte im Merkmalspeicher
                merkmalSpeicher.setMinMaxValues(
                        getSteigungsArrayGerichtet(zyklusErgebnis, instanzID).getFirst(),
                        getSteigungsArrayGerichtet(zyklusErgebnis, instanzID).getLast(),
                        getSenkungsArrayGerichtet(zyklusErgebnis, instanzID).getFirst(),
                        getSenkungsArrayGerichtet(zyklusErgebnis, instanzID).getLast()
                );

                // CountdownLatch für die 8 Threads
                CountDownLatch latch = new CountDownLatch(9); // 1 Klassische + 4 Polynomial + 4 FFT

                // Starte Thread für Klassische Merkmale
                new Thread(() -> {
                    KlassischeSignalMerkmale thread = starteKlassischeSignalMerkmale(getGesamtArrayRoh(zyklusErgebnis, instanzID), getGesamtArrayGerichtet(zyklusErgebnis, instanzID), merkmalSpeicher);
                    thread.start();
                    latch.countDown();
                }).start();

                // Starte Threads für polynomiale Approximation
                new Thread(() -> {
                    PolynomialeApproximation thread = startePolynomialeApproximationAnfang(getSteigungsArrayGerichtet(zyklusErgebnis, instanzID), 0);
                    thread.start();
                    latch.countDown();
                }).start();

                new Thread(() -> {
                    PolynomialeApproximation thread = startePolynomialeApproximationMitte(getMittelArrayGerichtet(zyklusErgebnis, instanzID), 1);
                    thread.start();
                    latch.countDown();
                }).start();

                new Thread(() -> {
                    PolynomialeApproximation thread = startePolynomialeApproximationEnde(getSenkungsArrayGerichtet(zyklusErgebnis, instanzID), 2);
                    thread.start();
                    latch.countDown();
                }).start();

                new Thread(() -> {
                    PolynomialeApproximation thread = startePolynomialeApproximationGesamterZyklus(getGesamtArrayGerichtet(zyklusErgebnis, instanzID), 3);
                    thread.start();
                    latch.countDown();
                }).start();

                // Starte FFT-Threads
                new Thread(() -> {
                    FastFourierTransformation thread = starteFFTAnfang(getSteigungsArrayRoh(zyklusErgebnis, instanzID), 0);
                    thread.start(); // Wichtig: Starten!
                    latch.countDown();
                }).start();

                new Thread(() -> {
                    FastFourierTransformation thread = starteFFTMitte(getMittelArrayRoh(zyklusErgebnis, instanzID), 1);
                    thread.start(); // Wichtig: Starten!
                    latch.countDown();
                }).start();

                new Thread(() -> {
                    FastFourierTransformation thread = starteFFTEnde(getSenkungsArrayRoh(zyklusErgebnis, instanzID), 2);
                    thread.start(); // Wichtig: Starten!
                    latch.countDown();
                }).start();

                new Thread(() -> {
                    FastFourierTransformation thread = starteFFTGesamt(getGesamtArrayRoh(zyklusErgebnis, instanzID), 3);
                    thread.start(); // Wichtig: Starten!
                    latch.countDown();
                }).start();

                // Warten, bis alle Threads fertig sind
                try {
                    latch.await();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                // Daten zusammenfügen
                allFeatures.add(merkmalSpeicher.getAlleMerkmale());
//                System.out.println("Merkmale: " + allFeatures);
            } else {
                allFeatures.add(Collections.singletonList(Arrays.asList(new Double[39])));
            }
        }

        // Falls alle Instanzen verarbeitet wurden, gebe das Ergebnis zurück
        if (!allFeatures.isEmpty() && allFeatures.stream().anyMatch(Objects::nonNull)) {
//            System.out.println("Ergebnis: " + allFeatures);
            return allFeatures;
        }

        return null;
    }










    // Methoden der Merkmalsextraktion
    // Starten der Polynomiale Approximation
    private PolynomialeApproximation startePolynomialeApproximationAnfang(ArrayList<Double> zyklusArrayInput, int formelTyp) {
        // Erstelle ein neues Objekt der Klasse PolynomialeApproximation für die Threadverwaltung
        PolynomialeApproximation polynomialeApproximation = new PolynomialeApproximation(merkmalSpeicher);

        polynomialeApproximation.setInputArray(zyklusArrayInput);
        polynomialeApproximation.setFormelTyp(formelTyp);

        return polynomialeApproximation;
    }

    private PolynomialeApproximation startePolynomialeApproximationMitte(ArrayList<Double> zyklusArrayInput, int formelTyp) {
        // Erstelle ein neues Objekt der Klasse PolynomialeApproximation für die Threadverwaltung
        PolynomialeApproximation polynomialeApproximation = new PolynomialeApproximation(merkmalSpeicher);

        int schleifeAusgeloest = 0;
        //System.out.println("Mittelformel");
        // Starte die Polynomiale Approximation der Steigung mit den Werten aus der Peak Normalisierung
        polynomialeApproximation.setInputArray(zyklusArrayInput);
        polynomialeApproximation.setFormelTyp(formelTyp);

        return polynomialeApproximation;
    }

    private PolynomialeApproximation startePolynomialeApproximationEnde(ArrayList<Double> zyklusArrayInput, int formelTyp) {
        // Erstelle ein neues Objekt der Klasse PolynomialeApproximation für die Threadverwaltung
        PolynomialeApproximation polynomialeApproximation = new PolynomialeApproximation(merkmalSpeicher);

        polynomialeApproximation.setInputArray(zyklusArrayInput);
        polynomialeApproximation.setFormelTyp(formelTyp);

        return polynomialeApproximation;
    }

    private PolynomialeApproximation startePolynomialeApproximationGesamterZyklus(ArrayList<Double> zyklusArrayInput, int formelTyp) {
        // Erstelle ein neues Objekt der Klasse PolynomialeApproximation für die Threadverwaltung
        PolynomialeApproximation polynomialeApproximation = new PolynomialeApproximation(merkmalSpeicher);

        polynomialeApproximation.setInputArray(zyklusArrayInput);
        polynomialeApproximation.setFormelTyp(formelTyp);

        return polynomialeApproximation;
    }


    // Starten der FFT
    private  FastFourierTransformation starteFFTAnfang(ArrayList<Double> zyklusArrayInput, int formelTyp) {
        // Erstelle ein neues Objekt der Klasse FastFourierTransformation für die Threadverwaltung
        FastFourierTransformation fftThread = new FastFourierTransformation(merkmalSpeicher);

        fftThread.setFormelTyp(formelTyp);
        fftThread.setInput(zyklusArrayInput);

        return fftThread;
    }

    private FastFourierTransformation starteFFTMitte(ArrayList<Double> zyklusArrayInput, int formelTyp) {
        // Objekt der Klasse FastFourierTransformation für die Threadverwaltung
        FastFourierTransformation fftThread = new FastFourierTransformation(merkmalSpeicher);

        fftThread.setFormelTyp(formelTyp);
        fftThread.setInput(zyklusArrayInput);

        return fftThread;
    }

    private FastFourierTransformation starteFFTEnde(ArrayList<Double> zyklusArrayInput, int formelTyp) {
        // Objekt der Klasse FastFourierTransformation für die Threadverwaltung
        FastFourierTransformation fftThread = new FastFourierTransformation(merkmalSpeicher);

        fftThread.setFormelTyp(formelTyp);
        fftThread.setInput(zyklusArrayInput);

        return fftThread;
    }

    private FastFourierTransformation starteFFTGesamt(ArrayList<Double> zyklusArrayInput, int formelTyp) {
        // Objekt der Klasse FastFourierTransformation für die Threadverwaltung
        FastFourierTransformation fftThread = new FastFourierTransformation(merkmalSpeicher);

        fftThread.setFormelTyp(formelTyp);
        fftThread.setInput(zyklusArrayInput);

        return fftThread;
    }

    // Starten der Klassischen Signalmerkmale
    private KlassischeSignalMerkmale starteKlassischeSignalMerkmale(ArrayList<Double> zyklusArrayInputRoh, ArrayList<Double> zyklusArrayInputGerichtet, Merkmal_Speicher merkmalSpeicher) {
        // Erstelle ein neues Objekt der Klasse KlassischeSignalMerkmale für die Threadverwaltung
        KlassischeSignalMerkmale klassischeSignalMerkmale = new KlassischeSignalMerkmale(zyklusArrayInputRoh, zyklusArrayInputGerichtet, merkmalSpeicher);

        return klassischeSignalMerkmale;
    }







    // Getter und Setter
    // Hauptliste in Steigung, Senkung, Mittel und Gesamt aufteilen
    public ArrayList<Double> getSteigungsArrayGerichtet(List<List<List>> zyklusErgebnis, int instanzID) {
//        System.out.println("SteigungsArrayGerichtet" + zyklusErgebnis.get(instanzID).get(0));
        return (ArrayList<Double>) zyklusErgebnis.get(instanzID).get(0);
    }

    public ArrayList<Double> getMittelArrayGerichtet(List<List<List>> zyklusErgebnis, int instanzID) {
        return (ArrayList<Double>) zyklusErgebnis.get(instanzID).get(3);
    }

    public ArrayList<Double> getSenkungsArrayGerichtet(List<List<List>> zyklusErgebnis, int instanzID) {
        return (ArrayList<Double>) zyklusErgebnis.get(instanzID).get(6);
    }

    public ArrayList<Double> getGesamtArrayGerichtet(List<List<List>> zyklusErgebnis, int instanzID) {
        ArrayList<Double> gesamtArrayGerichtet = new ArrayList<>();

        gesamtArrayGerichtet.addAll((ArrayList<Double>) zyklusErgebnis.get(instanzID).get(0));
        gesamtArrayGerichtet.addAll((ArrayList<Double>) zyklusErgebnis.get(instanzID).get(3));
        gesamtArrayGerichtet.addAll((ArrayList<Double>) zyklusErgebnis.get(instanzID).get(6));

        return gesamtArrayGerichtet;
    }

    public ArrayList<Double> getSteigungsArrayRoh(List<List<List>> zyklusErgebnis, int instanzID) {
        return (ArrayList<Double>) zyklusErgebnis.get(instanzID).get(1);
    }

    public ArrayList<Double> getMittelArrayRoh(List<List<List>> zyklusErgebnis, int instanzID) {
        return (ArrayList<Double>) zyklusErgebnis.get(instanzID).get(4);
    }

    public ArrayList<Double> getSenkungsArrayRoh(List<List<List>> zyklusErgebnis, int instanzID) {
        return (ArrayList<Double>) zyklusErgebnis.get(instanzID).get(7);
    }

    public ArrayList<Double> getGesamtArrayRoh(List<List<List>> zyklusErgebnis, int instanzID) {
        ArrayList<Double> gesamtArrayRoh = new ArrayList<>();

        gesamtArrayRoh.addAll((ArrayList<Double>) zyklusErgebnis.get(instanzID).get(1));
        gesamtArrayRoh.addAll((ArrayList<Double>) zyklusErgebnis.get(instanzID).get(4));
        gesamtArrayRoh.addAll((ArrayList<Double>) zyklusErgebnis.get(instanzID).get(7));

        return gesamtArrayRoh;
    }
}