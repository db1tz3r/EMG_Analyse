package Segmentation;

import java.util.ArrayList;
import java.util.List;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class Zyklen_Speicher {

    // Variablen
    private List<List<List>> instanzList = new ArrayList<>(); // Liste zur Speicherung der Instanzen
    private List<ArrayList<Integer>> instanzZyklusAktive = new ArrayList<>(); // Array zur Speicherung der Intervalle
    private int toleranceStart; // Toleranz für die Intervalle

    // Konstruktor
    public Zyklen_Speicher(int anzahlInstanzen, int toleranceStart) {
        // Initialisierung der speichernden Arrays der Instanzen
        for (int i = 0; i < anzahlInstanzen; i++) {
            List<List> instanz = new ArrayList<>();
            instanzList.add(instanz);

            List<ArrayList> zyklusInstanz = new ArrayList<>();
            instanzList.get(i).add(zyklusInstanz);

            instanzZyklusAktive.add(new ArrayList<>());
        }
        this.toleranceStart = toleranceStart;
    }

    // Methode zum Überprüfen auf gleiche Intervalle der Instanzen
    public List<List<List>> checkIntervallFromAllInstanzes(List<List<ArrayList<Double>>> lokaleDaten) {
        List<List<List>> result = new ArrayList<>(Collections.nCopies(instanzZyklusAktive.size(), null));

        boolean containsRealData = false;
        boolean allInstancesNullOrEmpty = true;

        for (int i = 0; i < instanzZyklusAktive.size(); i++) {
            if (lokaleDaten.get(i) != null && !lokaleDaten.get(i).isEmpty()) {
                result.set(i, new ArrayList<>(lokaleDaten.get(i))); // Kopiere statt Referenz zu setzen
                containsRealData = true;
                allInstancesNullOrEmpty = false;
            }
        }

        // Falls wirklich ALLE Instanzen `null` oder `[]` sind, gebe eine leere Liste zurück
        if (allInstancesNullOrEmpty) {
//            System.out.println("⚠ WARNUNG: Alle Hauptinstanzen sind leer oder null. Ergebnis bleibt leer.");
            return new ArrayList<>(); // Oder `return null;`
        }

//        System.out.println("✅ Erfolgreiche Rückgabe von checkIntervallFromAllInstanzes: " + result);
        return result;
    }

    // Setter und Getter
    // Methode zum Hinzufügen von Werten zu einer Instanz
//    public void addArraysToZyklusinstanz(int instanzID, List<ArrayList<Double>> zyklusInstanz) {
//        // Falls zyklusInstanz `null` ist, speichere eine leere Liste
//        if (zyklusInstanz == null) {
////            System.out.println("⚠ WARNUNG: Zyklus-Instanz für " + instanzID + " ist NULL!");
//            instanzList.set(instanzID, new ArrayList<>());
//            return;
//        }
//
//        // Löschen der alten Arrays
//        instanzList.get(instanzID).clear();
//
//        // Hinzufügen der einzelnen Arrays zum InstanzArray
//        for (ArrayList<Double> array : zyklusInstanz) {
//            instanzList.get(instanzID).add(array);
//        }
//
////        System.out.println("✅ Instanz " + instanzID + " wurde erfolgreich aktualisiert: " + instanzList.get(instanzID));
//    }
}