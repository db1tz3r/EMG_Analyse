package RandomForest;

import smile.data.DataFrame;
import smile.data.formula.Formula;
import smile.data.measure.NominalScale;
import smile.data.type.DataTypes;
import smile.data.vector.DoubleVector;
import smile.data.vector.IntVector;
import smile.data.vector.StringVector;
import smile.io.Read;
import org.apache.commons.csv.CSVFormat;
import smile.classification.RandomForest;

import java.util.*;
import java.util.stream.Collectors;

public class DataTraining {

    public static RandomForest trainRandomForest(String csvPath) {
        try {
            DataFrame data = Read.csv(csvPath, CSVFormat.DEFAULT.withHeader());

            // String → Double + Null zu 0.0
            data = convertStringsToDoubleAndReplaceNaN(data);

            // Klasse umwandeln
            String[] klassen = Arrays.stream(data.stringVector("Klasse").toArray())
                    .filter(Objects::nonNull).distinct().toArray(String[]::new);

            NominalScale scale = new NominalScale(klassen);
            int[] target = data.stringVector("Klasse").factorize(scale).toIntArray();

            System.out.println("🎯 Klassenzuordnung (Label → Index):");
            for (int i = 0; i < klassen.length; i++) {
                System.out.println(klassen[i] + " → " + i);
            }

            System.out.println("📦 Target-Vektor Beispiel:");
            System.out.println(Arrays.toString(Arrays.copyOf(target, 20))); // erste 20 anzeigen

            Map<Integer, Long> counts = Arrays.stream(target)
                    .boxed()
                    .collect(Collectors.groupingBy(i -> i, Collectors.counting()));

            System.out.println("📊 Klassenverteilung im Training (numeric):");
            counts.forEach((k, v) -> System.out.println("Klasse " + k + ": " + v));



            data = data.drop("Klasse").merge(IntVector.of("class_numeric", target));

            // Modelltraining
            Formula formula = Formula.lhs("class_numeric");


            // Create properties and set the number of trees
            Properties properties = new Properties();
            properties.setProperty("smile.random.forest.trees", "100");

            // Train Random Forest model
            RandomForest rf = RandomForest.fit(formula, data, properties);
            System.out.println("Model trained!");
            return rf; // Return the trained model

        } catch (Exception e) {
            e.printStackTrace();
            return null; // Return null if training fails
        }
    }

    public static DataFrame convertStringsToDoubleAndReplaceNaN(DataFrame df) {
        for (String col : df.names()) {
            if (df.column(col).type() == DataTypes.StringType && !col.equalsIgnoreCase("Klasse")) {
                String[] values = df.stringVector(col).toArray();
                double[] converted = new double[values.length];

                for (int i = 0; i < values.length; i++) {
                    try {
                        converted[i] = Double.parseDouble(values[i].trim().replace(",", "."));
                    } catch (Exception e) {
                        converted[i] = 0.0; // ➤ hier wird auch gleich ersetzt!
                    }
                }

                df = df.drop(col).merge(DoubleVector.of(col, converted));
            }
        }

        return df;
    }

}