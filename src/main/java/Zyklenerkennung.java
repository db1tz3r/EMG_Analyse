// Ideee: Funktionsweise der Standardabweichung vom vorherigen zum nachherigen Wert mit +-7%

//Hinzufügen von doppelten Extrempunkten, sodass besser zuordbar

import java.util.ArrayList;

public class Zyklenerkennung {

    private double vorherigerWert;
    private int zyklusIndex = 0;
    private boolean merkerHoch = false, merkerRunter = false;

    public double[] starteZyklenerkennung(ArrayList arrayListZyklenInput) {
        if (zyklusIndex == 0) {
            vorherigerWert = (double) arrayListZyklenInput.get(zyklusIndex);
            zyklusIndex++;
        } else {
            double ergebnisProzentVorherigerWert = vorherigerWert * 0.07;
//            System.out.println("VorherigerWert: " + vorherigerWert);
//            System.out.println("untere Grenze: " + (vorherigerWert - ergebnisProzentVorherigerWert));
//            System.out.println("obere Grenze: " + (vorherigerWert + ergebnisProzentVorherigerWert));
//            System.out.println("AktuellerWert: " + (double) arrayListZyklenInput.get(zyklusIndex));
//            System.out.println(merkerHoch);

            // Bedingung wenn es am Steigen ist -> Solange, is nicht mehr am steigen
            if ((vorherigerWert + ergebnisProzentVorherigerWert) < (double) arrayListZyklenInput.get(zyklusIndex) && !merkerHoch) {
                vorherigerWert = (double) arrayListZyklenInput.get(zyklusIndex);
                zyklusIndex++;
                merkerHoch = true;
                merkerRunter = false;
                //System.out.println("Schleife 1"); //Kontrolloutput

                //If bedingung, um zwei Höhepunkte zu haben bei lokaler Extremstelle
                if ((((double) arrayListZyklenInput.get(zyklusIndex -3) * 0.07) + (double) arrayListZyklenInput.get(zyklusIndex -3)) < (double) arrayListZyklenInput.get(zyklusIndex -2)) {
                    return new double[]{(double) arrayListZyklenInput.get(zyklusIndex - 2), zyklusIndex - 2 , 2};
                }
                return new double[]{(double) arrayListZyklenInput.get(zyklusIndex - 2), zyklusIndex - 2, 1};
            } else if (merkerHoch && !merkerRunter &&
                    (vorherigerWert - ergebnisProzentVorherigerWert) < (double) arrayListZyklenInput.get(zyklusIndex) &&
                    (double) arrayListZyklenInput.get(zyklusIndex) < (vorherigerWert + ergebnisProzentVorherigerWert)) {
                vorherigerWert = (double) arrayListZyklenInput.get(zyklusIndex);
                zyklusIndex++;
                merkerHoch = false;
                //System.out.println("Schleife 3"); //Kontrolloutput
                //ergebnisVorherWert = (double) arrayListZyklenInput.get(zyklusIndex - 1);
                return new double[]{(double) arrayListZyklenInput.get(zyklusIndex - 2), zyklusIndex - 2, 1};
            }


            // Bedingung wenn es am Sinken ist -> Solange, is nicht mehr am sinken
            else if ((vorherigerWert - ergebnisProzentVorherigerWert) > (double) arrayListZyklenInput.get(zyklusIndex) && !merkerRunter) {
                vorherigerWert = (double) arrayListZyklenInput.get(zyklusIndex);
                zyklusIndex++;
                merkerRunter = true;
                merkerHoch = false;
                //System.out.println("Schleife 2"); //Kontrolloutput

                //If bedingung, um zwei Höhepunkte zu haben bei lokaler Extremstelle
                if ((((double) arrayListZyklenInput.get(zyklusIndex -3) * 0.07) + (double) arrayListZyklenInput.get(zyklusIndex -3)) < (double) arrayListZyklenInput.get(zyklusIndex -2)) {
                    return new double[]{(double) arrayListZyklenInput.get(zyklusIndex - 2), zyklusIndex - 2, 2};
                }
                return new double[]{(double) arrayListZyklenInput.get(zyklusIndex - 2), zyklusIndex - 2, 1};
            } else if (!merkerHoch && merkerRunter &&
                    (vorherigerWert - ergebnisProzentVorherigerWert) < (double) arrayListZyklenInput.get(zyklusIndex) &&
                    (double) arrayListZyklenInput.get(zyklusIndex) < (vorherigerWert + ergebnisProzentVorherigerWert)) {
                vorherigerWert = (double) arrayListZyklenInput.get(zyklusIndex);
                zyklusIndex++;
                merkerRunter = false;
                //System.out.println("Schleife 4"); //Kontrolloutput
                //ergebnisVorherWert = (double) arrayListZyklenInput.get(zyklusIndex - 2);
                return new double[]{(double) arrayListZyklenInput.get(zyklusIndex - 2), zyklusIndex - 2, 1};
            }
            vorherigerWert = (double) arrayListZyklenInput.get(zyklusIndex);
            zyklusIndex++;
        }
        return new double[]{0, 0, 1};
    }
}
