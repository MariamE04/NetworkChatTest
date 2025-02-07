package FredagsOpgave2;

import java.io.*;
import java.net.Socket;
import java.util.Scanner;

public class SimpleClient {
    private Socket clientSocket;  // Socket til at forbinde med serveren
    private PrintWriter out;  // PrintWriter til at sende data til serveren
    private BufferedReader in;  // BufferedReader til at læse serverens svar

    public void startConnection(String ip, int port) {
        try {
            clientSocket = new Socket(ip, port);  // Opret en forbindelse til serveren på den givne IP og port
            out = new PrintWriter(clientSocket.getOutputStream(), true);  // Initialiser PrintWriter til at sende data
            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));  // Initialiser BufferedReader til at læse data
            System.out.println("Forbundet til serveren.");  // Udskriv en besked, når forbindelsen er etableret
        } catch (IOException e) {
            throw new RuntimeException("Kunne ikke forbinde til serveren", e);  // Håndter fejl ved forbindelse
        }
    }

    public void startInteractiveSession() {
        Scanner scanner = new Scanner(System.in);  // Scanner til at læse input fra brugeren

        while (true) {
            String userInput = scanner.nextLine();  // Læs en linje input fra brugeren

            if (userInput.equalsIgnoreCase("stop")) {  // Hvis brugeren skriver "stop", afslut sessionen
                out.println("stop");  // Send stop-besked til serveren
                break;
            }

            // Send brugerens input til serveren
            out.println(userInput);
            out.flush();  // Sørg for, at data bliver sendt

            // Læs og udskriv serverens respons
            try {
                String serverResponse;
                while ((serverResponse = in.readLine()) != null) {  // Læs serverens svar linje for linje
                    System.out.println("Server: " + serverResponse);  // Udskriv serverens svar
                    if (serverResponse.isEmpty()) {
                        break;  // Stop med at læse, når vi når slutningen af HTTP-responsen
                    }
                }
            } catch (IOException e) {
                System.out.println("Fejl ved læsning af serverrespons: " + e.getMessage());  // Håndter fejl ved læsning af serverens svar
            }
        }

        stopConnection();  // Luk forbindelsen
        scanner.close();  // Luk scanner
    }

    public void stopConnection() {
        try {
            in.close();  // Luk BufferedReader
            out.close();  // Luk PrintWriter
            clientSocket.close();  // Luk socket-forbindelsen
            System.out.println("Forbindelsen lukket.");  // Udskriv besked om, at forbindelsen er lukket
        } catch (IOException e) {
            throw new RuntimeException("Fejl ved lukning af forbindelsen", e);  // Håndter fejl ved lukning
        }
    }

    public static void main(String[] args) {
        SimpleClient client = new SimpleClient();  // Opret et nyt SimpleClient-objekt
        client.startConnection("127.0.0.1", 8080);  // Start forbindelse til serveren på localhost og port 8080
        client.startInteractiveSession();  // Start interaktiv session med serveren
    }
}
