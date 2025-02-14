package CodeLab3;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ChatServerDemo implements IObservable {
    private static volatile IObservable server = null; // Volatil variabel
    private List<ClientHandler> clients = new ArrayList<>();
    private ServerSocket serverSocket;
    private ExecutorService threadPool = Executors.newFixedThreadPool(10);

    // Privat konstruktor forhindrer direkte instansiering af klassen udefra
    private ChatServerDemo() {}

    // Singleton-mønster: Sikrer, at der kun er én instans af serveren
    public static synchronized ChatServerDemo getInstance() {
        if (server == null) {
            server = new ChatServerDemo();
        }
        return (ChatServerDemo) server;
    }

    public static void main(String[] args) {
        ChatServerDemo.getInstance().startServer(8080); // Starter serveren på port 8080
    }

    public void startServer(int port) {
        try {
            this.serverSocket = new ServerSocket(port);
            System.out.println("Server started on port " + port);

            while (true) {
                Socket clientSocket = serverSocket.accept();
                ClientHandler clientHandler = new ClientHandler(clientSocket, this, clients);
                clients.add(clientHandler);
                threadPool.execute(clientHandler); // Brug thread pool til at håndtere klienten
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void broadcast(String msg) {
        for (ClientHandler ch : clients) { // Går igennem alle klienter og sender beskeden
            ch.notify(msg);
        }
    }

    public synchronized ClientHandler getClientByName(String name) {
        for (ClientHandler client : clients) { // Går igennem alle klienter
            if (client.getName().equalsIgnoreCase(name)) { // Finder klienten med det ønskede navn
                return client;
            }
        }
        return null; // Returnerer null, hvis ingen klient med det navn findes
    }

    // Metode til at hente alle brugernavne
    public synchronized List<String> getUserList() {
        List<String> userList = new ArrayList<>();
        for (ClientHandler client : clients) {
            userList.add(client.getName());
        }
        return userList;
    }

    public synchronized List<String> getPrivateSubList(String clientName) {
        List<String> privateSubList = new ArrayList<>();
        for (ClientHandler client : clients) {
            if (!client.getName().equals(clientName)) {
                privateSubList.add(client.getName());  // Tilføjer alle klienter undtagen den anmodende klient
            }
        }
        return privateSubList;
    }

    // Metode til at sende en privat besked
    public synchronized void sendPrivateMessage(String senderName, String receiverName, String message) {
        // Find modtageren ved navn
        ClientHandler receiver = getClientByName(receiverName);
        if (receiver != null) {
            // Sender besked til modtageren
            receiver.sendMessage("Private message from " + senderName + ": " + message);
        } else {
            // Hvis modtageren ikke findes, send en fejlbesked til afsenderen
            ClientHandler sender = getClientByName(senderName);
            if (sender != null) {
                sender.sendMessage("Error: User " + receiverName + " not found.");
            }
        }
    }

    public synchronized void removeClient(ClientHandler client) {
        clients.remove(client);
    }


    public void shutdown() {
        try {
            broadcast("Server is shutting down. All clients will be disconnected.");

            for (ClientHandler client : clients) {
                try {
                    client.clientSocket.close();
                    client.out.close();
                    client.in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }   

            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }

            threadPool.shutdown(); // Lukker thread poolen efter alle klienter er håndteret

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    //--------------------------------------------------------------------------------------------------------------------------

    public static class ClientHandler implements Runnable, IObserver {
        private Socket clientSocket;
        private PrintWriter out;
        private BufferedReader in;
        private IObservable server;
        private List<ClientHandler> clients;
        private String name = "GUEST"; // Klientens navn

        public ClientHandler(Socket socket, IObservable server, List<ClientHandler> clients) throws IOException {
            this.clientSocket = socket;
            this.server = server;
            this.clients = clients;
            out = new PrintWriter(clientSocket.getOutputStream(), true); // Opretter PrintWriter til at sende data
            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream())); // BufferedReader til at læse data

                   }

        public String getName() {
            return name;
        }


        @Override
        public void run() {
            try {
                String msg;
                while ((msg = in.readLine()) != null) { // Læser beskeder fra klienten
                    // Hent kommandoen fra starten af beskeden
                    String commandKey = getCommandKey(msg);
                    Command command = CommandFactory.getCommand(commandKey);

                    // Udfør kommandoen
                    command.execute(this, msg);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        private String getCommandKey(String msg) {
            // Returnerer kommandoen ud fra den første del af beskeden
            int spaceIndex = msg.indexOf(" ");
            if (spaceIndex == -1) {
                return msg; // Hvis der ikke er noget mellemrum, er det en ren kommando
            } else {
                return msg.substring(0, spaceIndex); // Hvis der er et mellemrum, så tag kommandoen
            }
        }


        public void sendMessage(String message) {
            out.println(message);
        }

        public void setName(String name) {
            this.name = name;
        }

        public ChatServerDemo getServer() {
            return (ChatServerDemo) server;
        }

        public Socket getClientSocket() {
            return clientSocket;
        }

        public PrintWriter getOut() {
            return out;
        }

        public BufferedReader getIn() {
            return in;
        }


        @Override
        public void notify(String msg) {
            System.out.println(msg); // Udskriver beskeden i serverens terminal
            out.println(msg); // Sender beskeden til klienten
        }
    }
}

