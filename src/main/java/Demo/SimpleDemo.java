package Demo;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class SimpleDemo {
    private ServerSocket server;

    public void start() {
        try {
            server = new ServerSocket(8080);
            while (true) {
                Socket socket = server.accept();
                Runnable clientHandler = new ClientHandler(socket);
                new Thread((clientHandler)).start();
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
    public static void main(String[] args) {
        new SimpleDemo().start();
    }

        private static class ClientHandler implements Runnable {
            private Socket clientSocket;

            public ClientHandler(Socket socket) {
                this.clientSocket = socket;
            }

            @Override
            public void run() {
                try {
                    System.out.println("Client registered");
                    PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
                    BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                    String msg;
                    while (in.ready() && (msg = in.readLine()) != null) { // Read messages in a loop
                        System.out.println("Message from client: " + msg);
                        out.println("We received this message: " + msg);

                        if (msg.trim().equals("Over and Out")) {
                            break; // Exit loop if termination message is received
                        }
                    }

                    out.close();
                    in.close();
                    clientSocket.close();

                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        }
}

