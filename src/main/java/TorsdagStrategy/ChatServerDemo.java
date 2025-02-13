package TorsdagStrategy;

import CodeLab3.IObservable;
import CodeLab3.IObserver;

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

    private static class ClientHandler implements Runnable, IObserver {
        private Socket clientSocket;
        private PrintWriter out;
        private BufferedReader in;
        private IObservable server;
        private List<ClientHandler> clients;
        private String name = "GUEST"; // Klientens navn
        private ArrayList<String> badWords = new ArrayList<>();

        public ClientHandler(Socket socket, IObservable server, List<ClientHandler> clients) throws IOException {
            this.clientSocket = socket;
            this.server = server;
            this.clients = clients;
            out = new PrintWriter(clientSocket.getOutputStream(), true); // Opretter PrintWriter til at sende data
            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream())); // BufferedReader til at læse data

            badWords.add("fuck");
            badWords.add("bitch");
            badWords.add("shit");
            badWords.add("whore");
        }

        public String getName() {
            return name;
        }

        private String getHelpMessage() {
            return "List of available commands: #JOIN, #MESSAGE, #PRIVATE, #GETLIST, #PRIVATESUBLIST, #HELP, #STOPSERVER";
        }

        @Override
        public void run() {
            try {
                String msg;
                while ((msg = in.readLine()) != null) { // Læser beskeder fra klienten
                    if (containsBadWord(msg)) {
                        out.println("Din besked indeholder upassende ord og blev ikke sendt.");
                        continue; // Stopper behandlingen af beskeden
                    }
                    if (msg.startsWith("#JOIN")) {
                        String[] parts = msg.split(" ", 2);
                        if (parts.length < 2) {
                            out.println("Usage: #JOIN <name>");
                        } else {
                            this.name = parts[1];
                            server.broadcast("A new person joined the chat. Welcome to " + name);
                        }
                    } else if (msg.startsWith("#MESSAGE")) {
                        String messageContent = msg.substring(9).trim();
                        server.broadcast(name + ": " + messageContent);
                    } else if (msg.startsWith("#LEAVE")) {
                        server.broadcast(name + " left the chat.");
                        clients.remove(this);
                        in.close();
                        out.close();
                        clientSocket.close();
                        break; // Stopper while-løkken
                    } else if (msg.startsWith("#PRIVATE")) {
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
                    } else if (msg.startsWith("#GETLIST")) {
                        for (ClientHandler c : clients) {
                            out.println(c.getName());
                        }
                    } else if (msg.startsWith("#PRIVATESUBLIST")) {
                        String[] parts = msg.split(" ", 3);
                        if (parts.length == 3) {
                            String[] recipients = parts[1].split(",");
                            String sublistMessageContent = parts[2];
                            for (String recipientName : recipients) {
                                ClientHandler recipient = ((ChatServerDemo) server).getClientByName(recipientName.trim());
                                if (recipient != null) {
                                    recipient.notify(name + " (private): " + sublistMessageContent);
                                } else {
                                    out.println("User " + recipientName + " not found.");
                                }
                            }
                        } else {
                            out.println("Invalid private sublist message format. Use: #PRIVATESUBLIST <nickname1,nickname2,...> <message>");
                        }
                    } else if (msg.startsWith("#HELP")) {
                        out.println(getHelpMessage());
                    } else if (msg.startsWith("#STOPSERVER")) {
                        ((ChatServerDemo) server).shutdown();
                    } else if (msg.startsWith("**")) {
                        String wordToAdd = msg.substring(2).trim();
                        addBadWord(wordToAdd);
                    } else {
                        server.broadcast(name + ": " + msg);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public void addBadWord(String word) {
            if (!badWords.contains(word)) {
                badWords.add(word);
                System.out.println("Ordet '" + word + "' er blevet tilføjet til listen over upassende ord.");
            } else {
                System.out.println("Ordet '" + word + "' findes allerede i listen.");
            }
        }

        private boolean containsBadWord(String message) {
            for (String badWord : badWords) {
                if (message.toLowerCase().contains(badWord)) {
                    return true;
                }
            }
            return false;
        }

        @Override
        public void notify(String msg) {
            System.out.println(msg); // Udskriver beskeden i serverens terminal
            out.println(msg); // Sender beskeden til klienten
        }
    }
}
