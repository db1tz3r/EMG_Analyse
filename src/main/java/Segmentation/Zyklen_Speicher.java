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
    private int anzahlInstanzen; // Anzahl der Instanzen

    // Konstruktor
    public Zyklen_Speicher(int anzahlInstanzen, int toleranceStart) {
        for (int i = 0; i < anzahlInstanzen; i++) {
            instanzList.add(new ArrayList<>());
            instanzZyklusAktive.add(new ArrayList<>());
            lokaleDatenSpeicher.add(new HashMap<>());
        }
        this.toleranceStart = toleranceStart;
        this.anzahlInstanzen = anzahlInstanzen;
    }

    // Methode zum Starten des Matchings
    public List<List<List<List<Double>>>> startMatching() {
        List<List<List<Double>>> initialMatches = replaceEmptyWithNull(convertToDoubleList(findMatchStartpunkt()));
        List<List<List<Double>>> checkedMissing = null;
        int maxIterations = fehlendeDatenSpeicher.size();

        for (int i = 0; i < maxIterations; i++) {
            List<List<List<Double>>> currentCheck = replaceEmptyWithNull(convertToDoubleList(checkFehlendeDaten()));
            if (currentCheck == null || currentCheck.isEmpty()) {
                break;
            }
            if (checkedMissing == null) {
                checkedMissing = new ArrayList<>();
            }
            checkedMissing.addAll(currentCheck);
        }

        List<List<List<List<Double>>>> combinedResults = new ArrayList<>();

        if (initialMatches != null) {
            combinedResults.add(initialMatches);
        }
        if (checkedMissing != null) {
            combinedResults.add(checkedMissing);
        }
        Iterator<Map.Entry<Integer, List<Double>>> iterator = startzeitpunkte.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<Integer, List<Double>> entry = iterator.next();
            int instanzID = entry.getKey();
            List<Double> starts = new ArrayList<>(entry.getValue()); // Kopie der Liste, um direkte Modifikation zu vermeiden

            for (double start : starts) {
                combinedResults.add(getStartzeitpunktDaten(instanzID, start));
//                System.out.println("Startzeitpunkte: " + startzeitpunkte);
            }

            // Falls alle Startzeitpunkte entfernt wurden, lösche die gesamte Instanz
            if (entry.getValue().isEmpty()) {
                iterator.remove(); // Entfernt den Eintrag sicher aus `startzeitpunkte`
            }
        }

        // Entferne äußere Listen, die nur `null` enthalten
        combinedResults.removeIf(list -> list == null || list.stream().allMatch(Objects::isNull));

        // Falls nach der Bereinigung die Liste leer ist, gib `null` zurück
        return combinedResults.isEmpty() ? null : combinedResults;
    }

    private List<List<List<Double>>> getStartzeitpunktDaten(int instanzID, double start) {
//        System.out.println("Hole Startzeitpunkt für Instanz " + instanzID + " mit Startzeitpunkt " + start);

        // Überprüfung, ob Instanz vorhanden ist
        if (lokaleDatenSpeicher.get(instanzID) == null) {
//            System.out.println("Instanz " + instanzID + " existiert nicht." + lokaleDatenSpeicher.get(instanzID).get(start));
            return null; // Falls die Instanz nicht existiert, gebe null zurück.
        }

        // Überprüfung, ob Startzeitpunkt existiert
        if (lokaleDatenSpeicher.get(instanzID).get(start) == null) {
//            System.out.println("Lokale Daten für Instanz " + instanzID + " mit Startzeitpunkt " + start + " existieren nicht." + lokaleDatenSpeicher.get(instanzID).get(start));
            return null; // Falls der Startzeitpunkt nicht existiert, gebe null zurück.
        }
//        System.out.println("Lokale Daten für Instanz " + instanzID + " mit Startzeitpunkt " + start + " existieren." + lokaleDatenSpeicher.get(instanzID).get(start));

        // Formatieren des Outputs
        List<List<List<Double>>> ergebnis = new ArrayList<>();
        for (int i = 0; i < anzahlInstanzen; i++) {
            if (i == instanzID) {
                System.out.println(lokaleDatenSpeicher.get(instanzID).get(start));
                ergebnis.add(Collections.unmodifiableList(lokaleDatenSpeicher.get(instanzID).get(start)));
            } else {
                ergebnis.add(null);
            }
        }

        // Entferne den Startzeitpunkt aus dem Speicher
        startzeitpunkte.get(instanzID).remove(start);
        lokaleDatenSpeicher.get(instanzID).remove(start);

        return ergebnis;
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
//            System.out.println("\u001B[34mVerarbeite Match für Prozess: " + entry.getKey() + "\u001B[0m");
//            System.out.println("Instanzen und erwartete Startzeitpunkte: " + entry.getValue());
            List<List> matchData = new ArrayList<>(Collections.nCopies(lokaleDatenSpeicher.size(), null));
            boolean allDataAvailable = true;
            List<Integer> fehlendeInstanzen = new ArrayList<>();

            for (Map.Entry<Integer, Double> instanzEntry : entry.getValue().entrySet()) {
                int instanzID = instanzEntry.getKey();
                double specificStart = instanzEntry.getValue();
//                System.out.println("Verfügbare Keys für Instanz " + instanzID + ": " + lokaleDatenSpeicher.get(instanzID).keySet());
                if (!lokaleDatenSpeicher.get(instanzID).containsKey(roundDouble(specificStart))) {
//                    System.out.println("Fehlende Datenprüfung: Instanz " + instanzID + " hat nicht den Identifier " + specificStart);
//                    System.out.println("Verfügbare Keys für Instanz " + instanzID + ": " + lokaleDatenSpeicher.get(instanzID).keySet());
                    allDataAvailable = false;
                    fehlendeInstanzen.add(instanzID);
                }
            }

            if (!allDataAvailable) {
//                System.out.println("\u001B[31mÜberspringe Prozess für " + entry.getKey() + " wegen missenden Daten.\u001B[0m");
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
//                System.out.println("\u001B[32mErfolgreich Daten erhalten von Instanz " + instanzID + " with Identifier " + specificStart + "\u001B[0m");
//                System.out.println("Daten: " + matchData.get(instanzID));
//                System.out.println("Lösche Key: " + specificStart + " von Instanz " + instanzID);
                lokaleDatenSpeicher.get(instanzID).remove(roundDouble(specificStart));
//                System.out.println("Lösche key: " + specificStart + " von Instanz " + instanzID);
            }

            combinedResults.add(matchData);
            iterator.remove();
//            System.out.println("Fertiger Prozess für Instanz: " + entry.getKey());
//            System.out.println("Aktuelle Kombinierter Output: " + combinedResults);
        }
        return combinedResults;
    }

    // Hilfsmethode zum Ersetzen leerer Listen durch null
    private List<List<List<Double>>> replaceEmptyWithNull(List<List<List<Double>>> list) {
        if (list == null || list.isEmpty()) {
            return null;
        }

        List<List<List<Double>>> cleanedList = new ArrayList<>();
        for (List<List<Double>> match : list) {
            if (match == null || match.isEmpty()) {
                cleanedList.add(null);
            } else {
                List<List<Double>> cleanedMatch = new ArrayList<>();
                for (List<Double> instanz : match) {
                    cleanedMatch.add((instanz == null || instanz.isEmpty()) ? null : instanz);
                }
                cleanedList.add(cleanedMatch);
            }
        }
        return cleanedList;
    }

    public List<List<List<Double>>> convertToDoubleList(List<List<List>> rawList) {
//        if (!rawList.isEmpty()) {
//            System.out.println("Konvertiere zu Double-Liste...");
//            System.out.println("Raw List: " + rawList);
//        }

        List<List<List<Double>>> convertedMatch = null;
        for (List<List> match : rawList) {

            convertedMatch = new ArrayList<>();

            for (List<?> instanz : match) {
                if (instanz == null) {
                    convertedMatch.add(null);
                    continue;
                }

                List<List<Double>> instanzDaten = new ArrayList<>();

                for (Object obj : instanz) {
                    if (obj instanceof List<?>) {
                        List<Double> nestedList = new ArrayList<>();
                        for (Object nestedObj : (List<?>) obj) {
                            if (nestedObj instanceof Double) {
                                nestedList.add((Double) nestedObj);
                            } else if (nestedObj instanceof Number) {
                                nestedList.add(((Number) nestedObj).doubleValue());
                            } else {
                                System.out.println("⚠ WARNUNG: Unerwarteter Typ in verschachtelter Liste: " + nestedObj.getClass().getSimpleName() + " → " + nestedObj);
                            }
                        }
                        instanzDaten.add(nestedList);
                    } else if (obj instanceof Double) {
                        instanzDaten.add(Collections.singletonList((Double) obj));
                    } else if (obj instanceof Number) {
                        instanzDaten.add(Collections.singletonList(((Number) obj).doubleValue()));
                    } else {
                        System.out.println("⚠ WARNUNG: Unerwarteter Typ in der Liste: " + obj.getClass().getSimpleName() + " → " + obj);
                    }
                }

                // Hier wird sichergestellt, dass die Originalstruktur erhalten bleibt!
                convertedMatch.add(instanzDaten.isEmpty() ? null : instanzDaten);
            }
        }

//        System.out.println("Converted List: " + convertedMatch);
        return convertedMatch;
    }

    private double roundDouble(double value) {
        return new BigDecimal(value).setScale(5, RoundingMode.HALF_UP).doubleValue();
    }


    //Setter und Getter
    // Methode zum Hinzufügen eines Startzeitpunkts zu einer Instanz
    public void setStartzeitpunkt(int ID, double Startzeitpunkt) {
        startzeitpunkte.putIfAbsent(ID, new ArrayList<>());
        startzeitpunkte.get(ID).add(Startzeitpunkt);
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
}