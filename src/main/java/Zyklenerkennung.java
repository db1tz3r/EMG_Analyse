import java.util.ArrayList;

public class Zyklenerkennung {

    private double vorherigerWert;
    private int zyklusIndex = 0;
    private ArrayList<Double> steigung = new ArrayList<>();
    private ArrayList<Double> senkung = new ArrayList<>();
    private boolean merkerSteigung = false;
    private boolean merkerSenkung = false;

    public double[] starteZykluserkennung(ArrayList<Double> signal, double schwelleProzent, double toleranzProzent) {
        if (signal == null || signal.isEmpty()) {
            throw new IllegalArgumentException("Das Signal darf nicht null oder leer sein.");
        }

        if (zyklusIndex == 0) {
            vorherigerWert = signal.get(zyklusIndex++);
            return new double[]{0, 0, 0, 0}; // Kein Zyklus erkannt
        }

        double aktuellerWert = signal.get(zyklusIndex);
        double schwelle = vorherigerWert * (schwelleProzent / 100.0);

        // Steigungsphase erkennen und speichern
        if (aktuellerWert > vorherigerWert + schwelle) {
            if (!merkerSteigung) {
                merkerSteigung = true;
                merkerSenkung = false;
                steigung.clear();
            }
            steigung.add(aktuellerWert);
        }

        // Senkungsphase erkennen und speichern
        if (aktuellerWert < vorherigerWert - schwelle) {
            if (!merkerSenkung) {
                merkerSenkung = true;
                merkerSteigung = false;
                senkung.clear();
            }
            senkung.add(aktuellerWert);
        }

        // Zyklusprüfung: Sind Steigung und Senkung vollständig?
        if (!steigung.isEmpty() && !senkung.isEmpty()) {
            double amplitudeSteigung = steigung.get(steigung.size() - 1) - steigung.get(0);
            double amplitudeSenkung = senkung.get(0) - senkung.get(senkung.size() - 1);
            double toleranz = Math.abs(amplitudeSteigung) * (toleranzProzent / 100.0);

            if (Math.abs(amplitudeSteigung - amplitudeSenkung) <= toleranz) {
                // Zykluswerte berechnen
                double startSteigung = steigung.get(0);
                double endeSteigung = steigung.get(steigung.size() - 1);
                double startSenkung = senkung.get(0);
                double endeSenkung = senkung.get(senkung.size() - 1);

                // Rückgabe des vollständigen Zyklus
                zyklusIndex++;
                steigung.clear();
                senkung.clear();
                return new double[]{startSteigung, endeSteigung, startSenkung, endeSenkung};
            }
        }

        vorherigerWert = aktuellerWert;
        zyklusIndex++;
        return new double[]{0, 0, 0, 0}; // Kein Zyklus erkannt
    }
}
