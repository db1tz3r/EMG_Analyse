import java.util.ArrayList;

public class UpdatePlotter implements Runnable {

    private RealTimePlotter plotter;
    private ArrayList<Integer> list1 = new ArrayList<Integer>();
    private ArrayList<Integer> list2 = new ArrayList<Integer>();
    private ArrayList<Integer> list3 = new ArrayList<Integer>();
    int i = 0, j = 0, k = 0;

    // Konstruktor: Die beiden ArrayLists werden übergeben
    public UpdatePlotter(RealTimePlotter plotter) {
        this.plotter = plotter;
    }

    public void run(){
        for (int i = 0; i < list1.size(); i++) {
            plotter.addRawData(list1.get(i), i);
        }
        for (int j = 0; j < list2.size(); j++) {
            plotter.addRmsData(list2.get(j), j);
        }
        for (int k = 0; k < list3.size(); k++) {
            plotter.addPeakNormalisierungData(list3.get(k), k);
        }
    }

    public void setList1(int list1Value) {
        this.list1.add(list1Value);
    }

    public void setList2(int list2Value) {
        this.list2.add(list2Value);
    }

    public void setList3(int list3Value) {
        this.list3.add(list3Value);
    }
}