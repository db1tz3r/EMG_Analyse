package Merkmalsextraktion;

import java.util.ArrayList;

public class FastFourierTransformation extends Thread {

    // Variablen
    private double[] inputArray;
    private static Merkmal_Speicher merkmalSpeicher;

    // Konstruktor
    public FastFourierTransformation(Merkmal_Speicher merkmalSpeicher) {
        this.merkmalSpeicher = merkmalSpeicher;
    }

    // Thread zur parallelen Berechnung der FFT
    @Override
    public void run() {
        calculateFFT(this.inputArray);
    }

    // Klasse zur Darstellung komplexer Zahlen
    public static class Complex {
        public final double real;
        public final double imag;

        public Complex(double real, double imag) {
            this.real = real;
            this.imag = imag;
        }

        public Complex add(Complex b) {
            return new Complex(this.real + b.real, this.imag + b.imag);
        }

        public Complex multiply(Complex b) {
            return new Complex(this.real * b.real - this.imag * b.imag,
                    this.real * b.imag + this.imag * b.real);
        }
    }

    // FFT-Methode (rekursiv) für ein Array komplexer Zahlen
    public static Complex[] fft(Complex[] x) {
        int n = x.length;
        if (n == 1) return new Complex[]{x[0]};

        Complex[] even = new Complex[n / 2];
        Complex[] odd = new Complex[n / 2];
        for (int i = 0; i < n / 2; i++) {
            even[i] = x[i * 2];
            odd[i] = x[i * 2 + 1];
        }
        Complex[] q = fft(even);
        Complex[] r = fft(odd);

        Complex[] y = new Complex[n];
        for (int k = 0; k < n / 2; k++) {
            double kth = -2 * k * Math.PI / n;
            Complex wk = new Complex(Math.cos(kth), Math.sin(kth));
            y[k] = q[k].add(wk.multiply(r[k]));
            y[k + n / 2] = q[k].add(wk.multiply(r[k])).multiply(new Complex(-1, 0));
        }
        return y;
    }

    // Hilfsmethode: FFT für ein Array von double-Werten
    public static Complex[] fft(double[] input) {
        int n = input.length;
        int m = (int) Math.pow(2, Math.ceil(Math.log(n) / Math.log(2)));
        Complex[] x = new Complex[m];

        for (int i = 0; i < m; i++) {
            x[i] = i < n ? new Complex(input[i], 0) : new Complex(0, 0);
        }

        return fft(x);  // FFT auf gepolstertes Array anwenden
    }

    // Methode zur Berechnung der FFT und Ausgabe von Real- und Imaginärteil
    public static void calculateFFT(double[] input) {
        Complex[] fftResult = fft(input);

        // Ausgabe von Real- und Imaginärteil
        for (int i = 0; i < fftResult.length; i++) {
            double real = fftResult[i].real;
            double imag = fftResult[i].imag;
            merkmalSpeicher.setFFTValues( real, imag);
            System.out.printf("Index %d: Realteil: %.5f, Imaginärteil: %.5f%n", i, real, imag);
        }
    }

    public void setInput(ArrayList<Double> inputArrayList) {
        this.inputArray = inputArrayList.stream().mapToDouble(i -> (double) i).toArray();
    }
}
