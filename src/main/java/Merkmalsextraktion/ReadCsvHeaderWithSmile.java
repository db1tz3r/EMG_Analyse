package Merkmalsextraktion;

import smile.data.DataFrame;
import smile.io.Read;
import org.apache.commons.csv.CSVFormat;

import java.util.Arrays;

public class ReadCsvHeaderWithSmile {
    public static void main(String[] args) {
        String csvPath = "src/Data/Merkmale.csv";

        try {
            // Lese die CSV-Datei mit Header
            DataFrame data = Read.csv(csvPath, CSVFormat.DEFAULT.withHeader());

            // Header auslesen
            String[] headers = data.names();

            // Header ausgeben
            System.out.println("Header: " + Arrays.toString(headers));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
