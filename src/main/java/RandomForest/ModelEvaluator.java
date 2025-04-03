package RandomForest;

import smile.validation.metric.*;
import java.util.Arrays;

public class ModelEvaluator {

    public static void evaluateModel(int[] yTrue, int[] yPred) {
        // Accuracy
        double accuracy = Accuracy.of(yTrue, yPred);
        System.out.println("🎯 Accuracy: " + accuracy);

        // Precision, Recall, F1-Score (F-Measure)
        double precision = Precision.of(yTrue, yPred);  // Klasse 1 als Beispiel
        double recall = Recall.of(yTrue, yPred);
        double f1Score = calculateF1Score(precision, recall);  // F-Measure
        System.out.println("Precision: " + precision);
        System.out.println("Recall: " + recall);
        System.out.println("F1-Score (F-Measure): " + f1Score);

        // Kappa
        double kappa = calculateKappa(yTrue, yPred);
        System.out.println("Kappa: " + kappa);

        // Confusion Matrix
        int[] cm = confusionMatrix(yTrue, yPred);
        System.out.println("📊 Confusion Matrix:");
        System.out.println("True Negatives: " + cm[0]);
        System.out.println("False Positives: " + cm[1]);
        System.out.println("False Negatives: " + cm[2]);
        System.out.println("True Positives: " + cm[3]);
    }

    // F1-Score berechnen
    private static double calculateF1Score(double precision, double recall) {
        if (precision + recall == 0) return 0.0;
        return 2 * (precision * recall) / (precision + recall);
    }

    // Kappa berechnen
    private static double calculateKappa(int[] yTrue, int[] yPred) {
        int[] cm = confusionMatrix(yTrue, yPred);
        int total = yTrue.length;
        double p_o = (cm[0] + cm[3]) / (double) total; // Beobachtete Übereinstimmung
        double p_e = ((cm[0] + cm[1]) * (cm[0] + cm[2]) + (cm[2] + cm[3]) * (cm[1] + cm[3])) / (double) (total * total); // Erwartete Übereinstimmung
        return (p_o - p_e) / (1 - p_e);
    }

    // Confusion Matrix berechnen
    private static int[] confusionMatrix(int[] yTrue, int[] yPred) {
        // Für binäre Klassifikation: True Negative, False Positive, False Negative, True Positive
        int[] cm = new int[4]; // 4 Werte für die Confusion Matrix
        for (int i = 0; i < yTrue.length; i++) {
            if (yTrue[i] == 0 && yPred[i] == 0) cm[0]++;  // True Negative
            if (yTrue[i] == 0 && yPred[i] == 1) cm[1]++;  // False Positive
            if (yTrue[i] == 1 && yPred[i] == 0) cm[2]++;  // False Negative
            if (yTrue[i] == 1 && yPred[i] == 1) cm[3]++;  // True Positive
        }
        return cm;
    }
}
