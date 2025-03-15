package Management;

import Normalisierung.Normalisierung_Manager;
import Segmentation.Zyklenmanager;

import java.util.ArrayList;
import java.util.List;

public class InstanzManager {
    // Objekte
    private Normalisierung_Manager normalisierungManager; // Datenspeicher-Klasse
    private Zyklenmanager zyklenmanager; // Zyklenmanager-Klasse

    // Variablen
    private ArrayList<Double> rawData = new ArrayList<>();
    private int startZyklenerkennungIndex = 0;  // Startindex für die Zyklenerkennung
    // Zyklenmanager Variablen
    private double schwelleSteigungPorzent; // Schwelle für die Steigung in Prozent
    private int minBeteiligteWerteSteigung; // Mindestanzahl an beteiligten Werten für die Steigung
    private double minAmplitudeSteigung; // Mindestamplitude für die Steigung
    private int toleranzZwischenZyklen; // Toleranz für die Zwischenzyklen
    private int maxWerteOhneZyklus; // Maximale Anzahl an Werten ohne Zyklus

    // Konstruktor
    public InstanzManager(Normalisierung_Manager normalisierungManager, Zyklenmanager zyklenmanager,
                          double schwelleSteigungPorzent, int minBeteiligteWerteSteigung, double minAmplitudeSteigung, int toleranzZwischenZyklen,int  maxWerteOhneZyklus) {
        this.normalisierungManager = normalisierungManager;
        this.zyklenmanager = zyklenmanager;

        this.schwelleSteigungPorzent = schwelleSteigungPorzent;
        this.minBeteiligteWerteSteigung = minBeteiligteWerteSteigung;
        this.minAmplitudeSteigung = minAmplitudeSteigung;
        this.toleranzZwischenZyklen = toleranzZwischenZyklen;
        this.maxWerteOhneZyklus = maxWerteOhneZyklus;
    }

    // Start-Methode
    public List<ArrayList<Double>> startPipeline() {
//        System.out.println("RawData: " + rawData);

        // Überprüfe, ob rawData gültig ist
        if (rawData == null || rawData.isEmpty()) {
//            System.out.println("Fehler: rawData ist null oder leer!");
            return null;
        }

        // Start der Normalisierung
        ArrayList<Double> ergebnisNormalisierung = normalisierungManager.startNormalisierung(rawData);
//        System.out.println("Ergebnis Normalisierung: " + ergebnisNormalisierung);

        // Überprüfe, ob die Normalisierung ein Ergebnis hat
        if (ergebnisNormalisierung == null || ergebnisNormalisierung.isEmpty()) {
//            System.out.println("Fehler: Ergebnis Normalisierung ist leer!");
            return null;
        }

        // Start der Zyklenerkennung
        List<ArrayList<Double>> ergebnisZyklen = new ArrayList<>();
        boolean atLeastOneValidResult = false; // Prüft, ob mindestens ein gültiges Ergebnis existiert

        for (int i = 0; i < 5; i++) {
            List<ArrayList<Double>> tempErgebnis = zyklenmanager.startSegmentation(startZyklenerkennungIndex, startZyklenerkennungIndex,
                    schwelleSteigungPorzent, minBeteiligteWerteSteigung, minAmplitudeSteigung, toleranzZwischenZyklen, maxWerteOhneZyklus,
                    //7.0, 5, 30.0, 120, 120,
                    ergebnisNormalisierung, rawData);

            if (tempErgebnis != null && !tempErgebnis.isEmpty()) {
                ergebnisZyklen.addAll(tempErgebnis);
                atLeastOneValidResult = true; // Mindestens ein gültiges Ergebnis gefunden
            } else {
//                System.out.println("Warnung: startSegmentation() hat in Durchlauf " + i + " keine Werte geliefert.");
            }
            startZyklenerkennungIndex++;
        }

        // Falls kein Zyklus erkannt wurde oder alle tempErgebnisse `null` waren, gebe `null` zurück
        if (!atLeastOneValidResult) {
//            System.out.println("Fehler: Keine Zyklen erkannt.");
            return null;
        }

        // Rückgabe der gefundenen Zyklen
        return ergebnisZyklen;
    }










    // Setter und Getter
    public void setInputData(double inputData) {
        this.rawData.add(inputData);
    }
}
