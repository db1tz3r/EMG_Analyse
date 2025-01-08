package Merkmalsextraktion;

public class Merkmal_Speicher {

    // Merkmals-Speicher
    private double[] merkmaleArray = new double[17]; //Array mit Merkmale (erster Wert, Wert MaximumSteigung, Steigungsformel a, Steigungsformel b, Steigungsformel c,
                                                        //                      letzter Wert, Wert MaximumSenkung, Senkungsformel a, Senkungsformel b, Senkungsformel c,
                                                        //                      Mittelformel a, Mittelformel b, Mittelformel c,
                                                        //                      Gesamtformel a, Gesamtformel b, Gesamtformel c,
                                                        //                      FFT RealTeil, FFT ImgTeil,


    // Set-Methode für die Polynomiale Approximation
    public void setPolynomialeApproximation (Double aValue, Double bValue, Double cValue, Integer formelTyp){
        if (formelTyp == 0) {
            int anfangsWert = 2;
            merkmaleArray[anfangsWert] = aValue;
            merkmaleArray[anfangsWert + 1] = bValue;
            merkmaleArray[anfangsWert + 2] = cValue;
        } else if (formelTyp == 1) {
            int anfangsWert = 10;
            merkmaleArray[anfangsWert] = aValue;
            merkmaleArray[anfangsWert + 1] = bValue;
            merkmaleArray[anfangsWert + 2] = cValue;
        } else if (formelTyp == 2) {
            int anfangsWert = 7;
            merkmaleArray[anfangsWert] = aValue;
            merkmaleArray[anfangsWert + 1] = bValue;
            merkmaleArray[anfangsWert + 2] = cValue;
        } else if (formelTyp == 3) {
            int anfangsWert = 13;
            merkmaleArray[anfangsWert] = aValue;
            merkmaleArray[anfangsWert + 1] = bValue;
            merkmaleArray[anfangsWert + 2] = cValue;
        }
    }

    // Set-Methode für die FFT-Werte
    public void setFFTValues(Double realTeil, Double imgTeil){
        merkmaleArray[16] = realTeil;
        merkmaleArray[17] = imgTeil;
    }

}
