package RandomForest;

import smile.classification.RandomForest;
import smile.data.Tuple;
import smile.data.type.DataTypes;
import smile.data.type.StructField;
import smile.data.type.StructType;

import java.util.concurrent.ArrayBlockingQueue;

public class LiveDataPrediction {

    /**
     * Führt Live-Daten-Vorhersagen durch.
     *
     * @param rf            Das trainierte RandomForest-Modell
     * @param liveDataQueue Die Queue mit eingehenden Live-Daten
     */
    public static void predictLiveData(RandomForest rf, ArrayBlockingQueue<Object> liveDataQueue) {
        try {
            // Struktur definieren: gleiche Merkmale wie beim Training
            StructType schema = new StructType(
                    new StructField("feature1", DataTypes.DoubleType),
                    new StructField("feature2", DataTypes.DoubleType),
                    new StructField("feature3", DataTypes.DoubleType),
                    new StructField("feature4", DataTypes.DoubleType),
                    new StructField("class", DataTypes.StringType)
            );

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
        double[] randomValues = new double[4];
        for (int i = 0; i < 4; i++) {
            randomValues[i] = Math.random() * 10;
        }
        return randomValues;
    }
}