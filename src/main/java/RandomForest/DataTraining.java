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

    // Methode zum Trainieren eines RandomForest-Modells basierend auf einer CSV-Datei
    public static RandomForest trainRandomForest(String csvPath) {
        try {
            // Liest die CSV-Datei und erstellt ein DataFrame
            DataFrame data = Read.csv(csvPath, CSVFormat.DEFAULT.withHeader());
            // Konvertiert String-Werte in Double-Werte und ersetzt NaN-Werte
            data = convertStringsToDoubleAndReplaceNaN(data);

            // Extrahiert die Klassen aus der Spalte "Klasse" und erstellt eine Nominalskala
            String[] klassen = Arrays.stream(data.stringVector("Klasse").toArray())
                    .filter(Objects::nonNull).distinct().toArray(String[]::new);

            NominalScale scale = new NominalScale(klassen);
            // Konvertiert die Klassen in numerische Werte
            int[] target = data.stringVector("Klasse").factorize(scale).toIntArray();

            // Entfernt die ursprüngliche "Klasse"-Spalte und fügt die numerische Klasse hinzu
            data = data.drop("Klasse").merge(IntVector.of("class_numeric", target));

            // Gibt die Klassenzuordnung aus
            System.out.println("🎯 Klassenzuordnung:");
            for (int i = 0; i < klassen.length; i++) {
                System.out.println(klassen[i] + " → " + i);
            }

            // Trainiert das RandomForest-Modell mit den angegebenen Parametern
            RandomForest rf = RandomForest.fit(
                    Formula.lhs("class_numeric"), // Zielvariable
                    data,                         // Trainingsdaten
                    100,                          // Anzahl der Bäume
                    2,                            // Maximale Anzahl der Features (mtry)
                    SplitRule.GINI,               // Split-Regel (Gini-Index)
                    10,                           // Maximale Tiefe der Bäume
                    20,                           // Maximale Anzahl der Knoten
                    2,                            // Minimale Knotengröße
                    1.0                           // Subsample-Verhältnis
            );
            return rf; // Gibt das trainierte Modell zurück
        } catch (Exception e) {
            // Gibt den Fehler aus, falls eine Ausnahme auftritt
            e.printStackTrace();
            return null;
        }
    }

    // Methode zur Konvertierung von String-Werten in Double-Werte und zum Ersetzen von NaN-Werten
    public static DataFrame convertStringsToDoubleAndReplaceNaN(DataFrame df) {
        for (String col : df.names()) {
            // Überprüft, ob die Spalte vom Typ String ist und nicht "Klasse" heißt
            if (df.column(col).type() == DataTypes.StringType && !col.equalsIgnoreCase("Klasse")) {
                String[] values = df.stringVector(col).toArray();
                double[] converted = new double[values.length];

                // Konvertiert jeden String-Wert in einen Double-Wert
                for (int i = 0; i < values.length; i++) {
                    try {
                        converted[i] = Double.parseDouble(values[i].trim().replace(",", "."));
                    } catch (Exception e) {
                        // Setzt den Wert auf 0.0, falls die Konvertierung fehlschlägt
                        converted[i] = 0.0;
                    }
                }

                // Ersetzt die ursprüngliche Spalte durch die konvertierte Double-Spalte
                df = df.drop(col).merge(DoubleVector.of(col, converted));
            }
        }
        return df; // Gibt das aktualisierte DataFrame zurück
    }
}