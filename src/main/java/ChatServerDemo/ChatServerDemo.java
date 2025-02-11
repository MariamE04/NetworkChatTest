package ChatServerDemo;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class ChatServerDemo implements IObservable {
        private ChatServerDemo() {}

    public static synchronized IObservable getInstance() {
        if (server == null) {
            server = new ChatServerDemo();
        }
        return server;
    }

    private static volatile IObservable server = getInstance();

    private List<ClientHandler> clients = new ArrayList<>();

    public static void main(String[] args) {
        new ChatServerDemo().startServer(8080);

    }
    public void startServer(int port){
        try {
            ServerSocket serverSocket = new ServerSocket(port);

            while (true) {
                Socket clientSocket = serverSocket.accept();
                ClientHandler clientHandler = new ClientHandler(clientSocket, this);
                clients.add(clientHandler);
                new Thread(clientHandler).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void broadcast(String msg){
        for(ClientHandler ch : clients){
            ch.notify(msg);
        }
    }

    public synchronized ClientHandler getClientByName(String name) {
        for (ClientHandler client : clients) {
            if (client.getName().equalsIgnoreCase(name)) {
                return client;
            }
        }
        return null;
    }

    private static class ClientHandler implements Runnable, IObserver{
        private Socket clientSocket;
        private PrintWriter out;
        private BufferedReader in;
        private IObservable server;
        private String name = null;

        public ClientHandler(Socket socket, IObservable server) throws IOException{
            this.clientSocket = socket;
            this.server = server;
            out = new PrintWriter(clientSocket.getOutputStream(),true);
            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        }

        public String getName() {
            return name;
        }

        @Override
        public void run() {
            try {
                String msg;
                while ((msg = in.readLine()) != null) {
                    if (msg.startsWith("#JOIN")) {
                        this.name = msg.split(" ")[1];
                        server.broadcast("A new person joined the chat. Welcome to " + name);
                    } else if (msg.startsWith("#MESSAGE")) {
                        // Håndter offentlig besked
                        String messageContent = msg.substring(9).trim();
                        server.broadcast(name + ": " + messageContent);
                    } else if (msg.startsWith("#LEAVE")) {
                        // Håndter leave-melding
                        server.broadcast(name + " left the chat.");
                        out.close();
                    } else if (msg.startsWith("#PRIVATE")) {
                        // Håndter privat besked
                        String[] parts = msg.split(" ", 3);
                        if (parts.length == 3) {
                            String recipientName = parts[1];
                            String privateMessageContent = parts[2];
                            ClientHandler recipient = ((ChatServerDemo) server).getClientByName(recipientName);
                            if (recipient != null) {
                                recipient.notify(name + " (private): " + privateMessageContent);
                            } else {
                                out.println("User " + recipientName + " not found.");
                            }
                        } else {
                            out.println("Invalid private message format. Use: #PRIVATE <nickname> <message>");
                        }
                    } else {
                        // Håndter almindelig besked, hvis ingen kommandoer matcher
                        server.broadcast(name + ": " + msg);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void notify(String msg) {
            System.out.println(msg);
            out.println(msg);

        }
    }

}

