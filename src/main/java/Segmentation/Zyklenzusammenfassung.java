package Segmentation;

import java.util.ArrayList;

public class Zyklenzusammenfassung {

    private ArrayList<Double> steigungGesamt = new ArrayList<>();
    private ArrayList<Double> senkungGesamt = new ArrayList<>();
    private ArrayList<Integer> steigungZeitpunkteGesamt = new ArrayList<>();
    private ArrayList<Integer> senkungZeitpunkteGesamt = new ArrayList<>();

    private boolean zusammenfassungAktiv = false;
    private int letzteSenkungEndeZeitpunkt = 0;
    private double globalStartSteigung = 0;
    private double globalEndeSenkung = 0;
    private double globalEndeSteigung = 0;
    private double globalStartSenkung = 0;
    private int globalZeitStartSteigung = 0;
    private int globalZeitEndeSenkung = 0;
    private int globalZeitEndeSteigung = 0;
    private int globalZeitStartSenkung = 0;

    public double[] verarbeiteUndGebeZyklusZurueck(double[] zyklusDaten , int toleranzWerte) {
        double startSteigung = zyklusDaten[0];
        double endeSteigung = zyklusDaten[1];
        double startSenkung = zyklusDaten[2];
        double endeSenkung = zyklusDaten[3];

        int zeitStartSteigung = (int) zyklusDaten[4];
        int zeitEndeSteigung = (int) zyklusDaten[5];
        int zeitStartSenkung = (int) zyklusDaten[6];
        int zeitEndeSenkung = (int) zyklusDaten[7];

        if (zusammenfassungAktiv && (zeitStartSteigung - letzteSenkungEndeZeitpunkt) <= toleranzWerte) {
            steigungGesamt.add(startSenkung);
            steigungGesamt.add(endeSteigung);
            steigungZeitpunkteGesamt.add(zeitStartSenkung);
            steigungZeitpunkteGesamt.add(zeitEndeSteigung);
            senkungGesamt.clear();
            senkungGesamt.add(startSenkung);
            senkungGesamt.add(endeSenkung);
            senkungZeitpunkteGesamt.clear();
            senkungZeitpunkteGesamt.add(zeitStartSenkung);
            senkungZeitpunkteGesamt.add(zeitEndeSenkung);
            globalEndeSteigung = endeSteigung;
            globalStartSenkung = startSenkung;
            globalEndeSenkung = endeSenkung;
            globalZeitEndeSteigung = zeitEndeSteigung;
            globalZeitStartSenkung = zeitStartSenkung;
            globalZeitEndeSenkung = zeitEndeSenkung;
        } else {
            if (zusammenfassungAktiv) {
                return ausgabeZusammengefassterZyklus();
            }
            steigungGesamt.clear();
            senkungGesamt.clear();
            steigungZeitpunkteGesamt.clear();
            senkungZeitpunkteGesamt.clear();
            steigungGesamt.add(startSteigung);
            steigungGesamt.add(endeSteigung);
            steigungZeitpunkteGesamt.add(zeitStartSteigung);
            steigungZeitpunkteGesamt.add(zeitEndeSteigung);
            senkungGesamt.add(startSenkung);
            senkungGesamt.add(endeSenkung);
            senkungZeitpunkteGesamt.add(zeitStartSenkung);
            senkungZeitpunkteGesamt.add(zeitEndeSenkung);
            globalStartSteigung = startSteigung;
            globalEndeSteigung = endeSteigung;
            globalStartSenkung = startSenkung;
            globalEndeSenkung = endeSenkung;
            globalZeitStartSteigung = zeitStartSteigung;
            globalZeitEndeSteigung = zeitEndeSteigung;
            globalZeitStartSenkung = zeitStartSenkung;
            globalZeitEndeSenkung = zeitEndeSenkung;
            zusammenfassungAktiv = true;
        }
        letzteSenkungEndeZeitpunkt = zeitEndeSenkung;
        return new double[]{0, 0, 0, 0, 0, 0, 0, 0};
    }

    public double[] ausgabeZusammengefassterZyklus() {
        zusammenfassungAktiv = false;
//        System.out.println("Zykluswertergebnis: " + globalStartSteigung + "| " + globalEndeSteigung + " | " + globalStartSenkung + " | " + globalEndeSenkung);
//        System.out.println("Zykluszeit: " + globalZeitStartSteigung + " | " + globalZeitEndeSteigung + " | " + globalZeitStartSenkung + " | " + globalZeitEndeSenkung);
        return new double[]{
                globalStartSteigung, globalEndeSteigung,
                globalStartSenkung, globalEndeSenkung,
                globalZeitStartSteigung, globalZeitEndeSteigung,
                globalZeitStartSenkung, globalZeitEndeSenkung
        };
    }
}
