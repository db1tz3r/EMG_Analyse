package Merkmalsextraktion;

import Jama.Matrix;

import java.util.ArrayList;

public class PolynomialeApproximation extends Thread {

    private static ArrayList<Double> yValues = new ArrayList<>();
    private static ArrayList<Double> coefficientsArrayList = new ArrayList<>();
    private int formelTyp = 0;

    private Merkmal_Speicher merkmalSpeicher;

    public PolynomialeApproximation(Merkmal_Speicher merkmalSpeicher) {
        this.merkmalSpeicher = merkmalSpeicher;
    }

    public void run() {
        int n = yValues.size();

        if (n == 0) {
            System.err.println("Keine Werte vorhanden für die polynomiale Approximation.");
            return;
        }

        // Erstelle die x-Werte
        double[] xValues = new double[n];
        for (int i = 0; i < n; i++) {
            xValues[i] = i;
        }

        // Grad des Polynoms
        int degree = 2;

        // Erstelle die Designmatrix
        double[][] X = new double[n][degree + 1];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j <= degree; j++) {
                X[i][j] = Math.pow(xValues[i], j);
            }
        }

        // Erstelle die y-Werte als Vektor
        double[][] Y = new double[n][1];
        for (int i = 0; i < n; i++) {
            Y[i][0] = yValues.get(i);
        }

        Matrix matrixX = new Matrix(X);
        Matrix matrixY = new Matrix(Y);

        try {
            // Berechne (X^T * X) + λI
            Matrix Xt = matrixX.transpose();
            Matrix XtX = Xt.times(matrixX);

            // Regularisierung hinzufügen (λ = 1e-8)
            double lambda = 1e-8;
            for (int i = 0; i < XtX.getRowDimension(); i++) {
                XtX.set(i, i, XtX.get(i, i) + lambda);
            }

            // Lösen des Systems
            Matrix XtX_inv = XtX.inverse();
            Matrix XtY = Xt.times(matrixY);
            Matrix B = XtX_inv.times(XtY);

            // Extrahiere die Koeffizienten
            coefficientsArrayList.clear();
            for (int i = 0; i <= degree; i++) {
                coefficientsArrayList.add(B.get(i, 0));
            }
        } catch (Exception e) {
            System.err.println("Fehler beim Lösen des Gleichungssystems. Verwende Pseudoinverse: " + e.getMessage());

            // Fallback auf Pseudoinverse
            try {
                Matrix pseudoInverse = matrixX.transpose().times(matrixX).inverse().times(matrixX.transpose());
                Matrix B = pseudoInverse.times(matrixY);

                coefficientsArrayList.clear();
                for (int i = 0; i <= degree; i++) {
                    coefficientsArrayList.add(B.get(i, 0));
                }
            } catch (Exception ex) {
                System.err.println("Pseudoinverse konnte nicht berechnet werden: " + ex.getMessage());
                return;
            }
        }

        // Speichern der Koeffizienten
        double[] coefficients = new double[coefficientsArrayList.size()];
        for (int i = 0; i < coefficientsArrayList.size(); i++) {
            coefficients[i] = coefficientsArrayList.get(i);
        }
        merkmalSpeicher.setPolynomialeApproximation(coefficients[0], coefficients[1], coefficients[2], formelTyp);
    }

    public void setMiddleValues(double value) {
        yValues.add(value);
    }

    public void setBeginningValue(double value1) {
        yValues.clear();
        yValues.add(value1);
    }

    public void setEndValue(double value2) {
        yValues.add(value2);
    }

    public void setFormelTyp(int formelTyp) {
        this.formelTyp = formelTyp;
    }
}
