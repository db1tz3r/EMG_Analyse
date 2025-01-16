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
            , steigungA, steigungB, steigungC, senkungA, senkungB, senkungC, mittelA, mittelB, mittelC, gesamtA, gesamtB, gesamtC;

    //Array mit Merkmale (erster Wert, Wert MaximumSteigung, letzter Wert, Wert MaximumSenkung,
    //                      Steigungsformel a, Steigungsformel b, Steigungsformel c,
    //                      Senkungsformel a, Senkungsformel b, Senkungsformel c,
    //                      Mittelformel a, Mittelformel b, Mittelformel c,
    //                      Gesamtformel a, Gesamtformel b, Gesamtformel c,
    //                      FFT wird direkt bei Aufruf übergeben,
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


    // Set-Methode für die Min- und Max-Werte
    public void setMinMaxValues(double minimumSteigungWert, double maximumSteigungWert, double minimumSenkungWert, double maximumSenkungWert){
        this.minimumSteigungWert = minimumSteigungWert;
        this.maximumSteigungWert = maximumSteigungWert;
        this.maximumSenkungWert = minimumSenkungWert;
        this.minimumSenkungWert = maximumSenkungWert;
    }

    // Set-Methode für die Polynomiale Approximation
    public void setPolynomialeApproximation (Double aValue, Double bValue, Double cValue, Integer formelTyp){
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
    public void setFFTValues(double[] fftValues) throws InterruptedException {
        // Befüllung der csv-Datei oder senden der Live Daten
        if (createCsvFile) {
            createCSVFile(new String[]{"Finger1", String.valueOf(minimumSteigungWert), String.valueOf(maximumSteigungWert), String.valueOf(minimumSenkungWert), String.valueOf(maximumSenkungWert),
                    String.valueOf(steigungA), String.valueOf(steigungB), String.valueOf(steigungC),
                    String.valueOf(senkungA), String.valueOf(senkungB), String.valueOf(senkungC),
                    String.valueOf(mittelA), String.valueOf(mittelB), String.valueOf(mittelC),
                    String.valueOf(gesamtA), String.valueOf(gesamtB), String.valueOf(gesamtC),
                    String.join(",", Arrays.stream(fftValues).mapToObj(String::valueOf).toArray(String[]::new))});
        } else {
            double[] combinedArray = DoubleStream.concat(
                    DoubleStream.of(Double.NaN, maximumSteigungWert, minimumSenkungWert, maximumSenkungWert,
                            steigungA, steigungB, steigungC, senkungA, senkungB, senkungC, mittelA, mittelB, mittelC,
                            gesamtA, gesamtB, gesamtC),
                    DoubleStream.of(fftValues)
            ).toArray();

// In die Queue einfügen
            liveDataQueue.put(combinedArray);
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
                            "fftRealTeil,fftImgTeil"); // Header der CSV
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
