package RandomForest;

import smile.classification.RandomForest;

import java.util.concurrent.ArrayBlockingQueue;

public class ModellManager implements Runnable {

    // Pfad zur CSV-Datei, die für das Training verwendet wird
    private final String csvPath;

    // Warteschlange für Live-Daten, die für Vorhersagen verwendet werden
    private final ArrayBlockingQueue<Object> liveDataQueue;

    // Konstruktor zur Initialisierung des ModellManagers mit dem CSV-Pfad und der Live-Daten-Warteschlange
    public ModellManager(String csvPath, ArrayBlockingQueue<Object> liveDataQueue) {
        this.csvPath = csvPath + ".csv"; // Fügt die Dateiendung .csv hinzu
        this.liveDataQueue = liveDataQueue;
    }

    // Methode, die beim Starten des Threads ausgeführt wird
    @Override
    public void run() {
        try {
            // Trainiert ein RandomForest-Modell basierend auf den Daten in der CSV-Datei
            RandomForest trainedModel = DataTraining.trainRandomForest(csvPath);

            // Überprüft, ob das Modell erfolgreich trainiert wurde
            if (trainedModel != null) {
                System.out.println("✅ Modell erfolgreich trainiert!"); // Erfolgsnachricht
                // Startet die Live-Daten-Vorhersage mit dem trainierten Modell
                LiveDataPrediction.predictLiveData(trainedModel, liveDataQueue, csvPath);
            } else {
                System.out.println("❌ Modell konnte nicht trainiert werden.");
            }
        } catch (Exception e) {
            // Gibt den Fehler aus, falls eine Ausnahme auftritt
            e.printStackTrace();
        }
    }
}