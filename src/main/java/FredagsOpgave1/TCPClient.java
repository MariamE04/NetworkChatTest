package FredagsOpgave1;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public class TCPClient {
    private Socket clientSocket;
    private PrintWriter out;
    private BufferedReader in;

    public void startConnection(String ip, int port) {
        try {
            clientSocket = new Socket(ip, port);
            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            out = new PrintWriter(clientSocket.getOutputStream(), true);

            System.out.println("Connected to server!");

            Scanner scan = new Scanner(System.in);
            String userInput;

            while (true) {
                System.out.print("Enter message: ");
                userInput = scan.nextLine();
                out.println(userInput);

                StringBuilder requestBuilder = new StringBuilder();
                String newLine;
                while (in.ready() && (newLine = in.readLine()) != null && !newLine.isEmpty()) {
                    requestBuilder.append(newLine).append("\n");
                }

                System.out.println("Server: " + requestBuilder.toString());

                if (userInput.equalsIgnoreCase("stop")) {
                    System.out.println("Closing connection...");
                    break;
                }
            }
            stopConnection();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private void stopConnection() {
        try {
          in.close();
          out.close();
          clientSocket.close();
            System.out.println("Client disconnected.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        TCPClient client = new TCPClient();
        client.startConnection("127.0.0.1", 8080);
    }
}


