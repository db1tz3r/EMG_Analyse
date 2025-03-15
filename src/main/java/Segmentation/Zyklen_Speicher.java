package Segmentation;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class Zyklen_Speicher {

    // Variablen
    private List<List<List>> instanzList = new ArrayList<>(); // Liste zur Speicherung der Instanzen
    private List<ArrayList<Integer>> instanzZyklusAktive = new ArrayList<>(); // Array zur Speicherung der Intervalle
    private int toleranceStart; // Toleranz für die Intervalle
    private Map<Integer, List<Double>> startzeitpunkte = new ConcurrentHashMap<>(); // Globaler Speicher für Startzeitpunkte
    private List<List<ArrayList<Double>>> lokaleDatenSpeicher = new ArrayList<>(); // Globaler Speicher für lokaleDaten

    // Konstruktor
    public Zyklen_Speicher(int anzahlInstanzen, int toleranceStart) {
        for (int i = 0; i < anzahlInstanzen; i++) {
            instanzList.add(new ArrayList<>());
            instanzZyklusAktive.add(new ArrayList<>());
            lokaleDatenSpeicher.add(new ArrayList<>());
        }
        this.toleranceStart = toleranceStart;
    }

    // Methode zum Hinzufügen von neuen Daten zu lokaleDatenSpeicher
    public void addLokaleDaten(List<List<ArrayList<Double>>> neueDaten) {
        for (int i = 0; i < neueDaten.size(); i++) {
            if (i >= lokaleDatenSpeicher.size()) {
                lokaleDatenSpeicher.add(new ArrayList<>());
            }
            lokaleDatenSpeicher.get(i).addAll(neueDaten.get(i));
        }
    }

    // Methode zum Überprüfen auf gleiche Intervalle der Instanzen
    public List<List<List>> checkIntervallFromAllInstanzes(List<List<ArrayList<Double>>> lokaleDaten) {
        List<List<List>> result = new ArrayList<>(Collections.nCopies(instanzZyklusAktive.size(), null));
        boolean allInstancesNullOrEmpty = true;

        for (int i = 0; i < instanzZyklusAktive.size(); i++) {
            if (lokaleDaten.get(i) != null && !lokaleDaten.get(i).isEmpty()) {
                result.set(i, new ArrayList<>(lokaleDaten.get(i)));
                allInstancesNullOrEmpty = false;
            }
        }

        if (allInstancesNullOrEmpty) {
            return new ArrayList<>();
        }

        Map<Double, List<Integer>> valueToInstances = new HashMap<>();
        List<String> matchResults = new ArrayList<>();

        for (int i = 0; i < result.size(); i++) {
            if (result.get(i) != null && result.get(i).size() > 3 && result.get(i).get(3).size() > 0) {
                double value = (double) result.get(i).get(3).get(0);

                for (Map.Entry<Double, List<Integer>> entry : valueToInstances.entrySet()) {
                    if (Math.abs(entry.getKey() - value) <= toleranceStart && !entry.getValue().contains(i)) {
                        entry.getValue().add(i);
                        matchResults.add("Übereinstimmung gefunden für Wert: " + value + " mit Toleranz: " + toleranceStart + " Beteiligte Instanzen: " + entry.getValue());
                    }
                }

                valueToInstances.putIfAbsent(value, new ArrayList<>());
                valueToInstances.get(value).add(i);
            }
        }

        for (String match : matchResults) {
            System.out.println(match);
        }

        return result;
    }

    // Methode zum Hinzufügen eines Startzeitpunkts zu einer Instanz
    public void setStartzeitpunkt(int ID, double Startzeitpunkt) {
        startzeitpunkte.putIfAbsent(ID, new ArrayList<>());
        startzeitpunkte.get(ID).add(Startzeitpunkt);
    }

    // Getter-Methode für Startzeitpunkte
    public List<Double> getStartzeitpunkte(int ID) {
        return startzeitpunkte.getOrDefault(ID, new ArrayList<>());
    }

    // Methode zur Überprüfung, ob mehrere Startzeitpunkte innerhalb der Toleranz übereinstimmen und sie anschließend zu entfernen
    public List<String> findMatchStartpunkt() {
        Map<Double, List<Integer>> matchedInstances = new HashMap<>();
        List<String> matchResults = new ArrayList<>();
        List<Integer> instanzKeys = new ArrayList<>(startzeitpunkte.keySet());

        Set<Double> toRemove = new HashSet<>(); // Speichert Werte, die nach dem Match entfernt werden

        for (int i = 0; i < instanzKeys.size(); i++) {
            int instanz1 = instanzKeys.get(i);
            List<Double> starts1 = startzeitpunkte.get(instanz1);
            if (starts1 == null) continue;

            for (double start1 : starts1) {
                for (int j = i + 1; j < instanzKeys.size(); j++) {
                    int instanz2 = instanzKeys.get(j);
                    List<Double> starts2 = startzeitpunkte.get(instanz2);
                    if (starts2 == null) continue;

                    for (double start2 : starts2) {
                        if (Math.abs(start1 - start2) <= toleranceStart) {
                            matchedInstances.putIfAbsent(start1, new ArrayList<>());
                            if (!matchedInstances.get(start1).contains(instanz1)) {
                                matchedInstances.get(start1).add(instanz1);
                            }
                            if (!matchedInstances.get(start1).contains(instanz2)) {
                                matchedInstances.get(start1).add(instanz2);
                            }

                            // Markiere beide Werte zum Entfernen
                            toRemove.add(start1);
                            toRemove.add(start2);
                        }
                    }
                }
            }
        }

        // Entferne die gefundenen Werte aus dem Speicher, nachdem alle Iterationen abgeschlossen sind
        for (int instanzID : startzeitpunkte.keySet()) {
            startzeitpunkte.get(instanzID).removeIf(toRemove::contains);
        }

        for (Map.Entry<Double, List<Integer>> entry : matchedInstances.entrySet()) {
            matchResults.add("Startzeitpunkt-Match gefunden für Wert: " + entry.getKey() +
                    " mit Instanzen: " + entry.getValue() + " innerhalb der Toleranz " + toleranceStart);
        }

        for (String match : matchResults) {
            System.out.println(match);
        }

        return matchResults;
    }
}
