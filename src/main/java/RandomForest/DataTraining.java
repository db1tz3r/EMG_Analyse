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
import smile.base.cart.SplitRule;
import smile.validation.metric.Accuracy;

import java.util.*;
import java.util.stream.Collectors;

public class DataTraining {

    public static RandomForest trainRandomForest(String csvPath) {
        try {
            DataFrame data = Read.csv(csvPath, CSVFormat.DEFAULT.withHeader());
            data = convertStringsToDoubleAndReplaceNaN(data);

            String[] klassen = Arrays.stream(data.stringVector("Klasse").toArray())
                    .filter(Objects::nonNull).distinct().toArray(String[]::new);

            NominalScale scale = new NominalScale(klassen);
            int[] target = data.stringVector("Klasse").factorize(scale).toIntArray();

            data = data.drop("Klasse").merge(IntVector.of("class_numeric", target));

            System.out.println("🎯 Klassenzuordnung:");
            for (int i = 0; i < klassen.length; i++) {
                System.out.println(klassen[i] + " → " + i);
            }

            // Modell-Training mit vollständigen Parametern
            RandomForest rf = RandomForest.fit(
                    Formula.lhs("class_numeric"),
                    data,
                    100,                // Bäume
                    2,                  // max Features (mtry)
                    SplitRule.GINI,
                    10,                 // max Tiefe
                    20,                 // max Knoten
                    2,                  // min node size
                    1.0                 // subsample ratio
            );

            // Model Evaluation (Precision, Recall, F1-Score, etc.)
            int[] predictions = rf.predict(data);
            //ModelEvaluator.evaluateModel(target, predictions);

            return rf;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
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
                        converted[i] = 0.0;
                    }
                }

                df = df.drop(col).merge(DoubleVector.of(col, converted));
            }
        }
        return df;
    }
}
