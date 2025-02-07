package FredagsOpgave2;

import java.io.*;
import java.net.Socket;
import java.util.Scanner;

public class SimpleClient {
    private Socket clientSocket;
    private PrintWriter out;
    private BufferedReader in;

    public void startConnection(String ip, int port) {
        try {
            clientSocket = new Socket(ip, port);
            out = new PrintWriter(clientSocket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            System.out.println("Forbundet til serveren.");
        } catch (IOException e) {
            throw new RuntimeException("Kunne ikke forbinde til serveren", e);
        }
    }

    public void startInteractiveSession() {
        Scanner scanner = new Scanner(System.in);


        while (true) {
            String userInput = scanner.nextLine();

            if (userInput.equalsIgnoreCase("stop")) {
                out.println("stop");
                break;
            }

            // Send brugerens input til serveren
            out.println(userInput);
            out.flush();

            // Læs og udskriv serverens respons
            try {
                String serverResponse;
                while ((serverResponse = in.readLine()) != null) {
                    System.out.println("Server: " + serverResponse);
                    if (serverResponse.isEmpty()) {
                        break; // Stop med at læse, når vi når slutningen af HTTP-responsen
                    }
                }
            } catch (IOException e) {
                System.out.println("Fejl ved læsning af serverrespons: " + e.getMessage());
            }
        }

        stopConnection();
        scanner.close();
    }

    public void stopConnection() {
        try {
            in.close();
            out.close();
            clientSocket.close();
            System.out.println("Forbindelsen lukket.");
        } catch (IOException e) {
            throw new RuntimeException("Fejl ved lukning af forbindelsen", e);
        }
    }

    public static void main(String[] args) {
        SimpleClient client = new SimpleClient();
        client.startConnection("127.0.0.1", 8080);
        client.startInteractiveSession();
    }
}
