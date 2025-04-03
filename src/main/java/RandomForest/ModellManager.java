package RandomForest;

import smile.classification.RandomForest;

import java.util.concurrent.ArrayBlockingQueue;

public class ModellManager implements Runnable {

    private final String csvPath;
    private final ArrayBlockingQueue<Object> liveDataQueue;

    public ModellManager(String csvPath, ArrayBlockingQueue<Object> liveDataQueue) {
        this.csvPath = csvPath + ".csv";
        this.liveDataQueue = liveDataQueue;
    }

    @Override
    public void run() {
        try {
            RandomForest trainedModel = DataTraining.trainRandomForest(csvPath);

            if (trainedModel != null) {
                System.out.println("✅ Modell erfolgreich trainiert!");
                LiveDataPrediction.predictLiveData(trainedModel, liveDataQueue, csvPath);
            } else {
                System.out.println("❌ Modell konnte nicht trainiert werden.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
