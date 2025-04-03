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

    public static void predictLiveData(RandomForest rf, ArrayBlockingQueue<Object> liveDataQueue, String csvPath) {
        try {
            CSVParser parser = CSVParser.parse(new FileReader(csvPath), CSVFormat.DEFAULT.withHeader());
            List<String> headers = new ArrayList<>(parser.getHeaderMap().keySet());

            List<StructField> fields = new ArrayList<>();
            for (String header : headers) {
                if (!header.equalsIgnoreCase("Klasse")) {
                    fields.add(new StructField(header, DataTypes.DoubleType));
                }
            }

            StructType schema = new StructType(fields.toArray(new StructField[0]));

            while (true) {
                @SuppressWarnings("unchecked")
                List<String> rawInstance = (List<String>) liveDataQueue.take();
                System.out.println("📥 Rohdaten: " + rawInstance);

                // 🔧 Jetzt alle Elemente in rawInstance betrachten und aufsplitten
                // Die Elemente in `rawInstance` sind Strings, daher müssen wir jedes dieser Elemente splitten
                List<String> processedRawInstance = new ArrayList<>();
                for (String data : rawInstance) {
                    String[] parts = data.split(",");
                    processedRawInstance.addAll(Arrays.asList(parts));
                }

                // 🔄 Umwandlung in double[]
                double[] liveInstance = processedRawInstance.stream()
                        .mapToDouble(s -> {
                            try {
                                // Bei Bedarf Komma durch Punkt ersetzen und in double konvertieren
                                return Double.parseDouble(s.trim().replace(",", "."));
                            } catch (Exception e) {
                                System.err.println("⚠️ Fehler beim Parsen: '" + s + "' → " + e.getMessage());
                                return 0.0;
                            }
                        })
                        .toArray();

                if (liveInstance.length != fields.size()) {
                    System.out.println("⚠️ Länge der Live-Daten stimmt nicht mit Schema überein: " +
                            liveInstance.length + " vs. " + fields.size());
                    liveInstance = Arrays.copyOf(liveInstance, fields.size());
                }

                Tuple tupleInstance = Tuple.of(liveInstance, schema);
                int prediction = rf.predict(tupleInstance);

                System.out.println("🔍 Live-Daten:");
                System.out.println(Arrays.toString(liveInstance));
                System.out.println("📢 Vorhersage: Finger " + (prediction + 1));

                // Feature Importance
                System.out.println("🧭 Feature Importance: " + Arrays.toString(rf.importance()));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
