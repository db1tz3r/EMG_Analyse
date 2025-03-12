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

    // Konstruktor
    public InstanzManager(Normalisierung_Manager normalisierungManager, Zyklenmanager zyklenmanager) {
        this.normalisierungManager = normalisierungManager;
        this.zyklenmanager = zyklenmanager;
    }

    // Start-Methode
    public List<List<List>> startPipeline() {
        // Überprüfe, ob rawData gültig ist
        if (rawData == null || rawData.isEmpty()) {
//            System.out.println("Fehler: rawData ist null oder leer!");
            return null;
        }

        // Start der Normalisierung
        ArrayList<Double> ergebnisNormalisierung = normalisierungManager.startNormalisierung(rawData);

        // Überprüfe, ob die Normalisierung ein Ergebnis hat
        if (ergebnisNormalisierung == null || ergebnisNormalisierung.isEmpty()) {
//            System.out.println("Fehler: Ergebnis Normalisierung ist leer!");
            return null;
        }
//        System.out.println("Ergebnis Normalisierung: " + ergebnisNormalisierung);

        // Start der Zyklenerkennung
        List<List<List>> ergebnisZyklen = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            List<List<List>> tempErgebnis = zyklenmanager.startSegmentation(startZyklenerkennungIndex, startZyklenerkennungIndex,
                    7.0, 5, 30.0, 120, 120,
                    ergebnisNormalisierung, rawData);

            if (tempErgebnis != null && !tempErgebnis.isEmpty()) {
                ergebnisZyklen.addAll(tempErgebnis);
            } else {
//                System.out.println("Fehler: startSegmentation() hat in Durchlauf " + i + " keine Werte geliefert.");
            }
            startZyklenerkennungIndex++;
        }

        // Falls keine Zyklen erkannt wurden, gebe `null` zurück
        if (ergebnisZyklen.isEmpty()) {
//            System.out.println("Fehler: Keine Zyklen erkannt.");
            return null;
        }
//        System.out.println(ergebnisZyklen);
        return ergebnisZyklen;
    }










    // Setter und Getter
    public void setInputData(List<Double> inputDataValue) {
        this.rawData.addAll(inputDataValue);
    }
}
