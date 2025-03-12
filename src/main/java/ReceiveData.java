import Management.InitPipeline;
import Management.SystemManager;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;

public class ReceiveData {
    // Objekte
    private SystemManager systemManager;    // Systemmanager

    // Variablen
    private int dataPort;

    public ReceiveData(SystemManager systemManager, int dataPort) {
        this.dataPort = dataPort;
        this.systemManager = systemManager;
    }

    // Hauptmethode zum Empfangen von Daten
    public void receiveDatafromClient() {

        try (ServerSocket serverSocket = new ServerSocket(dataPort)) {
            System.out.println("Server läuft und wartet auf Verbindungen...");

            while (true) {
                // Auf eingehende Verbindungen warten
                try {
                    Socket socket = serverSocket.accept();
                    //System.out.println("Verbindung akzeptiert: " + socket.getRemoteSocketAddress());

                    // Für jede Verbindung einen neuen Thread starten
                    new Thread(() -> handleClient(socket)).start();
                } catch (IOException e) {
                    System.err.println("Fehler bei der Verbindung: " + e.getMessage());
                }
            }
        } catch (IOException e) {
            System.err.println("Fehler beim Starten des Servers: " + e.getMessage());
        }
    }

    // Methode, die den Input für den Client verarbeitet
    private void handleClient(Socket socket) {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
            String line;
            // Lies die Daten vom Client und verarbeite sie
            while ((line = reader.readLine()) != null) {
                //System.out.println("Empfangene Nachricht: " + line); // Debug-Ausgabe
                // Konvertiere die empfangenen Daten und speichere sie im Sensormanagement.Datenspeicher Array
//                System.out.println("Input: " + line);
//                System.out.println();
//                datenspeicher.setInputData(Double.valueOf(line.replace(",",".")));
//                datenspeicher.start();
                systemManager.addRawData(line);
            }
        } catch (IOException e) {
            System.err.println("Fehler beim Verarbeiten der Client-Verbindung: " + e.getMessage());
        }
    }

//    private void handleClient(Socket socket) {
//        try (BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
//            String line;
//            // Lies die Daten vom Client und verarbeite sie
//            while ((line = reader.readLine()) != null) {
//                try {
//                    // Konvertiere die empfangenen Daten und prüfe auf negative Werte
//                    Double value = Double.valueOf(line.replace(",", "."));
//                    if (value >= 0) {
//                        // Nur positive Werte verarbeiten
//                        datenspeicher.setInputData(value); // Werte speichern
//                        datenspeicher.start();
//                    } else {
//                        //System.out.println("Negativer Wert übersprungen: " + value);
//                    }
//                } catch (NumberFormatException e) {
//                    //System.err.println("Ungültiges Zahlenformat empfangen: " + line);
//                }
//            }
//        } catch (IOException e) {
//            System.err.println("Fehler beim Verarbeiten der Client-Verbindung: " + e.getMessage());
//        }
//    }

}