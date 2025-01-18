import java.util.ArrayList;

public class Zyklenerkennung {

    private double vorherigerWert;
    private int zyklusIndex = 0;
    private ArrayList<Double> steigung = new ArrayList<>();
    private ArrayList<Double> senkung = new ArrayList<>();
    private boolean merkerSteigung = false;
    private boolean merkerSenkung = false;

    private double globalStartSteigung = Double.MAX_VALUE;
    private double globalEndeSteigung = Double.MIN_VALUE;
    private double globalStartSenkung = Double.MIN_VALUE;
    private double globalEndeSenkung = Double.MAX_VALUE;
    private int toleranzZaehler = 0;
    private final int maxToleranz = 7;
    private int stillstandsZaehler = 0; // Zählt die Werte ohne neue Aktivität
    private final int maxStillstand = 15; // Anzahl der Werte, nach denen globale Parameter zurückgesetzt werden

    public double[] starteZykluserkennung(double aktuellerWert, double schwelleProzent) {
        if (aktuellerWert == 0.0) {
            return new double[]{0, 0, 0, 0}; // Ignoriere 0.0-Werte
        }

        double durchschnittAmplitude = (vorherigerWert + aktuellerWert) / 2.0;
        double adaptiveSchwelle = durchschnittAmplitude * (schwelleProzent / 100.0);

        int minPunkte = 5; // Mindestens 5 Punkte pro Phase
        double minGesamtabweichung = 20.0; // Mindestamplitude für eine gültige Phase

        boolean aktivitaetErkannt = false; // Flag für erkannte Aktivität

        // Steigungsphase erkennen
        if ((aktuellerWert > vorherigerWert + adaptiveSchwelle)) {
            if (!merkerSteigung) {
                merkerSteigung = true;
                merkerSenkung = false;
                toleranzZaehler = 0;
                steigung.clear();
            }
            steigung.add(aktuellerWert);
            aktivitaetErkannt = true;
        } else if (merkerSteigung && toleranzZaehler < maxToleranz) {
            steigung.add(aktuellerWert); // Innerhalb der Toleranz speichern
            toleranzZaehler++;
        } else if (toleranzZaehler >= maxToleranz) {
            merkerSteigung = false; // Beende die Steigungsphase
        }

        // Senkungsphase erkennen
        if ((aktuellerWert < vorherigerWert - adaptiveSchwelle)) {
            if (!merkerSenkung) {
                merkerSenkung = true;
                merkerSteigung = false;
                toleranzZaehler = 0;
                senkung.clear();
            }
            senkung.add(aktuellerWert);
            aktivitaetErkannt = true;
        } else if (merkerSenkung && toleranzZaehler < maxToleranz) {
            senkung.add(aktuellerWert); // Innerhalb der Toleranz speichern
            toleranzZaehler++;
        } else if (toleranzZaehler >= maxToleranz) {
            merkerSenkung = false; // Beende die Senkungsphase
        }

        // Globalen Zyklus aktualisieren
        if (steigung.size() >= minPunkte && senkung.size() >= minPunkte) {
            double startSteigung = steigung.get(0);
            double endeSteigung = steigung.get(steigung.size() - 1);
            double startSenkung = senkung.get(0);
            double endeSenkung = senkung.get(senkung.size() - 1);

            // Sicherstellen, dass die Reihenfolge korrekt ist
            if (startSteigung < endeSteigung && startSenkung > endeSenkung && endeSteigung < startSenkung) {
                double gesamtabweichungSteigung = endeSteigung - startSteigung;
                double gesamtabweichungSenkung = startSenkung - endeSenkung;

                if (gesamtabweichungSteigung > minGesamtabweichung && gesamtabweichungSenkung > minGesamtabweichung) {
                    globalStartSteigung = Math.min(globalStartSteigung, startSteigung);
                    globalEndeSteigung = Math.max(globalEndeSteigung, endeSteigung);
                    globalStartSenkung = Math.max(globalStartSenkung, startSenkung);
                    globalEndeSenkung = Math.min(globalEndeSenkung, endeSenkung);

                    System.out.printf("Zyklus erkannt: Start Steigung=%.2f, Ende Steigung=%.2f, Start Senkung=%.2f, Ende Senkung=%.2f%n",
                            globalStartSteigung, globalEndeSteigung, globalStartSenkung, globalEndeSenkung);

                    steigung.clear();
                    senkung.clear();
                    merkerSteigung = false;
                    merkerSenkung = false;
                    toleranzZaehler = 0;
                    vorherigerWert = aktuellerWert;
                    stillstandsZaehler = 0; // Reset des Stillstandszählers

                    return new double[]{globalStartSteigung, globalEndeSteigung, globalStartSenkung, globalEndeSenkung};
                }
            }
        }

        // Stillstandszähler aktualisieren
        if (!aktivitaetErkannt) {
            stillstandsZaehler++;
            if (stillstandsZaehler >= maxStillstand) {
                resetGlobaleParameter();
            }
        } else {
            stillstandsZaehler = 0; // Reset bei erkannter Aktivität
        }

        vorherigerWert = aktuellerWert;
        return new double[]{0, 0, 0, 0}; // Kein vollständiger Zyklus erkannt
    }

    private void resetGlobaleParameter() {
        globalStartSteigung = Double.MAX_VALUE;
        globalEndeSteigung = Double.MIN_VALUE;
        globalStartSenkung = Double.MIN_VALUE;
        globalEndeSenkung = Double.MAX_VALUE;
        //System.out.println("Globale Parameter zurückgesetzt nach Stillstand von 10 Werten.");
    }
}
