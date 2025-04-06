package Management;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class CreateCSV {

    // Variablen
    private final String fileName;
    private boolean createCsvFile;
    private int counter;

    // Konstruktor
    public CreateCSV(String csvFileName, boolean createCsvFile) {
        this.fileName = getNextFileName(csvFileName) + ".csv"; // Erstelle einen neuen Dateinamen
        this.createCsvFile = createCsvFile;
    }


    // Erstellen und befüllen der csv-Datei
    public void createCSVFile(List<String> input, int anzahlSensoren) {
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
                            "fftGesamtMedian,fftGesamtMittel,fftGesamtPSD," +
                            "mittelwert,signalstärke,varianz," +
                            "standardabweichung,energie,kurtosis," +
                            "schiefe,zeroCrossings,laenge," +
                            "maximalwert,minimalwert"); // Header der CSV
                    if (anzahlSensoren > 1){
                        for (int i = 1; i < anzahlSensoren; i++) {
                            writer.write(",minimumSteigungWert,maximumSteigungWert,minimumSenkungWert,maximumSenkungWert," +
                                    "steigungA,steigungB,steigungC," +
                                    "senkungA,senkungB,senkungC," +
                                    "mittelA,mittelB,mittelC," +
                                    "gesamtA,gesamtB,gesamtC," +
                                    "fftAnfangMedian,fftAnfangMittel,fftAnfangPSD," +
                                    "fftMitteMedian,fftMitteMittel,fftMittePSD," +
                                    "fftEndeMedian,fftEndeMittel,fftEndePSD," +
                                    "fftGesamtMedian,fftGesamtMittel,fftGesamtPSD," +
                                    "mittelwert,signalstärke,varianz," +
                                    "standardabweichung,energie,kurtosis," +
                                    "schiefe,zeroCrossings,laenge," +
                                    "maximalwert,minimalwert"); // Header der CSV
                        }
                        writer.newLine();
                    } else {
                        writer.newLine();
                    }
                }

                // Schreibe die neue Zeile
                writer.write("Finger1," + String.join(",", input));
                writer.newLine();

                System.out.println("Daten in Datei geschrieben: " + fileName);
                System.out.println("Anzahl der geschriebenen Daten: " + (counter + 1));
                counter++;
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
            fileName = baseFileName + (counter == 0 ? "" : "_" + counter);
            counter++;
        } while (Files.exists(Paths.get(fileName + ".csv")));
        return fileName;
    }

}
