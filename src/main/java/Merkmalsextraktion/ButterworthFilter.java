package Merkmalsextraktion;

public class ButterworthFilter {

    public static double[] applyButterworthFilter(double[] signal, double sampleRate, double lowCut, double highCut, int order) {
        int n = signal.length;
        double[] filteredSignal = new double[n];

        // Normalize frequencies
        double nyquist = sampleRate / 2.0;
        double low = lowCut / nyquist;
        double high = highCut / nyquist;

        // Create Butterworth coefficients (low-pass and high-pass)
        double[] bLow = createButterworthCoefficients(low, order, true);
        double[] bHigh = createButterworthCoefficients(high, order, false);

        // Apply filter to the signal
        for (int i = 0; i < n; i++) {
            filteredSignal[i] = signal[i];
            for (int j = 1; j <= order; j++) {
                if (i - j >= 0) {
                    filteredSignal[i] += bLow[j] * signal[i - j] - bHigh[j] * filteredSignal[i - j];
                }
            }
        }

        return filteredSignal;
    }

    private static double[] createButterworthCoefficients(double cutoff, int order, boolean isLowPass) {
        double[] coefficients = new double[order + 1];
        double a = Math.pow(Math.tan(Math.PI * cutoff), order);

        for (int i = 0; i <= order; i++) {
            coefficients[i] = isLowPass ? a / (1 + a) : (1 - a) / (1 + a);
        }

        return coefficients;
    }
}
