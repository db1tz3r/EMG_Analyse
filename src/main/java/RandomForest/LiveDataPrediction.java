package RandomForest;

import smile.classification.RandomForest;
import smile.data.Tuple;
import smile.data.type.DataTypes;
import smile.data.type.StructField;
import smile.data.type.StructType;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;

import java.io.FileReader;
import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;

public class LiveDataPrediction {

    // Methode zur Vorhersage von Live-Daten mit einem RandomForest-Modell
    public static void predictLiveData(RandomForest rf, ArrayBlockingQueue<Object> liveDataQueue, String csvPath) {
        try {
            // Liest die CSV-Datei und erstellt einen Parser
            CSVParser parser = CSVParser.parse(new FileReader(csvPath), CSVFormat.DEFAULT.withHeader());
            List<String> headers = new ArrayList<>(parser.getHeaderMap().keySet());

            // Erstellt das Schema basierend auf den Headern der CSV-Datei
            List<StructField> fields = new ArrayList<>();
            for (String header : headers) {
                if (!header.equalsIgnoreCase("Klasse")) { // Ignoriert die "Klasse"-Spalte
                    fields.add(new StructField(header, DataTypes.DoubleType));
                }
            }

            StructType schema = new StructType(fields.toArray(new StructField[0]));

            // Endlosschleife zur Verarbeitung von Live-Daten
            while (true) {
                @SuppressWarnings("unchecked")
                List<String> rawInstance = (List<String>) liveDataQueue.take(); // Holt die nächste Instanz aus der Warteschlange
//                System.out.println("Rohdaten: " + rawInstance);

                // Verarbeitet die Rohdaten und splittet sie in einzelne Werte
                List<String> processedRawInstance = new ArrayList<>();
                for (String data : rawInstance) {
                    String[] parts = data.split(",");
                    processedRawInstance.addAll(Arrays.asList(parts));
                }

                // Konvertiert die verarbeiteten Daten in ein double-Array
                double[] liveInstance = processedRawInstance.stream()
                        .mapToDouble(s -> {
                            try {
                                // Ersetzt Komma durch Punkt und konvertiert in double
                                return Double.parseDouble(s.trim().replace(",", "."));
                            } catch (Exception e) {
                                System.err.println("Fehler beim Parsen: '" + s + "' → " + e.getMessage());
                                return 0.0; // Setzt fehlerhafte Werte auf 0.0
                            }
                        })
                        .toArray();

                // Überprüft, ob die Länge der Live-Daten mit dem Schema übereinstimmt
                if (liveInstance.length != fields.size()) {
                    System.out.println("Länge der Live-Daten stimmt nicht mit Schema überein: " +
                            liveInstance.length + " vs. " + fields.size());
                    liveInstance = Arrays.copyOf(liveInstance, fields.size()); // Passt die Länge an
                }

                // Erstellt ein Tuple basierend auf den Live-Daten und dem Schema
                Tuple tupleInstance = Tuple.of(liveInstance, schema);
                int prediction = rf.predict(tupleInstance); // Führt die Vorhersage durch

                // Gibt die Live-Daten und die Vorhersage aus
//                System.out.println("Live-Daten:");
//                System.out.println(Arrays.toString(liveInstance));
                System.out.println("Vorhersage: Finger " + (prediction + 1));

                // Gibt die Feature-Wichtigkeit aus
//                System.out.println("Feature Importance: " + Arrays.toString(rf.importance()));
            }

        } catch (Exception e) {
            // Gibt den Fehler aus, falls eine Ausnahme auftritt
            e.printStackTrace();
        }
    }
}