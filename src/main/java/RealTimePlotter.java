import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import javax.swing.*;
import java.awt.*;

public class RealTimePlotter extends JFrame {

    private XYSeries seriesRaw; // XYSeries, um die rawDaten zu speichern
    private XYSeries seriesRms; // XYSeries, um die RMS-Daten zu speichern
    private XYSeries seriesPeakNormalisierung; // XYSeries, um die RMS-Daten zu speichern
    private double timeVariable; // Präzise Zeitangabe für mit der Berechnung der angegebenen Hz-Anzahl der Datenübertragung

    // Konstruktor
    public RealTimePlotter(int hzInput) {
        //Erstellen des Plotters
        // Titel für das Fenster
        super("Data Plotter");

        // Erstellen einer XYSeries, um Daten zu speichern
        seriesRaw = new XYSeries("Raw-Daten");
        seriesRms = new XYSeries("RMS-Daten");
        seriesPeakNormalisierung = new XYSeries("Peak-Normailsierung-Daten");

        // Hinzufügen der Serien zu einem XYSeriesCollection
        XYSeriesCollection dataset = new XYSeriesCollection();
        dataset.addSeries(seriesRaw);
        dataset.addSeries(seriesRms);
        dataset.addSeries(seriesPeakNormalisierung);

        // Erstellen des Diagramms
        JFreeChart chart = ChartFactory.createXYLineChart(
                "Daten Plotter", // Titel des Diagramms
                "Zeit",          // X-Achsen-Beschriftung
                "Wert",          // Y-Achsen-Beschriftung
                dataset,         // Datenquelle
                PlotOrientation.VERTICAL, // Orientierung des Diagramms
                true,            // Legende anzeigen
                true,            // Tooltips anzeigen
                false            // URLs deaktivieren
        );

        // Erstellen des Panels für das Diagramm
        ChartPanel chartPanel = new ChartPanel(chart);
        chartPanel.setPreferredSize(new java.awt.Dimension(800, 600));

        // Hinzufügen des Panels zum Fenster
        setLayout(new BorderLayout());
        add(chartPanel, BorderLayout.CENTER);


        // Weitere Konstruktor Deklarationen
        // Berechnen der Zeit anhand der Frequenz in der Main Methode
        timeVariable = (double) 1 / hzInput;
    }


    // Methode zum Hinzufügen von Daten
    public void addRawData(double value1, int timeCounter) {
        seriesRaw.add((timeCounter * timeVariable), value1); // Raw-Daten hinzufügen
        repaint(); // Diagramm neu zeichnen
    }

    public void addRmsData(double value2, int timeCounter) {
        seriesRms.add((timeCounter * timeVariable), value2); // RMS-Daten hinzufügen
        repaint(); // Diagramm neu zeichnen
    }

    public void addPeakNormalisierungData(double value3, int timeCounter) {
        seriesPeakNormalisierung.add((timeCounter * timeVariable), value3); // RMS-Daten hinzufügen
        repaint(); // Diagramm neu zeichnen
    }
}