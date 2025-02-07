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
    private ServerSocket server;

    // HashMap til rutehåndtering
    private Map<String, String> routes;

    public SimpleDemo() {
        // Initialiser HashMap med ruter og deres svar
        routes = new HashMap<>();
        routes.put("/hello", "Hello, World!");
        routes.put("/time", new Date().toString());
        routes.put("/echo", "Ingen gemte beskeder.");
    }

    public void start() {
        try {
            server = new ServerSocket(8080);  // Lyt på port 8080
            System.out.println("Server lytter på port 8080...");

            // Lyt på forbindelser og håndter dem i nye tråde
            while (true) {
                Socket socket = server.accept();  // Vent på forbindelse
                new Thread(() -> handleClient(socket)).start();
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private void handleClient(Socket socket) {
        try (BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true)) {

            String requestLine = in.readLine();
            if (requestLine != null) {
                System.out.println("Besked fra klient: " + requestLine);
                String[] parts = requestLine.split(" ");
                String method = parts[0];
                String path = parts[1];

                // Håndtering af GET-anmodninger
                if (method.equals("GET")) {
                    handleGET(path, out);
                }
                // Håndtering af POST-anmodninger
                else if (method.equals("POST")) {
                    handlePOST(in, out);
                }
                else {
                    sendResponse(out, "404 Not Found", "Siden blev ikke fundet.");
                }
            }
            socket.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private void handleGET(String path, PrintWriter out) {
        String responseBody = routes.get(path);

        if (responseBody != null) {
            sendResponse(out, "200 OK", responseBody);
        } else {
            sendResponse(out, "404 Not Found", "Siden blev ikke fundet.");
        }
    }

    private void handlePOST(BufferedReader in, PrintWriter out) throws IOException {
        StringBuilder message = new StringBuilder();
        String line;

        // Læs alle POST-data (måske flere linjer)
        while ((line = in.readLine()) != null && !line.isEmpty()) {
            message.append(line).append("\n");
        }

        // Hvis beskeden er tom, send fejl
        if (message.length() == 0) {
            sendResponse(out, "400 Bad Request", "No data received in POST request.");
            return;
        }

        // Her gemmes beskeden, og den returneres som en response.
        String storedMessage = message.toString().trim();
        sendResponse(out, "200 OK", "Message stored: " + storedMessage);
    }

    private void sendResponse(PrintWriter out, String status, String message) {
        SimpleDateFormat sdf = new SimpleDateFormat("E, dd MMM yyyy HH:mm:ss z");
        sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
        String formattedDate = sdf.format(new Date());

        String response = "HTTP/1.1 " + status + "\r\n" +
                "Date: " + formattedDate + "\r\n" +
                "Server: SimpleDemo\r\n" +
                "Content-Type: text/plain; charset=UTF-8\r\n" +
                "Content-Length: " + message.length() + "\r\n" +
                "\r\n" + message;

        out.print(response);
        out.flush();  // Sørg for, at svaret bliver sendt til klienten.
    }


    public static void main(String[] args) {
        new SimpleDemo().start();  // Start serveren
    }
}


