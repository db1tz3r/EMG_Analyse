package RandomForest;

import org.apache.commons.csv.CSVFormat;
import smile.classification.RandomForest;
import smile.data.Tuple;
import smile.data.type.DataTypes;
import smile.data.type.StructField;
import smile.data.type.StructType;
import org.apache.commons.csv.CSVParser;


import java.io.IOException;
import java.util.ArrayList;
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

                // Beispiel: Datentypen basierend auf Headernamen bestimmen (anpassbar)
                if (header.toLowerCase().contains("class")) {
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
                double[] liveInstance = (double[]) liveDataQueue.take(); // Hole die nächste Instanz aus der Queue

                // Konvertiere das double[] in ein Tuple
                Tuple tupleInstance = Tuple.of(liveInstance, schema);

                // Vorhersage machen
                int prediction = rf.predict(tupleInstance);

                // Vorhersage ausgeben
                System.out.println("Vorhersage für die eingehenden Daten: " + prediction);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Simuliert eingehende Live-Daten.
     *
     * @param liveDataQueue Die Queue, in die die simulierten Daten eingefügt werden.
     */
    public static void simulateLiveData(ArrayBlockingQueue<Object> liveDataQueue) {
        new Thread(() -> {
            try {
                for (int i = 0; i < 10; i++) {
                    // Simuliere eingehende Daten
                    double[] newInstance = generateRandomValues(); // Beispiel-Merkmale
                    // Ausgabe der simulierten Live-Daten
                    //System.out.println("Simulierte Live-Daten: " + newInstance[0] + ", " + newInstance[1] + ", " + newInstance[2] + ", " + newInstance[3]);
                    // Füge die simulierten Daten in die Queue ein
                    liveDataQueue.put(newInstance); // Füge Daten in die Queue ein
                    Thread.sleep(1000); // 1 Sekunde Verzögerung
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }

    //Random Werte zwischen 0 und 7 generieren
    public static double[] generateRandomValues() {
        double[] randomValues = new double[17];
        for (int i = 0; i < 17; i++) {
            randomValues[i] = Math.random() * 1024;
        }
        return randomValues;
    }
}