package Segmentation;

import java.util.ArrayList;
import java.util.List;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class Zyklen_Speicher {

    // Variablen
    private List<List<List>> instanzList = new ArrayList<>(); // Liste zur Speicherung der Instanzen
    private int anzahlInstnanzen;  // Anzahl der Instanzen
    private List<ArrayList<Integer>> instanzZyklusAktive = new ArrayList<>(); // Array zur Speicherung der Intervalle
    private int toleranceStart; // Toleranz für die Intervalle

    // Konstruktor
    public Zyklen_Speicher(int anzahlInstanzen, int toleranceStart){
        // Inizalisierung der speichernden Arrays der Instanzen
        for (int i = 0; i < anzahlInstanzen; i++) {
            List<List> instanz = new ArrayList<>();
            instanzList.add(instanz);

            List<ArrayList> zyklusInstanz = new ArrayList<>();
            instanzList.get(i).add(zyklusInstanz);

            instanzZyklusAktive.add(new ArrayList<>());
        }
        this.anzahlInstnanzen = anzahlInstanzen;
        this.toleranceStart = toleranceStart;
    }

    // Methode zum Überprüfen auf gleiche Intervalle der Instanzen
    private final Map<Integer, AtomicInteger> activeMatches = new ConcurrentHashMap<>(); // Speichert die aktiven Matchesprivate final Map<Integer, AtomicInteger> activeMatches = new ConcurrentHashMap<>(); // Speichert die aktiven Matches

    public List<List<List>> checkIntervallFromAllInstanzes(int sensorId) {
        List<List<List>> result = new ArrayList<>();
        List<Integer> lastValues = new ArrayList<>();

        // Speichere die letzten Werte der Listen
        for (List<Integer> list : instanzZyklusAktive) {
            if (!list.isEmpty()) {
                lastValues.add(list.get(list.size() - 1)); // Letzter Wert jeder Liste
            } else {
                lastValues.add(null); // Falls eine Liste leer ist, speichere null
            }
        }

        Set<Integer> matchingIndices = new HashSet<>();

        // Überprüfe, welche letzten Werte innerhalb der Toleranz liegen
        for (int j = 0; j < lastValues.size(); j++) {
            if (j != sensorId && lastValues.get(j) != null && Math.abs(lastValues.get(sensorId) - lastValues.get(j)) <= toleranceStart) {
                matchingIndices.add(j); // Füge passenden Index hinzu
            }
        }

        // Falls keine Übereinstimmung existiert, gebe sofort zurück
        if (matchingIndices.isEmpty()) {
//            System.out.println("Kein Match gefunden für Sensor " + sensorId + ". Gebe sofort zurück.");
            for (int i = 0; i < instanzZyklusAktive.size(); i++) {
                if (i == sensorId) {
                    result.add(instanzList.get(i));
                } else {
                    result.add(null);
                }
            }
//            System.out.println("Result (Kein Match): " + result);
            return result;
        }

        // Synchronisation: Anzahl der aktiven Matches für diese Matching-Gruppe zählen
        synchronized (activeMatches) {
            for (int i : matchingIndices) {
                activeMatches.putIfAbsent(i, new AtomicInteger(0));
                activeMatches.get(i).incrementAndGet();
            }
            activeMatches.putIfAbsent(sensorId, new AtomicInteger(0));
            activeMatches.get(sensorId).incrementAndGet();
        }

//        System.out.println("Beteiligte Matches für Sensor " + sensorId + ": " + matchingIndices);

        // Falls dieser Thread der letzte ist, gebe das Ergebnis zurück
        synchronized (activeMatches) {
            for (int i = 0; i < instanzZyklusAktive.size(); i++) {
                if (i == sensorId || matchingIndices.contains(i)) {
                    // Verhindere doppelte Einträge in `result`
                    if (!result.contains(instanzList.get(i))) {
                        result.add(instanzList.get(i));
                    }
                } else {
                    result.add(null);
                }
            }

            // Reduziere den Zähler für jeden Sensor in der Match-Gruppe
            for (int i : matchingIndices) {
                if (activeMatches.get(i).decrementAndGet() == 0) {
                    activeMatches.remove(i); // Entferne den Eintrag, wenn alle Threads fertig sind
                }
            }

            if (activeMatches.get(sensorId).decrementAndGet() == 0) {
                activeMatches.remove(sensorId);
//                System.out.println("Letzter beteiligter Thread erreicht für Sensor " + sensorId + ", gebe Ergebnis zurück.");
//                System.out.println("Final Result: " + result);
                return result;
            }
        }

        return null; // Falls der letzte Thread noch nicht erreicht wurde, gebe nichts zurück
    }

    // Setter und Getter
    // Methode zum Hinzufügen von Werten zu einer Instanz
    public void addArraysToZyklusinstanz(int instanzID, List<ArrayList<Double>> zyklusInstanz){
        // Löschen der alten Arrays
        instanzList.get(instanzID).clear();
        // Hinzufügen der einzelnen Arrays zum InstanzArray
        for (ArrayList<Double> array : zyklusInstanz){
            instanzList.get(instanzID).add(array);
        }
    }

    public void setInstanzZyklusAktive(int instanzID, int zyklusZeitpunkt){
        instanzZyklusAktive.get(instanzID).add(zyklusZeitpunkt);
    }

}
