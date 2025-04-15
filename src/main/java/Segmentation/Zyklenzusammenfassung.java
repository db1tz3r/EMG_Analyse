package Segmentation;

import java.util.ArrayList;

public class Zyklenzusammenfassung {

    // Listen zur Speicherung der Steigungs- und Senkungswerte sowie deren Zeitpunkte
    private ArrayList<Double> steigungGesamt = new ArrayList<>(); // Gesamte Steigungswerte
    private ArrayList<Double> senkungGesamt = new ArrayList<>(); // Gesamte Senkungswerte
    private ArrayList<Integer> steigungZeitpunkteGesamt = new ArrayList<>(); // Zeitpunkte der Steigungen
    private ArrayList<Integer> senkungZeitpunkteGesamt = new ArrayList<>(); // Zeitpunkte der Senkungen

    // Statusvariablen für die Zusammenfassung
    private boolean zusammenfassungAktiv = false; // Gibt an, ob eine Zusammenfassung aktiv ist
    private int letzteSenkungEndeZeitpunkt = 0; // Zeitstempel des letzten Endes einer Senkung

    // Globale Variablen zur Speicherung der aktuellen Zyklusdaten
    private double globalStartSteigung = 0; // Startwert der Steigung
    private double globalEndeSteigung = 0; // Endwert der Steigung
    private double globalStartSenkung = 0; // Startwert der Senkung
    private double globalEndeSenkung = 0; // Endwert der Senkung
    private int globalZeitStartSteigung = 0; // Startzeitpunkt der Steigung
    private int globalZeitEndeSteigung = 0; // Endzeitpunkt der Steigung
    private int globalZeitStartSenkung = 0; // Startzeitpunkt der Senkung
    private int globalZeitEndeSenkung = 0; // Endzeitpunkt der Senkung

    // Methode zur Verarbeitung eines Zyklus und Rückgabe der zusammengefassten Daten
    public double[] verarbeiteUndGebeZyklusZurueck(double[] zyklusDaten, int toleranzWerte) {
        // Extrahiert die Zyklusdaten aus dem Eingabearray
        double startSteigung = zyklusDaten[0];
        double endeSteigung = zyklusDaten[1];
        double startSenkung = zyklusDaten[2];
        double endeSenkung = zyklusDaten[3];

        int zeitStartSteigung = (int) zyklusDaten[4];
        int zeitEndeSteigung = (int) zyklusDaten[5];
        int zeitStartSenkung = (int) zyklusDaten[6];
        int zeitEndeSenkung = (int) zyklusDaten[7];

        // Überprüft, ob die Zusammenfassung aktiv ist und die Toleranz eingehalten wird
        if (zusammenfassungAktiv && (zeitStartSteigung - letzteSenkungEndeZeitpunkt) <= toleranzWerte) {
            // Fügt die neuen Werte zur bestehenden Zusammenfassung hinzu
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

            // Aktualisiert die globalen Variablen
            globalEndeSteigung = endeSteigung;
            globalStartSenkung = startSenkung;
            globalEndeSenkung = endeSenkung;
            globalZeitEndeSteigung = zeitEndeSteigung;
            globalZeitStartSenkung = zeitStartSenkung;
            globalZeitEndeSenkung = zeitEndeSenkung;
        } else {
            // Gibt die zusammengefassten Daten zurück, wenn die Zusammenfassung abgeschlossen ist
            if (zusammenfassungAktiv) {
                return ausgabeZusammengefassterZyklus();
            }

            // Initialisiert eine neue Zusammenfassung
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

            // Setzt die globalen Variablen
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

        // Aktualisiert den letzten Endzeitpunkt der Senkung
        letzteSenkungEndeZeitpunkt = zeitEndeSenkung;

        // Gibt ein leeres Array zurück, wenn die Zusammenfassung noch nicht abgeschlossen ist
        return new double[]{0, 0, 0, 0, 0, 0, 0, 0};
    }

    // Methode zur Ausgabe der zusammengefassten Zyklusdaten
    public double[] ausgabeZusammengefassterZyklus() {
        zusammenfassungAktiv = false; // Deaktiviert die Zusammenfassung
        return new double[]{
                globalStartSteigung, globalEndeSteigung,
                globalStartSenkung, globalEndeSenkung,
                globalZeitStartSteigung, globalZeitEndeSteigung,
                globalZeitStartSenkung, globalZeitEndeSenkung
        };
    }
}