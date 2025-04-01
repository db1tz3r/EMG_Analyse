package RandomForest;

import org.apache.commons.csv.CSVFormat;
import smile.classification.RandomForest;
import smile.data.Tuple;
import smile.data.type.DataTypes;
import smile.data.type.StructField;
import smile.data.type.StructType;
import org.apache.commons.csv.CSVParser;


import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.io.FileReader;

public class LiveDataPrediction {

    /**
     * Führt Live-Daten-Vorhersagen durch.
     *
     * @param rf            Das trainierte RandomForest-Modell
     * @param liveDataQueue Die Queue mit eingehenden Live-Daten
     */


    public static void predictLiveData(RandomForest rf, ArrayBlockingQueue<Object> liveDataQueue, String csvPath) {
        try {
            // Start: Struktur automatisch aus dem Header auslesen
            // Lese den Header der CSV-Datei
            CSVParser parser = CSVParser.parse(new FileReader(csvPath), CSVFormat.DEFAULT.withHeader());
            List<String> headers = new ArrayList<>(parser.getHeaderMap().keySet());

            // Liste der StructFields basierend auf den Headern
            List<StructField> fields = new ArrayList<>();
            for (String header : headers) {
                StructField field;

                // Header-Typ bestimmen: "Klasse" als String, Rest als Double
                if ("Klasse".equals(header)) {
                    field = new StructField(header, DataTypes.StringType); // Klassenname als String
                } else {
                    field = new StructField(header, DataTypes.DoubleType); // Features als Double
                }

                fields.add(field);
            }

            // Erstelle das StructType-Schema
            StructType schema = new StructType(fields.toArray(new StructField[0]));
            // Ende: Struktur aus dem Header auslesen

            // Vorhersagen für live eingehende Daten
            while (true) {
                @SuppressWarnings("unchecked")
                List<String> rawInstance = (List<String>) liveDataQueue.take();
                System.out.println("🔍 Live-Daten:");
                System.out.println(rawInstance);


                double[] liveInstance = rawInstance.stream()
                        .mapToDouble(s -> {
                            try {
                                return Double.parseDouble(s.trim().replace(",", "."));
                            } catch (Exception e) {
                                return 0.0; // fallback für ungültige Werte
                            }
                        })
                        .toArray();

                //System.out.println(Arrays.toString(liveInstance));

                int i = 0;
                while (liveInstance.length != fields.size()) {
                    if (liveInstance.length < fields.size()) {
                        // Neues Array erstellen, wenn liveInstance zu klein ist
                        double[] temp = new double[fields.size()];
                        System.arraycopy(liveInstance, 0, temp, 0, liveInstance.length);
                        for (int j = liveInstance.length; j < temp.length; j++) {
                            temp[j] = 0; // Fülle die fehlenden Werte mit 0
                        }
                        liveInstance = temp; // Ersetze das ursprüngliche Array
                    } else {
                        // Neues Array erstellen, wenn liveInstance zu groß ist
                        double[] temp = new double[fields.size()];
                        System.arraycopy(liveInstance, 0, temp, 0, fields.size());
                        liveInstance = temp; // Ersetze das ursprüngliche Array
                    }
                }

                // Konvertiere das double[] in ein Tuple
                Tuple tupleInstance = Tuple.of(liveInstance, schema);

                // Vorhersage machen
                int prediction = rf.predict(tupleInstance);

                // Vorhersage ausgeben
                System.out.println("Vorhersage für die eingehenden Daten: " + prediction);

                int numClasses = rf.numClasses();
                int[] votes = new int[numClasses];

// Gehe durch alle Bäume und zähle die Vorhersagen
                for (var tree : rf.trees()) {
                    int predictionP = tree.predict(tupleInstance);
                    votes[predictionP]++;
                }

// Normalisiere zu Wahrscheinlichkeiten
                double[] probs = new double[numClasses];
                for (int j = 0; j < numClasses; j++) {
                    probs[j] = (double) votes[j] / rf.trees().length;
                }

                System.out.println("🔢 Geschätzte Wahrscheinlichkeiten: " + Arrays.toString(probs));

                System.out.println("Starkes Feature: " + Arrays.toString(rf.importance()));



            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}