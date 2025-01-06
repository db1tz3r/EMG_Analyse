package RandomForest;

import smile.classification.RandomForest;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;


public class ModellManager {
    // Example usage
    public static void main(String[] args) {
        try {
            String csvPath = "C:/Users/bitzer/IdeaProjects/EMG_Analyse_Server/Data/rfiris.csv";

            //Initalisieren und Starten des Model-Trainings
            DataTraining dataTraining = new DataTraining();

            RandomForest trainedModel = dataTraining.trainRandomForest(csvPath);


            //Starten der Vorhersage mit dem trainieren Modell
            if (trainedModel != null) {
                System.out.println("Random Forest Model is ready for predictions!");

                //initalisieren der LiveDataPrediction
                LiveDataPrediction liveDataPrediction = new LiveDataPrediction();

                // Live-Daten simulieren
                ArrayBlockingQueue<Object> liveDataQueue = new ArrayBlockingQueue<>(10);
                LiveDataPrediction.simulateLiveData(liveDataQueue);

                // Vorhersagen basierend auf Live-Daten
                LiveDataPrediction.predictLiveData(trainedModel, liveDataQueue);

            } else {
                System.out.println("Failed to train the model.");
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
}
