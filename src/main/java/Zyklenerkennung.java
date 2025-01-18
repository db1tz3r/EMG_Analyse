import java.util.ArrayList;

public class Zyklenerkennung {

    private double vorherigerWert;
    private int zyklusIndex = 0;
    private ArrayList<Double> steigung = new ArrayList<>();
    private ArrayList<Double> senkung = new ArrayList<>();
    private boolean merkerSteigung = false;
    private boolean merkerSenkung = false;
    private int zaehlerSeitSenkung = 0; // Zähler für Werte nach Beginn der Senkung

    public double[] starteZykluserkennung(double aktuellerWert, double schwelleProzent) {
        if (aktuellerWert == 0.0) {
            return new double[]{0, 0, 0, 0}; // Ignoriere 0.0-Werte
        }

        double durchschnittAmplitude = (vorherigerWert + aktuellerWert) / 2.0;
        double adaptiveSchwelle = durchschnittAmplitude * (schwelleProzent / 100.0);

        int minPunkte = 5; // Mindestens 5 Punkte pro Phase
        double minGesamtabweichung = 20.0; // Mindestamplitude für eine gültige Phase

        // Steigungsphase erkennen
        if ((aktuellerWert > vorherigerWert + adaptiveSchwelle) && Math.abs(aktuellerWert - vorherigerWert) > minGesamtabweichung) {
            if (!merkerSteigung) {
                merkerSteigung = true;
                merkerSenkung = false;
                steigung.clear();
                zaehlerSeitSenkung = 0; // Reset Zähler, da eine neue Steigung beginnt
            }
            steigung.add(aktuellerWert);
        }

        // Senkungsphase erkennen
        if ((aktuellerWert < vorherigerWert - adaptiveSchwelle) && Math.abs(vorherigerWert - aktuellerWert) > minGesamtabweichung) {
            if (!merkerSenkung) {
                merkerSenkung = true;
                merkerSteigung = false;
                senkung.clear();
                zaehlerSeitSenkung = 0; // Zähler zurücksetzen, da eine neue Senkung beginnt
            }
            senkung.add(aktuellerWert);
        }

        // Überprüfen, ob innerhalb von 10 Werten nach Beginn der Senkung eine höhere Steigung folgt
        if (merkerSenkung) {
            zaehlerSeitSenkung++;
            if (!steigung.isEmpty() && zaehlerSeitSenkung <= 10 && aktuellerWert > steigung.get(steigung.size() - 1)) {
                // Steigung wird fortgesetzt ab dem neuen höheren Wert
                merkerSenkung = false;
                merkerSteigung = true;
                steigung.add(aktuellerWert);
                zaehlerSeitSenkung = 0; // Reset des Zählers
            }
        }

        // Zyklusprüfung
        if (steigung.size() >= minPunkte && senkung.size() >= minPunkte) {
            double startSteigung = steigung.get(0);
            double endeSteigung = steigung.get(steigung.size() - 1);
            double startSenkung = senkung.get(0);
            double endeSenkung = senkung.get(senkung.size() - 1);

            if ((endeSteigung - startSteigung) > minGesamtabweichung && (startSenkung - endeSenkung) > minGesamtabweichung) {
                System.out.printf("Zyklus erkannt: Start Steigung=%.2f, Ende Steigung=%.2f, Start Senkung=%.2f, Ende Senkung=%.2f%n",
                        startSteigung, endeSteigung, startSenkung, endeSenkung);
                System.out.println("Steigung: " + steigung);
                System.out.println("Senkung: " + senkung);

                steigung.clear();
                senkung.clear();
                merkerSteigung = false;
                merkerSenkung = false;
                vorherigerWert = aktuellerWert;
                return new double[]{startSteigung, endeSteigung, startSenkung, endeSenkung};
            }
        }

        vorherigerWert = aktuellerWert;
        return new double[]{0, 0, 0, 0}; // Kein vollständiger Zyklus erkannt
    }

}
