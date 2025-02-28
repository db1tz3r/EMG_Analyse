package Merkmalsextraktion;

import RandomForest.LiveDataPrediction;
import smile.regression.RandomForest;

import java.io.*;
import java.lang.reflect.Array;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.stream.DoubleStream;

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
            , fftGesamtMedoan, fftGesamtMittel, fftGesamtLeistungsdichtespektrum;

    //Array mit Merkmale (erster Wert, Wert MaximumSteigung, letzter Wert, Wert MaximumSenkung,
    //                      Steigungsformel a, Steigungsformel b, Steigungsformel c,
    //                      Senkungsformel a, Senkungsformel b, Senkungsformel c,
    //                      Mittelformel a, Mittelformel b, Mittelformel c,
    //                      Gesamtformel a, Gesamtformel b, Gesamtformel c,
    //                      FFT Anfang Medoan, FFT Anfang Mittel, FFT Anfang Leistungsdichtespektrum,
    //                      FFT Mitte Medoan, FFT Mitte Mittel, FFT Mitte Leistungsdichtespektrum,
    //                      FFT Ende Medoan, FFT Ende Mittel, FFT Ende Leistungsdichtespektrum,
    //                      FFT Gesamt Medoan, FFT Gesamt Mittel, FFT Gesamt Leistungsdichtespektrum
    private boolean createCsvFile;
    // Weitere Variablen
    private String fileName;
    private final ArrayBlockingQueue<Object> liveDataQueue;

    //Konstruktor
    public Merkmal_Speicher(String csvFileName, boolean createCsvFile, ArrayBlockingQueue<Object> liveDataQueue) {
        this.createCsvFile = createCsvFile;
        this.fileName = getNextFileName(csvFileName);
        this.liveDataQueue = liveDataQueue;
    }

    // Methode zum Starten der Klassifikation oder der CSV-Datei
    public void startCSVOrKlassification() throws InterruptedException {
        if (createCsvFile){
            // Erstellen der CSV-Datei
            createCSVFile(new String[]{
                    "1", String.valueOf(minimumSteigungWert), String.valueOf(maximumSteigungWert), String.valueOf(minimumSenkungWert), String.valueOf(maximumSenkungWert),
                    String.valueOf(steigungA), String.valueOf(steigungB), String.valueOf(steigungC),
                    String.valueOf(senkungA), String.valueOf(senkungB), String.valueOf(senkungC),
                    String.valueOf(mittelA), String.valueOf(mittelB), String.valueOf(mittelC),
                    String.valueOf(gesamtA), String.valueOf(gesamtB), String.valueOf(gesamtC),
                    String.valueOf(fftAnfangMedoan), String.valueOf(fftAnfangMittel), String.valueOf(fftAnfangLeistungsdichtespektrum),
                    String.valueOf(fftMitteMedoan), String.valueOf(fftMitteMittel), String.valueOf(fftMitteLeistungsdichtespektrum),
                    String.valueOf(fftEndeMedoan), String.valueOf(fftEndeMittel), String.valueOf(fftEndeLeistungsdichtespektrum),
                    String.valueOf(fftGesamtMedoan), String.valueOf(fftGesamtMittel), String.valueOf(fftGesamtLeistungsdichtespektrum)
            });
        }else {
            // Starte die Klassifikation
            double[] combinedArray = {Double.NaN, maximumSteigungWert, minimumSenkungWert, maximumSenkungWert,
                            steigungA, steigungB, steigungC,
                            senkungA, senkungB, senkungC,
                            mittelA, mittelB, mittelC,
                            gesamtA, gesamtB, gesamtC,
                            fftAnfangMedoan, fftAnfangMittel, fftAnfangLeistungsdichtespektrum,
                            fftMitteMedoan, fftMitteMittel, fftMitteLeistungsdichtespektrum,
                            fftEndeMedoan, fftEndeMittel, fftEndeLeistungsdichtespektrum,
                            fftGesamtMedoan, fftGesamtMittel, fftGesamtLeistungsdichtespektrum};

            // In die Queue einfügen
            liveDataQueue.put(combinedArray);
        }
    }

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

    // Erstellen und befüllen der csv-Datei
    public void createCSVFile(String[] rowData){
        // Erstellen der csv-Datei
        try {
            // Überprüfe, ob die Datei existiert
            boolean fileExists = Files.exists(Paths.get(fileName));

            // Datei im Anhängemodus öffnen
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileName, true))) {
                // Falls die Datei neu ist, füge die Header hinzu
                if (!fileExists) {
                    writer.write("Klasse,minimumSteigungWert,maximumSteigungWert,minimumSenkungWert,maximumSenkungWert," +
                            "steigungA,steigungB,steigungC," +
                            "senkungA,senkungB,senkungC," +
                            "mittelA,mittelB,mittelC," +
                            "gesamtA,gesamtB,gesamtC," +
                            "fftAnfangMedian,fftAnfangMittel,fftAnfangPSD," +
                            "fftMitteMedian,fftMitteMittel,fftMittePSD," +
                            "fftEndeMedian,fftEndeMittel,fftEndePSD," +
                            "fftGesamtMedian,fftGesamtMittel,fftGesamtPSD"); // Header der CSV
                    writer.newLine();
                }

                // Schreibe die neue Zeile
                writer.write(String.join(",", rowData));
                writer.newLine();

                System.out.println("Daten in Datei geschrieben: " + fileName);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Methode, um den nächsten verfügbaren Dateinamend der csv zu finden
    private static String getNextFileName(String baseFileName) {
        int counter = 0;
        String fileName;
        do {
            fileName = baseFileName + (counter == 0 ? "" : "_" + counter) + ".csv";
            counter++;
        } while (Files.exists(Paths.get(fileName)));
        return fileName;
    }

}
