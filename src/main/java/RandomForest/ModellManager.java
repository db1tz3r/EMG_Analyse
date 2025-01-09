package RandomForest;

import smile.classification.RandomForest;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class ModellManager implements Runnable {

    // Variablen festlegen
    private String csvPath;

    // Konstruktor
    public ModellManager(String csvPath) {
        this.csvPath = csvPath + ".csv";
    }



    @Override
    public void run() {
        try {
            // Initialisieren und Starten des Model-Trainings
            DataTraining dataTraining = new DataTraining();

            RandomForest trainedModel = dataTraining.trainRandomForest(csvPath);

            // Starten der Vorhersage mit dem trainierten Modell
            if (trainedModel != null) {
                System.out.println("Random Forest Model is ready for predictions!");

                // Initialisieren der LiveDataPrediction
                LiveDataPrediction liveDataPrediction = new LiveDataPrediction();

                // Live-Daten simulieren
                ArrayBlockingQueue<Object> liveDataQueue = new ArrayBlockingQueue<>(10);
                LiveDataPrediction.simulateLiveData(liveDataQueue);

                // Vorhersagen basierend auf Live-Daten
                LiveDataPrediction.predictLiveData(trainedModel, liveDataQueue, csvPath);

            } else {
                System.out.println("Failed to train the model.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}