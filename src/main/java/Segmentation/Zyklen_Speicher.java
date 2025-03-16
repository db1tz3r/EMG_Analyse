package Segmentation;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class Zyklen_Speicher {

    // Variablen
    private List<Map<Double, List<ArrayList<Double>>>> lokaleDatenSpeicher = new ArrayList<>(); // Globaler Speicher für lokaleDaten
    private List<List<List>> instanzList = new ArrayList<>(); // Liste zur Speicherung der Instanzen
    private List<ArrayList<Integer>> instanzZyklusAktive = new ArrayList<>(); // Array zur Speicherung der Intervalle
    private int toleranceStart; // Toleranz für die Intervalle
    private Map<Integer, List<Double>> startzeitpunkte = new ConcurrentHashMap<>(); // Globaler Speicher für Startzeitpunkte
    private Map<Double, Map<Integer, Double>> fehlendeDatenSpeicher = new ConcurrentHashMap<>(); // Speicher für unvollständige Matches (Startzeitpunkt-spezifisch)

    // Konstruktor
    public Zyklen_Speicher(int anzahlInstanzen, int toleranceStart) {
        for (int i = 0; i < anzahlInstanzen; i++) {
            instanzList.add(new ArrayList<>());
            instanzZyklusAktive.add(new ArrayList<>());
            lokaleDatenSpeicher.add(new HashMap<>());
        }
        this.toleranceStart = toleranceStart;
    }

    // Methode zum Hinzufügen von neuen Daten zu lokaleDatenSpeicher
    public void addLokaleDaten(List<List<ArrayList<Double>>> neueDaten) {
        for (int i = 0; i < neueDaten.size(); i++) {
            if (i >= lokaleDatenSpeicher.size()) {
                lokaleDatenSpeicher.add(new HashMap<>());
            }
            List<ArrayList<Double>> daten = neueDaten.get(i);
            if (daten.size() > 2 && daten.get(2).size() > 0) {
                double identifier = roundDouble(daten.get(2).get(0));
                lokaleDatenSpeicher.get(i).put(identifier, daten);
//                System.out.println("Daten hinzugefügt für Instanz " + i + " mit Identifier " + identifier);
            }
        }
    }

    // Methode zur Überprüfung, ob mehrere Startzeitpunkte innerhalb der Toleranz übereinstimmen und sie anschließend zu entfernen
    public List<List<List>> findMatchStartpunkt() {
//        System.out.println("Starte findMatchStartpunkt...");
        Map<Double, Map<Integer, Double>> matchedInstances = new HashMap<>();
        List<List<List>> combinedResults = new ArrayList<>();
        List<Integer> instanzKeys = new ArrayList<>(startzeitpunkte.keySet());

//        System.out.println("Verfügbare Startzeitpunkte: " + startzeitpunkte);

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
                            matchedInstances.putIfAbsent(roundDouble(start1), new HashMap<>());
                            matchedInstances.get(roundDouble(start1)).put(instanz1, start1);
                            matchedInstances.get(roundDouble(start1)).put(instanz2, start2);
                            System.out.println("\u001B[32mMatch gefunden: " + start1 + " mit Instanzen: " + matchedInstances.get(roundDouble(start1)) + "\u001B[0m");

                            // Markiere beide Werte zum Entfernen
                            toRemove.add(roundDouble(start1));
                            toRemove.add(roundDouble(start2));
                        }
                    }
                }
            }
        }

        for (int instanzID : startzeitpunkte.keySet()) {
            startzeitpunkte.get(instanzID).removeIf(toRemove::contains);
        }

