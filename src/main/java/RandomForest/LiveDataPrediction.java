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

                // 🔧 Nur ein Element → splitten
                String[] parts = rawInstance.get(0).split(",");

                // 🔄 In double[] umwandeln
                double[] liveInstance = Arrays.stream(parts)
                        .mapToDouble(s -> {
                            try {
                                return Double.parseDouble(s.trim().replace(",", ".")); // falls Kommas dezimaltrennend
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
                System.out.println("📢 Vorhersage: Finger " + (prediction+1));


                // Feature Importance
                System.out.println("🧭 Feature Importance: " + Arrays.toString(rf.importance()));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
