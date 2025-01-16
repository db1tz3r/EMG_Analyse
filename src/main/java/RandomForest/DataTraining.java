package RandomForest;

import smile.data.DataFrame;
import smile.data.formula.Formula;
import smile.data.measure.NominalScale;
import smile.data.vector.IntVector;
import smile.data.vector.StringVector;
import smile.io.Read;
import org.apache.commons.csv.CSVFormat;
import smile.classification.RandomForest;

import java.util.Properties;

public class DataTraining {

    public static RandomForest trainRandomForest(String csvPath) {
        try {
            // Load CSV file
            DataFrame data = Read.csv(csvPath, CSVFormat.DEFAULT.withHeader());
            System.out.println("Data loaded:");
            System.out.println(data.structure());

            // Force the column "class" to be a StringVector
            data = data.merge(StringVector.of("class_fixed", data.column("Klasse").toStringArray()));

            // Create a NominalScale for the target variable
            NominalScale scale = new NominalScale("Finger1", "Finger2");

            // Convert the target variable `class_fixed` to numeric values
            data = data.merge(IntVector.of("class_numeric", data.stringVector("class_fixed").factorize(scale).toIntArray()));

            // Update DataFrame: Remove old string target variables
            data = data.drop("Klasse").drop("class_fixed");
            System.out.println("After conversion:");
            System.out.println(data.structure());

            // Define target variable
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
}