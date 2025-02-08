//SimpleDemo (Server): En simpel webserver, der håndterer HTTP GET- og POST-anmodninger.
//Denne klasse fungerer som en simpel HTTP-server, der lytter på port 8080 og håndterer HTTP-anmodninger.
package FredagsOpgave2;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

public class SimpleDemo {
    private ServerSocket server;  // ServerSocket objekt til at lytte på indkommende forbindelser

    // HashMap til rutehåndtering (til at matche HTTP-anmodninger med svar)
    private Map<String, String> routes;

    public SimpleDemo() {
        routes = new HashMap<>(); //Initialiser HashMap med ruter og deres svar
        routes.put("/hello", "Hello, World!");  // /hello rute svarer med "Hello, World!"
        routes.put("/time", new Date().toString());  // /time rute svarer med den nuværende dato og tid
        routes.put("/echo", "Ingen gemte beskeder.");  // /echo rute svarer med en standardbesked
    }

    public void start() {
        try {
            server = new ServerSocket(8080);  // Opret en server, der lytter på port 8080
            System.out.println("Server lytter på port 8080...");

            // Lyt på forbindelser og håndter dem i nye tråde
            while (true) {
                Socket socket = server.accept();  //Serveren lytter konstant på nye forbindelser (server.accept()).
                new Thread(() -> handleClient(socket)).start();  //For hver ny forbindelse opretter den en ny tråd, der håndterer klienten.
            }
        } catch (IOException ex) {
            ex.printStackTrace();  // Udskriv fejl, hvis serveren ikke kan starte
        }
    }

    private void handleClient(Socket socket) {
        try (BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));  // Læs indkommende data fra klienten
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true)) {  // Send data tilbage til klienten

            String requestLine = in.readLine();  // Læs første linje af HTTP-anmodningen
            if (requestLine != null) {
                System.out.println("Besked fra klient: " + requestLine);  // Udskriv anmodningen fra klienten
                String[] parts = requestLine.split(" ");  // Del anmodningen i dele (metode og sti)
                String method = parts[0];  // HTTP-metoden (f.eks. GET, POST)
                String path = parts[1];  // Stien (f.eks. /hello)

                // Håndtering af GET-anmodninger
                if (method.equals("GET")) {
                    handleGET(path, out);  // Håndter GET-anmodningen
                }
                // Håndtering af POST-anmodninger
                else if (method.equals("POST")) {
                    handlePOST(in, out);  // Håndter POST-anmodningen
                }
                else {
                    sendResponse(out, "404 Not Found", "Siden blev ikke fundet.");  // Ugyldig metode, svar med 404
                }
            }
            socket.close();  // Luk forbindelsen til klienten
        } catch (IOException ex) {
            ex.printStackTrace();  // Udskriv fejl, hvis der er problemer med klienthåndtering
        }
    }
    //Behandler GET-anmodninger
    private void handleGET(String path, PrintWriter out) {
        String responseBody = routes.get(path);  // Hent svar fra routes HashMap baseret på stien

        if (responseBody != null) {
            sendResponse(out, "200 OK", responseBody);  // Svar med OK og den passende besked
        } else {
            sendResponse(out, "404 Not Found", "Siden blev ikke fundet.");  // Hvis stien ikke findes, svar med 404
        }
    }
    //Behandler POST-anmodninger
    private void handlePOST(BufferedReader in, PrintWriter out) throws IOException {
        StringBuilder message = new StringBuilder();
        String line;

        // Læs alle POST-data (måske flere linjer)
        while ((line = in.readLine()) != null && !line.isEmpty()) {
            message.append(line).append("\n");  // Gem POST-data
        }

        // Hvis beskeden er tom, send fejl
        if (message.length() == 0) {
            sendResponse(out, "400 Bad Request", "No data received in POST request.");  // Hvis der ikke er data, svar med 400
            return;
        }

        // Her gemmes beskeden, og den returneres som en response.
        String storedMessage = message.toString().trim();
        sendResponse(out, "200 OK", "Message stored: " + storedMessage);  // Returner den modtagne besked
    }

    private void sendResponse(PrintWriter out, String status, String message) {
        SimpleDateFormat sdf = new SimpleDateFormat("E, dd MMM yyyy HH:mm:ss z");  // Formatér datoen for HTTP-responsen
        sdf.setTimeZone(TimeZone.getTimeZone("GMT"));  // Indstil tid til GMT
        String formattedDate = sdf.format(new Date());  // Få den aktuelle dato i korrekt format

        // Byg HTTP-responsen
        String response = "HTTP/1.1 " + status + "\r\n" +
                "Date: " + formattedDate + "\r\n" +
                "Server: SimpleDemo\r\n" +
                "Content-Type: text/plain; charset=UTF-8\r\n" +
                "Content-Length: " + message.length() + "\r\n" +
                "\r\n" + message;

        out.print(response);  // Send responsen til klienten
        out.flush();  // Sørg for, at svaret bliver sendt til klienten
    }

    public static void main(String[] args) {
        new SimpleDemo().start();  // Start serveren
    }
}

