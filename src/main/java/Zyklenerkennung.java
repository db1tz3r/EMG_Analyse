import java.util.ArrayList;

public class Zyklenerkennung {

    private double vorherigerWert;
    private int zyklusIndex = 0;
    private ArrayList<Double> steigung = new ArrayList<>();
    private ArrayList<Double> senkung = new ArrayList<>();
    private ArrayList<Integer> steigungZeitpunkte = new ArrayList<>(); // Zeitpunkte für Steigung
    private ArrayList<Integer> senkungZeitpunkte = new ArrayList<>(); // Zeitpunkte für Senkung
    private boolean merkerSteigung = false;
    private boolean merkerSenkung = false;
    private int zaehlerSeitSenkung = 0; // Zähler für Werte nach Beginn der Senkung

    public double[] starteZykluserkennung(double aktuellerWert, double schwelleProzent, int zeitpunkt) {
        if (aktuellerWert == 0.0) {
            return new double[]{0, 0, 0, 0, 0, 0, 0, 0}; // Ignoriere 0.0-Werte
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
                steigungZeitpunkte.clear();
                zaehlerSeitSenkung = 0; // Reset Zähler, da eine neue Steigung beginnt
            }
            steigung.add(aktuellerWert);
            steigungZeitpunkte.add(zeitpunkt);
        }

        // Senkungsphase erkennen
        if ((aktuellerWert < vorherigerWert - adaptiveSchwelle) && Math.abs(vorherigerWert - aktuellerWert) > minGesamtabweichung) {
            if (!merkerSenkung) {
                merkerSenkung = true;
                merkerSteigung = false;
                senkung.clear();
                senkungZeitpunkte.clear();
                zaehlerSeitSenkung = 0; // Zähler zurücksetzen, da eine neue Senkung beginnt
            }
            senkung.add(aktuellerWert);
            senkungZeitpunkte.add(zeitpunkt);
        }

        // Überprüfen, ob innerhalb von 10 Werten nach Beginn der Senkung eine höhere Steigung folgt
        if (merkerSenkung) {
            zaehlerSeitSenkung++;
            if (!steigung.isEmpty() && zaehlerSeitSenkung <= 10 && aktuellerWert > steigung.get(steigung.size() - 1)) {
                // Steigung wird fortgesetzt ab dem neuen höheren Wert
                merkerSenkung = false;
                merkerSteigung = true;
                steigung.add(aktuellerWert);
                steigungZeitpunkte.add(zeitpunkt);
                zaehlerSeitSenkung = 0; // Reset des Zählers
            }
        }

        // Zyklusprüfung
        if (steigung.size() >= minPunkte && senkung.size() >= minPunkte) {
            double startSteigung = steigung.get(0);
            double endeSteigung = steigung.get(steigung.size() - 1);
            double startSenkung = senkung.get(0);
            double endeSenkung = senkung.get(senkung.size() - 1);

            int zeitStartSteigung = steigungZeitpunkte.get(0);
            int zeitEndeSteigung = steigungZeitpunkte.get(steigungZeitpunkte.size() - 1);
            int zeitStartSenkung = senkungZeitpunkte.get(0);
            int zeitEndeSenkung = senkungZeitpunkte.get(senkungZeitpunkte.size() - 1);

            if ((endeSteigung - startSteigung) > minGesamtabweichung && (startSenkung - endeSenkung) > minGesamtabweichung) {
                //System.out.println("Zyklus: StartSteigung: " + startSteigung + " EndeSteigung: " + endeSteigung + " StartSenkung: " + startSenkung + " EndeSenkung: " + endeSenkung);

                steigung.clear();
                senkung.clear();
                steigungZeitpunkte.clear();
                senkungZeitpunkte.clear();
                merkerSteigung = false;
                merkerSenkung = false;
                vorherigerWert = aktuellerWert;
                return new double[]{startSteigung, endeSteigung, startSenkung, endeSenkung, zeitStartSteigung, zeitEndeSteigung, zeitStartSenkung, zeitEndeSenkung};
            }
        }

        vorherigerWert = aktuellerWert;
        return new double[]{0, 0, 0, 0, 0, 0, 0, 0}; // Kein vollständiger Zyklus erkannt
    }

}
