package Segmentation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Zyklenmanager {
    // Variablen
    private int zyklusAnschliessenNachZeitCount = 0;    // Zähler für die Anzahl an Werten ohne Zyklus
    private double[] letztesAusgegebenesErgebnis = new double[8];   // Speicher für das letzte ausgegebene Ergebnis
    private ArrayList<Double> rohDaten = new ArrayList<>();    // Liste für die Rohdaten
    private ArrayList<Double> gerichteterInput = new ArrayList<>();    // Liste für die Peak-Normalisierungswerte
    private ArrayList<Double> zyklusArrayWertErgebnis = new ArrayList<>();  // Liste für die Werte der Zyklen
    private ArrayList<Integer> zyklusArrayZeitErgebnis = new ArrayList<>();
    private int zyklusArrayWertErgebnisSeizeOld = 0;    // Speicher für die Größe des Wertearrays
    private int instanzID;    // ID der Instanz

    // Objekte
    private Zyklenerkennung zykluserkennung;    // Objekt der Klasse Zyklenerkennung
    private Zyklenzusammenfassung zyklenzusammenfassung;    // Objekt der Klasse Zyklenzusammenfassung
    private Zyklen_Speicher zyklenSpeicher;    // Objekt der Klasse Zyklen_Speicher

    // Konsruktor
    public Zyklenmanager(Zyklenerkennung zykluserkennung, Zyklenzusammenfassung zyklenzusammenfassung, Zyklen_Speicher zyklenSpeicher, int instanzID) {
        this.zykluserkennung = zykluserkennung;
        this.zyklenzusammenfassung = zyklenzusammenfassung;
        this.zyklenSpeicher = zyklenSpeicher;
        this.instanzID = instanzID;
    }

    // Methode zum Starten der Zykluserkennung
    public List<List<List>> startSegmentation(int startZyklenerkennungIndex, int globalerZeitpunkt, double schwelleProzent, int minPunkte, double minGesamtabweichung,
                                              int toleranzZwischenzyklen, int maxWerteOhneZyklus, ArrayList<Double> peakNormalisierungArrayErgebnis, ArrayList<Double> rohDaten) {
        // Setze die Inputs
        this.rohDaten = rohDaten;
        this.gerichteterInput = peakNormalisierungArrayErgebnis;

        // Starte die Zykluserkennung mit dem aktuellen Index und globalem Zeitpunkt
        boolean zyklusErkannt = startZyklenerkennung(startZyklenerkennungIndex, globalerZeitpunkt, schwelleProzent, minPunkte, minGesamtabweichung,
                toleranzZwischenzyklen, maxWerteOhneZyklus, peakNormalisierungArrayErgebnis);

        // Arrays generieren aus den einzelnen Daten
        if (zyklusErkannt) {
            List<ArrayList<Double>> tempListWithArrays = generateListWithArrays();
//        System.out.println("Zyklenmanager: " + instanzID + " " + tempListWithArrays);
            if (tempListWithArrays != null) {
//            System.out.println("Zyklenmanager: " + instanzID + " " + tempListWithArrays);

                // Hinzufügen der Listen zu dem Zyklen_Speicher
                zyklenSpeicher.addArraysToZyklusinstanz(instanzID, tempListWithArrays);

                // Überprüfen auf hintereinanderfolgende Instanzen
                List<List<List>> checkErgebnis = zyklenSpeicher.checkIntervallFromAllInstanzes(instanzID);

                return checkErgebnis;
            }
        }
    }














    // Methode zum Starten der Zykluserkennung
    private boolean startZyklenerkennung(int startZyklenerkennungIndex, int globalerZeitpunkt, double schwelleProzent, int minPunkte, double minGesamtabweichung,
                                  int toleranzZwischenzyklen, int maxWerteOhneZyklus, ArrayList<Double> peakNormalisierungArrayErgebnis) {
        // Starte die Zykluserkennung mit dem aktuellen Index und globalem Zeitpunkt
        double[] ergebnis = zykluserkennung.starteZykluserkennung(
                peakNormalisierungArrayErgebnis.get(startZyklenerkennungIndex), schwelleProzent, globalerZeitpunkt, minPunkte, minGesamtabweichung);
        //System.out.println(peakNormalisierungArrayErgebnis.size());

        if (ergebnis[0] != 0) {
            // Speicher den Merker, dass die Instanz läuft weitergeben
            zyklenSpeicher.setInstanzZyklusAktive(instanzID, (int) ergebnis[4]);

            //Starten der Zykluszusammenfassung
            zyklusAnschliessenNachZeitCount = 0;
            //System.out.println("Ergebnis: " + ergebnis[0] + " " + ergebnis[1] + " " + ergebnis[2] + " " + ergebnis[3]);
            //System.out.println("Zeit: " + ergebnis[4] + " " + ergebnis[5] + " " + ergebnis[6] + " " + ergebnis[7]);

            // Übergabe der erkannten Zyklen an die Zusammenfassungsmethode
            ergebnis = zyklenzusammenfassung.verarbeiteUndGebeZyklusZurueck(ergebnis, toleranzZwischenzyklen);

            // Überprüfe, ob ein vollständiger Zyklus erkannt wurde
            if (ergebnis[0] != 0 || ergebnis[1] != 0 || ergebnis[2] != 0 || ergebnis[3] != 0) {
                for (int i = 0; i < 4; i++) {
                    zyklusArrayWertErgebnis.add(ergebnis[i]);
                }
                for (int i = 4; i < 8; i++) {
                    zyklusArrayZeitErgebnis.add((int) ergebnis[i]);
                }
                // Speichere das letzte ausgegebene Ergebnis zur Vermeidung doppelter Ausgaben
                letztesAusgegebenesErgebnis = ergebnis.clone();
            }
        } else {
            zyklusAnschliessenNachZeitCount++;
            if (zyklusAnschliessenNachZeitCount >= maxWerteOhneZyklus) {
                double[] zusammengefassterZyklus = zyklenzusammenfassung.ausgabeZusammengefassterZyklus();
                if (!Arrays.equals(zusammengefassterZyklus, letztesAusgegebenesErgebnis)) {
                    zyklusAnschliessenNachZeitCount = 0;
                    for (int i = 0; i < 4; i++) {
                        zyklusArrayWertErgebnis.add(zusammengefassterZyklus[i]);
                    }
                    for (int i = 4; i < 8; i++) {
                        zyklusArrayZeitErgebnis.add((int) zusammengefassterZyklus[i]);
                    }
                    letztesAusgegebenesErgebnis = zusammengefassterZyklus.clone();
                    return true;
                }
            }
        }
        return false;
    }

    // Methode zum Generieren der Arrays mit folgenden Arrays
//            steigungsArrayGerichteteWerte, steigungsArrayRohWerte, steigungsArrayZeit,
//            mittelwertArrayGerichteteWerte, mittelwertArrayRohWerte, mittelwertArrayZeit,
//            senkungsArrayGerichteteWerte, senkungsArrayRohWerte, senkungsArrayZeit

    private List<ArrayList<Double>> generateListWithArrays() {
        // Generiere die Arrays aus den einzelnen Daten
        if (zyklusArrayWertErgebnis.size() >= 4 && (zyklusArrayWertErgebnis.size() != zyklusArrayWertErgebnisSeizeOld)) {
            zyklusArrayWertErgebnisSeizeOld = zyklusArrayWertErgebnis.size();
            if (zyklusArrayWertErgebnis.size() % 4 == 0) {
                // Extrahiere die letzten vier Werte
                double startSteigung = zyklusArrayWertErgebnis.get(zyklusArrayWertErgebnis.size() - 4);
                double endeSteigung = zyklusArrayWertErgebnis.get(zyklusArrayWertErgebnis.size() - 3);
                double startSenkung = zyklusArrayWertErgebnis.get(zyklusArrayWertErgebnis.size() - 2);
                double endeSenkung = zyklusArrayWertErgebnis.get(zyklusArrayWertErgebnis.size() - 1);

                // Überprüfe, ob die Werte gültige Bedingungen für einen Zyklus erfüllen (Zweite Sicherheitsprüfung)
                if (endeSteigung > startSteigung && endeSenkung < startSenkung) {
//                    System.out.println("Sensor: " + instanzID + " Kompletter Muskelzyklus erkannt");
//                    System.out.printf("Start Steigung: %.2f, Ende Steigung: %.2f, Start Senkung: %.2f, Ende Senkung: %.2f%n",
//                            startSteigung, endeSteigung, startSenkung, endeSenkung);

                    // Extrahiere die Werte der Steigung in gerichteten Daten
                    ArrayList<Double> steigungsArrayGerichteteWerte = new ArrayList<>();

                    for (int i = zyklusArrayZeitErgebnis.get(zyklusArrayWertErgebnis.size() - 4) - 1; i <= (zyklusArrayZeitErgebnis.get(zyklusArrayWertErgebnis.size() - 3) - 1); i++) {
                        //System.out.println(zyklusArrayInput.get(i + 1));
                        steigungsArrayGerichteteWerte.add(gerichteterInput.get(i + 1));
                    }

                    // Extrahiere die Werte der Steigung in Rohdaten
                    ArrayList<Double> steigungsArrayRohWerte = new ArrayList<>();

                    for (int i = zyklusArrayZeitErgebnis.get(zyklusArrayWertErgebnis.size() - 4) - 1; i <= (zyklusArrayZeitErgebnis.get(zyklusArrayWertErgebnis.size() - 3)) - 1; i++) {
                        //System.out.println(zyklusArrayInput.get(i + 1));
                        steigungsArrayRohWerte.add(rohDaten.get(i + 1));
                    }

                    // Extrahiere die Zeitpunkte der Steigung
                    ArrayList<Double> steigungsArrayZeit = new ArrayList<>();

                    int j = 0; // Zähler für die Zeit
                    for (int i = zyklusArrayZeitErgebnis.get(zyklusArrayWertErgebnis.size() - 4) - 1; i <= (zyklusArrayZeitErgebnis.get(zyklusArrayWertErgebnis.size() - 3)) - 1; i++) {
                        steigungsArrayZeit.add(Double.valueOf(zyklusArrayZeitErgebnis.get(zyklusArrayWertErgebnis.size() - 4) - 1 + j));
                        j++;
                    }

                    // Extrahiere die Werte des Mittelwertes in gerichteten Daten
                    ArrayList<Double> mittelwertArrayGerichteteWerte = new ArrayList<>();

                    for (int i = zyklusArrayZeitErgebnis.get(zyklusArrayWertErgebnis.size() - 3); i < (zyklusArrayZeitErgebnis.get(zyklusArrayWertErgebnis.size() - 2)) - 1; i++) {
                        //System.out.println(zyklusArrayInput.get(i + 1));
                        mittelwertArrayGerichteteWerte.add(gerichteterInput.get(i + 1));
                    }

                    // Extrahiere die Werte des Mittelwertes in Rohdaten
                    ArrayList<Double> mittelwertArrayRohWerte = new ArrayList<>();

                    for (int i = zyklusArrayZeitErgebnis.get(zyklusArrayWertErgebnis.size() - 3); i < (zyklusArrayZeitErgebnis.get(zyklusArrayWertErgebnis.size() - 2)) - 1; i++) {
                        //System.out.println(zyklusArrayInput.get(i + 1));
                        mittelwertArrayRohWerte.add(rohDaten.get(i + 1));
                    }

                    // Extrahiere die Zeitpunkte des Mittelwertes
                    ArrayList<Double> mittelwertArrayZeit = new ArrayList<>();

                    j = 0; // Zähler für die Zeit
                    for (int i = zyklusArrayZeitErgebnis.get(zyklusArrayWertErgebnis.size() - 3); i < (zyklusArrayZeitErgebnis.get(zyklusArrayWertErgebnis.size() - 2)) - 1; i++) {
                        mittelwertArrayZeit.add(Double.valueOf(zyklusArrayZeitErgebnis.get(zyklusArrayWertErgebnis.size() - 3) + j));
                        j++;
                    }

                    // Extrahiere die Werte der Senkung in gerichteten Daten
                    ArrayList<Double> senkungsArrayGerichteteWerte = new ArrayList<>();

                    for (int i = zyklusArrayZeitErgebnis.get(zyklusArrayWertErgebnis.size() - 2) - 1; i <= (zyklusArrayZeitErgebnis.get(zyklusArrayWertErgebnis.size() - 1)); i++) {
                        //System.out.println(zyklusArrayInput.get(i + 1));
                        senkungsArrayGerichteteWerte.add(gerichteterInput.get(i + 1));
                    }

                    // Extrahiere die Werte der Senkung in Rohdaten
                    ArrayList<Double> senkungsArrayRohWerte = new ArrayList<>();

                    for (int i = zyklusArrayZeitErgebnis.get(zyklusArrayWertErgebnis.size() - 2) - 1; i <= (zyklusArrayZeitErgebnis.get(zyklusArrayWertErgebnis.size() - 1)); i++) {
                        //System.out.println(zyklusArrayInput.get(i + 1));
                        senkungsArrayRohWerte.add(rohDaten.get(i + 1));
                    }

                    // Extrahiere die Zeitpunkte der Senkung
                    ArrayList<Double> senkungsArrayZeit = new ArrayList<>();

                    j = 0; // Zähler für die Zeit
                    for (int i = zyklusArrayZeitErgebnis.get(zyklusArrayWertErgebnis.size() - 2) - 1; i <= (zyklusArrayZeitErgebnis.get(zyklusArrayWertErgebnis.size() - 1)); i++) {
                        senkungsArrayZeit.add(Double.valueOf(zyklusArrayZeitErgebnis.get(zyklusArrayWertErgebnis.size() - 2) - 1 + j));
                        j++;
                    }

                    // Zusammengefassen der Arrays und Rückgabe der Liste
//                    System.out.println(Arrays.asList(steigungsArrayGerichteteWerte, steigungsArrayRohWerte, steigungsArrayZeit,
//                            mittelwertArrayGerichteteWerte, mittelwertArrayRohWerte, mittelwertArrayZeit,
//                            senkungsArrayGerichteteWerte, senkungsArrayRohWerte, senkungsArrayZeit));
                    List<ArrayList<Double>> ergebnis = new ArrayList<>();
                    ergebnis.add(steigungsArrayGerichteteWerte);
                    ergebnis.add(steigungsArrayRohWerte);
                    ergebnis.add(steigungsArrayZeit);
                    ergebnis.add(mittelwertArrayGerichteteWerte);
                    ergebnis.add(mittelwertArrayRohWerte);
                    ergebnis.add(mittelwertArrayZeit);
                    ergebnis.add(senkungsArrayGerichteteWerte);
                    ergebnis.add(senkungsArrayRohWerte);
                    ergebnis.add(senkungsArrayZeit);
                    return ergebnis;
                }

            }
        }
        return null;
    }

}