package CodeLab2;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class TPCdemo {
    private ServerSocket server;
    private Socket clientHandler;
    private PrintWriter out;
    private BufferedReader in;

    public void start() {
        try {
            server = new ServerSocket(8080);
            System.out.println("Server lytter på port 8080...");

            clientHandler = server.accept(); // Venter på klientforbindelse
            System.out.println("Klient forbundet!");

            out = new PrintWriter(clientHandler.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(clientHandler.getInputStream()));

            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                System.out.println("Besked fra klient: " + inputLine);

                // Hvis klienten sender en HTTP GET-request, håndteres den:
                if (inputLine.startsWith("GET /hello")) {
                    handleRequestGET(inputLine);
                }

                if(inputLine.startsWith("POST /echo")){
                    handleRequestPOST(inputLine);
                }

                // Hvis klienten sender "stop", lukkes forbindelsen:
                else if (inputLine.equalsIgnoreCase("stop")) {
                    out.println("Goodbye. Server shutting down.");
                    break;
                }
                // Ellers ekkoes beskeden:
                else {
                    out.println("Echo: " + inputLine);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            stop();
        }
    }

    public void handleRequestGET(String request) {
        // Split requesten for at finde HTTP-metoden og stien
        String[] parts = request.split(" ");
        if(parts.length == 3){
        String responseBody = "Hello, World!";

        // Opret dato i HTTP-format og sørg for at bruge GMT
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("E, dd MMM yyyy HH:mm:ss z");
        sdf.setTimeZone(java.util.TimeZone.getTimeZone("GMT"));
        String formattedDate = sdf.format(new java.util.Date());

        // Forbered HTTP-responsen med headers og body
        String headersResponse = "HTTP/1.1 200 OK\r\n" +
                "Date: " + formattedDate + "\r\n" +
                "Server: YourServerName\r\n" +
                "Content-Type: text/html; charset=UTF-8\r\n" +
                "Content-Length: " + responseBody + "\r\n" +
                "\r\n"; // Tom linje markerer slutningen af headers

        out.print(headersResponse);
        out.flush();

        // Hvis stien ikke er "/hello", returneres en 404 Not Found
    }   else {
        String notFoundResponse = "HTTP/1.1 404 Not Found\r\n\r\n";
        out.print(notFoundResponse);
        out.flush();
         }
    }

    public void handleRequestPOST(String request) {
        String[] parts = request.split(" ");

        // Tjek om stien er /hello
        if (parts.length == 3 && parts[1].equals("/echo")) {
            String responseBody = "Hello";

            // Opret dato i HTTP-format og sørg for at bruge GMT
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("E, dd MMM yyyy HH:mm:ss z");
            sdf.setTimeZone(java.util.TimeZone.getTimeZone("GMT"));
            String formattedDate = sdf.format(new java.util.Date());

            // Forbered HTTP-responsen med headers og body
            String headersResponse = "HTTP/1.1 200 OK\r\n" +
                    "Date: " + formattedDate + "\r\n" +
                    "Server: YourServerName\r\n" +
                    "Content-Type: text/html; charset=UTF-8\r\n" +
                    "Content-Length: " + responseBody.length() + "\r\n" +  // Brug længden af responseBody
                    "\r\n"; // Tom linje markerer slutningen af headers

            // Skriv headers og body til output
            out.print(headersResponse);
            out.print(responseBody);  // Tilføj body til svaret
            out.flush();
        } else {
            // Hvis stien ikke er "/hello", returneres en 404 Not Found
            String notFoundResponse = "HTTP/1.1 404 Not Found\r\n\r\n";
            out.print(notFoundResponse);
            out.flush();
        }
    }


    private void stop() {
        try {
            in.close();
            out.close();
            clientHandler.close();
            server.close();
            System.out.println("Server stoppet.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        new TPCdemo().start();
    }
}



