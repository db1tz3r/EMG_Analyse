package Merkmalsextraktion;

import java.util.List;

public class KlassischeSignalMerkmale extends Thread {
    private List<Double> rohSignal;
    private List<Double> gerichtetesSignal;
    private Merkmal_Speicher merkmalSpeicher;
    private double mittelwert;
    private double signalstärke;
    private int laenge;
    private double maximalwert;
    private double minimalwert;
    private double varianz;
    private double standardabweichung;
    private double energie;
    private double kurtosis;
    private double schiefe;
    private int zeroCrossings;

    public KlassischeSignalMerkmale(List<Double> rohSignal, List<Double> gerichtetesSignal, Merkmal_Speicher merkmalSpeicher) {
        this.rohSignal = rohSignal;
        this.gerichtetesSignal = gerichtetesSignal;
        this.merkmalSpeicher = merkmalSpeicher;
    }

    @Override
    public void run() {
        // Ausführen der Merkmalsextraktion
        mittelwert = berechneMittelwert(rohSignal);
        signalstärke = berechneSignalstärke(gerichtetesSignal);
        laenge = berechneLaenge(gerichtetesSignal);
        maximalwert = berechneMaximalwert(rohSignal);
        minimalwert = berechneMinimalwert(rohSignal);
        varianz = berechneVarianz(rohSignal);
        standardabweichung = berechneStandardabweichung(rohSignal);
        energie = berechneEnergie(gerichtetesSignal);
        kurtosis = berechneKurtosis(rohSignal);
        schiefe = berechneSchiefe(rohSignal);
        zeroCrossings = berechneZeroCrossings(rohSignal);

        // Speichern der Ringfinger.csv
        merkmalSpeicher.setMittelwert(mittelwert);
        merkmalSpeicher.setSignalstärke(signalstärke);
        merkmalSpeicher.setLaenge(laenge);
        merkmalSpeicher.setMaximalwert(maximalwert);
        merkmalSpeicher.setMinimalwert(minimalwert);
        merkmalSpeicher.setVarianz(varianz);
        merkmalSpeicher.setStandardabweichung(standardabweichung);
        merkmalSpeicher.setEnergie(energie);
        merkmalSpeicher.setKurtosis(kurtosis);
        merkmalSpeicher.setSchiefe(schiefe);
        merkmalSpeicher.setZeroCrossings(zeroCrossings);
    }

    // Mittelwert des Signals berechnen
    public static double berechneMittelwert(List<Double> signal) {
        if (signal.isEmpty()) return 0;
        double summe = 0;
        for (double wert : signal) {
            summe += wert;
        }
        return summe / signal.size();
    }

    // Signalstärke berechnen (Summe der absoluten Werte)
    public static double berechneSignalstärke(List<Double> signal) {
        double summe = 0;
        for (double wert : signal) {
            summe += Math.abs(wert);
        }
        return summe;
    }

    // Länge des Signals (Anzahl der Datenpunkte)
    public static int berechneLaenge(List<Double> signal) {
        return signal.size();
    }

    // Maximalwert des Signals berechnen
    public static double berechneMaximalwert(List<Double> signal) {
        if (signal.isEmpty()) return Double.NEGATIVE_INFINITY;
        double max = signal.get(0);
        for (double wert : signal) {
            if (wert > max) max = wert;
        }
        return max;
    }

    // Minimalwert des Signals berechnen
    public static double berechneMinimalwert(List<Double> signal) {
        if (signal.isEmpty()) return Double.POSITIVE_INFINITY;
        double min = signal.get(0);
        for (double wert : signal) {
            if (wert < min) min = wert;
        }
        return min;
    }

    // Varianz des Signals berechnen
    public static double berechneVarianz(List<Double> signal) {
        if (signal.size() < 2) return 0;
        double mittelwert = berechneMittelwert(signal);
        double summe = 0;
        for (double wert : signal) {
            summe += Math.pow(wert - mittelwert, 2);
        }
        return summe / (signal.size() - 1);
    }

    // Standardabweichung berechnen
    public static double berechneStandardabweichung(List<Double> signal) {
        return Math.sqrt(berechneVarianz(signal));
    }

    // Energie des Signals berechnen (Summe der quadrierten Werte)
    public static double berechneEnergie(List<Double> signal) {
        double summe = 0;
        for (double wert : signal) {
            summe += Math.pow(wert, 2);
        }
        return summe;
    }

    // Kurtosis berechnen (Maß für Spitzen der Verteilung)
    public static double berechneKurtosis(List<Double> signal) {
        if (signal.size() < 2) return 0;
        double mittelwert = berechneMittelwert(signal);
        double standardabweichung = berechneStandardabweichung(signal);
        double summe = 0;
        for (double wert : signal) {
            summe += Math.pow((wert - mittelwert) / standardabweichung, 4);
        }
        return (summe / signal.size()) - 3;  // Excess Kurtosis
    }

    // Schiefe berechnen (Maß für Asymmetrie der Verteilung)
    public static double berechneSchiefe(List<Double> signal) {
        if (signal.size() < 2) return 0;
        double mittelwert = berechneMittelwert(signal);
        double standardabweichung = berechneStandardabweichung(signal);
        double summe = 0;
        for (double wert : signal) {
            summe += Math.pow((wert - mittelwert) / standardabweichung, 3);
        }
        return summe / signal.size();
    }

    // Zero Crossing Rate berechnen (Anzahl der Nulldurchgänge im Signal)
    public static int berechneZeroCrossings(List<Double> signal) {
        if (signal.size() < 2) return 0;
        int zeroCrossings = 0;
        for (int i = 1; i < signal.size(); i++) {
            if ((signal.get(i - 1) >= 0 && signal.get(i) < 0) || (signal.get(i - 1) < 0 && signal.get(i) >= 0)) {
                zeroCrossings++;
            }
        }
        return zeroCrossings;
    }
}

