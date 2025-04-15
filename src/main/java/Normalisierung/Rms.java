package Normalisierung;

public class Rms {

    public double rmsCalculation(double[] rmsArrayValuesInput){
        double sumOfSquares = 0.0;


        // Quadriere jeden Wert und addiere ihn zur Summe
        for (double value : rmsArrayValuesInput) {
            sumOfSquares += Math.pow(value, 2);
        }

        // Berechne den Mittelwert der quadrierten Werte
        double meanOfSquares = sumOfSquares / rmsArrayValuesInput.length;

        // Ziehe die Quadratwurzel des Mittelwerts
        //System.out.println(Math.sqrt(meanOfSquares));
        return Math.sqrt(meanOfSquares);
    }
}

R