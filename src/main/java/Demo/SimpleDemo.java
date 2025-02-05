package Demo;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class SimpleDemo {
    private ServerSocket server;
    private Socket clientHandler;
    private PrintWriter out;
    private BufferedReader in;

    public void start() {
        try {
            server = new ServerSocket(8080);
            clientHandler = server.accept();
            out = new PrintWriter(clientHandler.getOutputStream(), true); //opretter en kanal der kan håndtere 0 og 1 som sendes fra den ene til den anden (fortolket)
            in = new BufferedReader(new InputStreamReader(clientHandler.getInputStream()));
            System.out.println("Server started");
            out.println("Connection established");
            String inputLine;

            while ((inputLine = in.readLine()) != null) {
                System.out.println("Message from client: " + inputLine);
                out.println("Echo: " + inputLine);
                if (inputLine.equals("stop")) {
                    out.println("Good bye. I am shutting down");
                    stop();
                    break;
                }
            }
        } catch (IOException e) {
            // TODO: remember production ready
            e.printStackTrace();
        } finally {
            try {
                stop();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void stop() throws IOException {
        in.close();
        out.close();
        clientHandler.close();
        server.close();
    }

    public static void main(String[] args) {
        new SimpleDemo().start();
    }
}

