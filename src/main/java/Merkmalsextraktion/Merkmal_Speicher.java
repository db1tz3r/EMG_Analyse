package Merkmalsextraktion;

import java.io.*;
import java.nio.file.*;
import java.util.*;

public class Merkmal_Speicher {

    // Merkmals-Speicher
    private double minimumSteigungWert, maximumSteigungWert, minimumSenkungWert, maximumSenkungWert
            , steigungA, steigungB, steigungC, senkungA, senkungB, senkungC, mittelA, mittelB, mittelC, gesamtA, gesamtB, gesamtC
            , fftRealTeil, fftImgTeil;

    //Array mit Merkmale (erster Wert, Wert MaximumSteigung, letzter Wert, Wert MaximumSenkung,
    //                      Steigungsformel a, Steigungsformel b, Steigungsformel c,
    //                      Senkungsformel a, Senkungsformel b, Senkungsformel c,
    //                      Mittelformel a, Mittelformel b, Mittelformel c,
    //                      Gesamtformel a, Gesamtformel b, Gesamtformel c,
    //                      FFT RealTeil, FFT ImgTeil,
    private String csvFileName;
    private boolean createCsvFile;

    //Konstruktor
    public Merkmal_Speicher(String csvFileName, boolean createCsvFile) {
        this.csvFileName = csvFileName;
        this.createCsvFile = createCsvFile;
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
    public void setFFTValues(Double realTeil, Double imgTeil){
        fftRealTeil = realTeil;
        fftImgTeil = imgTeil;
        // Befüllung der csv-Datei
        if (createCsvFile) {
            createCSVFile(new String[]{String.valueOf(minimumSteigungWert), String.valueOf(maximumSteigungWert), String.valueOf(minimumSenkungWert), String.valueOf(maximumSenkungWert),
                    String.valueOf(steigungA), String.valueOf(steigungB), String.valueOf(steigungC),
                    String.valueOf(senkungA), String.valueOf(senkungB), String.valueOf(senkungC),
                    String.valueOf(mittelA), String.valueOf(mittelB), String.valueOf(mittelC),
                    String.valueOf(gesamtA), String.valueOf(gesamtB), String.valueOf(gesamtC),
                    String.valueOf(fftRealTeil), String.valueOf(fftImgTeil)}, csvFileName);
        }
    }

    // Erstellen und befüllen der csv-Datei
    public void createCSVFile(String[] rowData, String baseFileName){
        // Erstellen der csv-Datei
        try {
            // Finde den nächsten verfügbaren Dateinamen
            String fileName = getNextFileName(baseFileName);

            // Überprüfe, ob die Datei existiert
            boolean fileExists = Files.exists(Paths.get(fileName));

            // Datei im Anhängemodus öffnen
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileName, true))) {
                // Falls die Datei neu ist, füge die Header hinzu
                if (!fileExists) {
                    writer.write("minimumSteigungWert,maximumSteigungWert,minimumSenkungWert,maximumSenkungWert," +
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
