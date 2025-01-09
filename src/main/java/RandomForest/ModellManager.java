package RandomForest;

import smile.classification.RandomForest;
import java.util.concurrent.ArrayBlockingQueue;

public class ModellManager implements Runnable {

    // Variablen festlegen
    private String csvPath;
    private final ArrayBlockingQueue<Object> liveDataQueue;

    // Konstruktor
    public ModellManager(String csvPath, ArrayBlockingQueue<Object> liveDataQueue) {
        this.csvPath = csvPath + ".csv";
        this.liveDataQueue = liveDataQueue;
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