//        System.out.println("Matched Instances: " + matchedInstances);

        combinedResults = getCombinedMatchData(matchedInstances);
        return combinedResults;
    }

    private List<List<List>> getCombinedMatchData(Map<Double, Map<Integer, Double>> matchedInstances) {
        return processMatchData(matchedInstances, false);
    }

    public List<List<List>> checkFehlendeDaten() {
//        System.out.println("Prüfe erneut auf fehlende Daten...");
        Iterator<Map.Entry<Double, Map<Integer, Double>>> iterator = fehlendeDatenSpeicher.entrySet().iterator();
        Map<Double, Map<Integer, Double>> neuGefundeneDaten = new HashMap<>();

        while (iterator.hasNext()) {
            Map.Entry<Double, Map<Integer, Double>> entry = iterator.next();
            boolean alleDatenJetztVerfügbar = true;

            for (Map.Entry<Integer, Double> instanzEntry : entry.getValue().entrySet()) {
                int instanzID = instanzEntry.getKey();
                double specificStart = instanzEntry.getValue();

                // Prüfen, ob die Daten inzwischen vorhanden sind
                if (instanzID >= lokaleDatenSpeicher.size() ||
                        !lokaleDatenSpeicher.get(instanzID).containsKey(roundDouble(specificStart))) {
                    alleDatenJetztVerfügbar = false;
                    break;
                }
            }

            // Falls alle Daten nun vorhanden sind, fügen wir sie zur Verarbeitung hinzu
            if (alleDatenJetztVerfügbar) {
                neuGefundeneDaten.put(entry.getKey(), entry.getValue());
                iterator.remove(); // Entferne die Daten aus dem Speicher
            }
        }

        return processMatchData(neuGefundeneDaten, true);
    }

    private List<List<List>> processMatchData(Map<Double, Map<Integer, Double>> matchedInstances, boolean isCheckingMissing) {
        List<List<List>> combinedResults = new ArrayList<>();
        Iterator<Map.Entry<Double, Map<Integer, Double>>> iterator = matchedInstances.entrySet().iterator();

        while (iterator.hasNext()) {
            Map.Entry<Double, Map<Integer, Double>> entry = iterator.next();
            System.out.println("\u001B[34mVerarbeite Match für Prozess: " + entry.getKey() + "\u001B[0m");
            System.out.println("Instanzen und erwartete Startzeitpunkte: " + entry.getValue());
            List<List> matchData = new ArrayList<>(Collections.nCopies(lokaleDatenSpeicher.size(), null));
            boolean allDataAvailable = true;
            List<Integer> fehlendeInstanzen = new ArrayList<>();

            for (Map.Entry<Integer, Double> instanzEntry : entry.getValue().entrySet()) {
                int instanzID = instanzEntry.getKey();
                double specificStart = instanzEntry.getValue();
                System.out.println("Verfügbare Keys für Instanz " + instanzID + ": " + lokaleDatenSpeicher.get(instanzID).keySet());
                if (!lokaleDatenSpeicher.get(instanzID).containsKey(roundDouble(specificStart))) {
//                    System.out.println("Fehlende Datenprüfung: Instanz " + instanzID + " hat nicht den Identifier " + specificStart);
//                    System.out.println("Verfügbare Keys für Instanz " + instanzID + ": " + lokaleDatenSpeicher.get(instanzID).keySet());
                    allDataAvailable = false;
                    fehlendeInstanzen.add(instanzID);
                }
            }

            if (!allDataAvailable) {
                System.out.println("\u001B[31mÜberspringe Prozess für " + entry.getKey() + " wegen missenden Daten.\u001B[0m");
//                System.out.println("Daten fehlen für Identifier " + entry.getKey() + " von Instanzen: " + fehlendeInstanzen);
//                System.out.println("Fehlende Daten: " + fehlendeDatenSpeicher);
//                System.out.println("Verfügbare Keys für Instanz " + fehlendeInstanzen.get(0) + ": " + lokaleDatenSpeicher.get(fehlendeInstanzen.get(0)).keySet());
//                System.out.println("Verfügbare Keys für Instanz " + fehlendeInstanzen.getLast() + ": " + lokaleDatenSpeicher.get(fehlendeInstanzen.getLast()).keySet());
//                System.out.println("Lokale Daten: " + lokaleDatenSpeicher);
                if (!isCheckingMissing) {
                    fehlendeDatenSpeicher.put(entry.getKey(), entry.getValue());
                }
                continue;
            }

            for (Map.Entry<Integer, Double> instanzEntry : entry.getValue().entrySet()) {
                int instanzID = instanzEntry.getKey();
                double specificStart = instanzEntry.getValue();
                matchData.set(instanzID, new ArrayList<>(lokaleDatenSpeicher.get(instanzID).get(roundDouble(specificStart))));
                System.out.println("\u001B[32mErfolgreich Daten erhalten von Instanz " + instanzID + " with Identifier " + specificStart + "\u001B[0m");
                System.out.println("Lösche Key: " + specificStart + " von Instanz " + instanzID);
                lokaleDatenSpeicher.get(instanzID).remove(roundDouble(specificStart));
                System.out.println("Lösche key: " + specificStart + " von Instanz " + instanzID);
            }

            combinedResults.add(matchData);
            iterator.remove();
            System.out.println("Fertiger Prozess für Instanz: " + entry.getKey());
//            System.out.println("Aktuelle Kombinierter Output: " + combinedResults);
        }
        return combinedResults;
    }

    public List<List<List>> startMatching() {
        List<List<List>> initialMatches = findMatchStartpunkt();
        if (!initialMatches.isEmpty()) {
//            System.out.println("Erste Matching-Ergebnisse verarbeitet.");
        }

        List<List<List>> checkedMissing = checkFehlendeDaten();
        if (!checkedMissing.isEmpty()) {
//            System.out.println("Fehlende Daten wurden gefunden und verarbeitet.");
        }

        // Kombiniere die Ergebnisse und gib sie zurück
        List<List<List>> combinedResults = new ArrayList<>();
        combinedResults.addAll(initialMatches);
        combinedResults.addAll(checkedMissing);
        if (!combinedResults.isEmpty()) {
            System.out.println("Kombinierte Ergebnisse Endergebnis: " + combinedResults);
        }
        return combinedResults;
    }

    //Setter und Getter
    // Methode zum Hinzufügen eines Startzeitpunkts zu einer Instanz
    public void setStartzeitpunkt(int ID, double Startzeitpunkt) {
        startzeitpunkte.putIfAbsent(ID, new ArrayList<>());
        startzeitpunkte.get(ID).add(Startzeitpunkt);
    }

    private double roundDouble(double value) {
        return new BigDecimal(value).setScale(5, RoundingMode.HALF_UP).doubleValue();
    }
}