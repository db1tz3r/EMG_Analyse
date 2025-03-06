package Merkmalsextraktion;

import java.io.*;
import java.nio.file.*;
import java.util.Arrays;
import java.util.concurrent.ArrayBlockingQueue;

public class Merkmal_Speicher {

    // Merkmals-Speicher
    protected double minimumSteigungWert, maximumSteigungWert, minimumSenkungWert, maximumSenkungWert
            , steigungA, steigungB, steigungC
            , senkungA, senkungB, senkungC
            , mittelA, mittelB, mittelC
            , gesamtA, gesamtB, gesamtC
            , fftAnfangMedoan, fftAnfangMittel, fftAnfangLeistungsdichtespektrum
            , fftMitteMedoan, fftMitteMittel, fftMitteLeistungsdichtespektrum
            , fftEndeMedoan, fftEndeMittel, fftEndeLeistungsdichtespektrum
            , fftGesamtMedoan, fftGesamtMittel, fftGesamtLeistungsdichtespektrum
            , mittelwert, signalstärke, varianz
            , standardabweichung, energie, kurtosis
            , schiefe, zeroCrossings, laenge
            , maximalwert, minimalwert;

    //Array mit Merkmale (erster Wert, Wert MaximumSteigung, letzter Wert, Wert MaximumSenkung,
    //                      Steigungsformel a, Steigungsformel b, Steigungsformel c,
    //                      Senkungsformel a, Senkungsformel b, Senkungsformel c,
    //                      Mittelformel a, Mittelformel b, Mittelformel c,
    //                      Gesamtformel a, Gesamtformel b, Gesamtformel c,
    //                      FFT Anfang Medoan, FFT Anfang Mittel, FFT Anfang Leistungsdichtespektrum,
    //                      FFT Mitte Medoan, FFT Mitte Mittel, FFT Mitte Leistungsdichtespektrum,
    //                      FFT Ende Medoan, FFT Ende Mittel, FFT Ende Leistungsdichtespektrum,
    //                      FFT Gesamt Medoan, FFT Gesamt Mittel, FFT Gesamt Leistungsdichtespektrum
    //                      Mittelwert, Signalstärke, Varianz,
    //                      Standardabweichung, Energie, Kurtosis,
    //                      Schiefe, ZeroCrossings, Länge,
    //                      Maximalwert, Minimalwert


    // Set-Methode für die Min- und Max-Werte
    public void setMinMaxValues(double minimumSteigungWert, double maximumSteigungWert, double minimumSenkungWert, double maximumSenkungWert){
        this.minimumSteigungWert = minimumSteigungWert;
        this.maximumSteigungWert = maximumSteigungWert;
        this.maximumSenkungWert = minimumSenkungWert;
        this.minimumSenkungWert = maximumSenkungWert;
    }

    // Set-Methode für die Polynomiale Approximation
    public void setPolynomialeApproximation (double aValue, double bValue, double cValue, int formelTyp){
        if (formelTyp == 0) {
            steigungA = aValue;
            steigungB = bValue;
            steigungC = cValue;
        } else if (formelTyp == 1) {
            mittelA = aValue;
            mittelB = bValue;
            mittelC = cValue;
        } else if (formelTyp == 2) {
            senkungA = aValue;
            senkungB = bValue;
            senkungC = cValue;
        } else if (formelTyp == 3) {
            gesamtA = aValue;
            gesamtB = bValue;
            gesamtC = cValue;
        }
    }

    // Set-Methode für die FFT-Werte und ggf befüllung der csv-Datei
    int markerFftVollstaendigkeit = 0;
    public void setFFTValues(double[] fftValues, int formeltyp) {
        // Befüllung der FFT-Werte
        if (formeltyp == 0) {
            fftAnfangMedoan = fftValues[0];
            fftAnfangMittel = fftValues[1];
            fftAnfangLeistungsdichtespektrum = fftValues[2];
        } else if (formeltyp == 1) {
            fftMitteMedoan = fftValues[0];
            fftMitteMittel = fftValues[1];
            fftMitteLeistungsdichtespektrum = fftValues[2];
        } else if (formeltyp == 2) {
            fftEndeMedoan = fftValues[0];
            fftEndeMittel = fftValues[1];
            fftEndeLeistungsdichtespektrum = fftValues[2];
        } else if (formeltyp == 3) {
            fftGesamtMedoan = fftValues[0];
            fftGesamtMittel = fftValues[1];
            fftGesamtLeistungsdichtespektrum = fftValues[2];
        }

    }

    // Set-Methoden für Klassische Merkmale
    public void setMittelwert(double mittelwert) {
        this.mittelwert = mittelwert;
    }

    public void setSignalstärke(double signalstärke) {
        this.signalstärke = signalstärke;
    }

    public void setVarianz(double varianz) {
        this.varianz = varianz;
    }

    public void setStandardabweichung(double standardabweichung) {
        this.standardabweichung = standardabweichung;
    }

    public void setEnergie(double energie) {
        this.energie = energie;
    }

    public void setKurtosis(double kurtosis) {
        this.kurtosis = kurtosis;
    }

    public void setSchiefe(double schiefe) {
        this.schiefe = schiefe;
    }

    public void setZeroCrossings(int zeroCrossings) {
        this.zeroCrossings = zeroCrossings;
    }

    public void setLaenge(int laenge) {
        this.laenge = laenge;
    }

    public void setMaximalwert(double maximalwert) {
        this.maximalwert = maximalwert;
    }

    public void setMinimalwert(double minimalwert) {
        this.minimalwert = minimalwert;
    }

    // Getter Methode zum holen der einzelnen Werte für den gemeinsamen Speicher
    public String getAlleMerkmale() {
        return minimumSenkungWert + "," + maximumSenkungWert + "," + minimumSteigungWert + "," + maximumSteigungWert + ","
                + steigungA + "," + steigungB + "," + steigungC + ","
                + senkungA + "," + senkungB + "," + senkungC + ","
                + mittelA + "," + mittelB + "," + mittelC + ","
                + gesamtA + "," + gesamtB + "," + gesamtC + ","
                + fftAnfangMedoan + "," + fftAnfangMittel + "," + fftAnfangLeistungsdichtespektrum + ","
                + fftMitteMedoan + "," + fftMitteMittel + "," + fftMitteLeistungsdichtespektrum + ","
                + fftEndeMedoan + "," + fftEndeMittel + "," + fftEndeLeistungsdichtespektrum + ","
                + fftGesamtMedoan + "," + fftGesamtMittel + "," + fftGesamtLeistungsdichtespektrum + ","
                + mittelwert + "," + signalstärke + "," + varianz + ","
                + standardabweichung + "," + energie + "," + kurtosis + ","
                + schiefe + "," + zeroCrossings + "," + laenge + ","
                + maximalwert + "," + minimalwert;
    }










}
