package Normalisierung;

public class PeakNormalisierung {

    private double max;

    public PeakNormalisierung(double max) {
        this.max = max;
    }

    // Methode zur Peak-Normalisierung
    public double[] normalizePeak(double[] values) {
        // Normalisiere jedes Element im Array
        double[] normalizedValues = new double[values.length];
        for (int i = 0; i < values.length; i++) {
            normalizedValues[i] = (values[i] / max)*1000;
        }

        return normalizedValues;
    }
}