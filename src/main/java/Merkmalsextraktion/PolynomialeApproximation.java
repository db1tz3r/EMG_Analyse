package Merkmalsextraktion;//Polynomiale Approximation starten
//    public void startPolynomialeApproximation(){
//        Merkmalsextraktion.PolynomialeApproximation polynomialeApproximation = new Merkmalsextraktion.PolynomialeApproximation();
//        double[] bestCoefficients = polynomialeApproximation.polynomialApproximation(rmsArrayValuesErgebnis, 5);
//        // Ausgabe der besten Polynomformel
//        System.out.print("Bestes Polynom: f(x) = ");
//        for (int i = 0; i < bestCoefficients.length; i++) {
//            System.out.printf("%.4f", bestCoefficients[i]);
//            if (i > 0) {
//                System.out.print(" * x^" + i);
//            }
//            if (i < bestCoefficients.length - 1) {
//                System.out.print(" + ");
//            }
//        }
//    }
//Ende Polynomiale Approximation


import Jama.Matrix;

import java.util.ArrayList;

public class PolynomialeApproximation implements Runnable {

    private static ArrayList<Double> yValues = new ArrayList<Double>();
    private static ArrayList<Double> coefficientsArrayList = new ArrayList<Double>();

    public void run() {
        //System.out.println(yValues);  //Analyse für Fehlerfindung
        int n = yValues.size();

        // Erstelle die x-Werte, die automatisch von 0 bis n-1 gehen
        double[] xValues = new double[n];
        for (int i = 0; i < n; i++) {
            xValues[i] = i;
        }

        // Erstelle die Designmatrix für die Methode der kleinsten Quadrate
        // Grad des Polynoms
        int degree = 2;
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

        // Lineares Gleichungssystem lösen
        Matrix matrixX = new Matrix(X);
        Matrix matrixY = new Matrix(Y);

        // Berechne (X^T * X)^-1 * X^T * Y
        Matrix Xt = matrixX.transpose();
        Matrix XtX = Xt.times(matrixX);
        Matrix XtX_inv = XtX.inverse();
        Matrix XtY = Xt.times(matrixY);
        Matrix B = XtX_inv.times(XtY);

        // Extrahiere die Koeffizienten des Polynoms
        coefficientsArrayList.clear();
        for (int i = 0; i <= degree; i++) {
            coefficientsArrayList.add(B.get(i, 0));
        }
        System.out.print("Polynom: f(x) = ");
        for (int i = 0; i < coefficientsArrayList.size(); i++) {
            System.out.printf("%.4f", coefficientsArrayList.get(i));
            if (i > 0) {
                System.out.print(" * x^" + i);
            }
            if (i < coefficientsArrayList.size() - 1) {
                System.out.print(" + ");
            }
        }
        System.out.println();
    }


    public void setMiddleValues(double value){
        yValues.add(value);
    }

    public void setBeginningValue(double value1){
        yValues.clear();
        yValues.add(value1);
    }

    public void setEndValue(double value2){
        yValues.add(value2);
    }
}