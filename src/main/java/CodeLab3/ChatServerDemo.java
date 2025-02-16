package CodeLab3;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ChatServerDemo implements IObservable {
    // Singleton-instansering: Sikrer, at der kun er én instans af serveren
    private static volatile IObservable server = null; // Volatil variabel bruges for trådsikkerhed
    private List<ClientHandler> clients = new ArrayList<>(); // Liste over klienter
    private ServerSocket serverSocket; // Serverens socket til at lytte efter forbindelser
    private ExecutorService threadPool = Executors.newFixedThreadPool(10); // Thread pool til håndtering af klienter
    private static HashSet<String> inappropriateWords = new HashSet<>(); // Liste over upassende ord

    // Privat konstruktor forhindrer direkte instansiering af klassen udefra
    private ChatServerDemo() {}

    // Singleton-metode: Sikrer, at kun én instans oprettes
    public static synchronized ChatServerDemo getInstance() {
        if (server == null) {
            server = new ChatServerDemo();
        }
        return (ChatServerDemo) server;
    }

    public static void main(String[] args) {
        ChatServerDemo.getInstance().startServer(9999); // Starter serveren på port 9999
    }

    public void startServer(int port) {
        try {
            // Tilføjer standard upassende ord
            inappropriateWords.add("fuck");
            inappropriateWords.add("bitch");
            inappropriateWords.add("shit");
            this.serverSocket = new ServerSocket(port);
            System.out.println("Server started on port " + port);

            while (true) {
                Socket clientSocket = serverSocket.accept(); // Accepterer nye klientforbindelser
                ClientHandler clientHandler = new ClientHandler(clientSocket, this, clients);
                clients.add(clientHandler); // Tilføjer klienten til listen
                threadPool.execute(clientHandler); // Starter klienthåndtering i en separat tråd
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void broadcast(String msg) {
        // Sender besked til alle klienter
        for (ClientHandler ch : clients) {
            ch.notify(msg);
        }
    }

    public synchronized ClientHandler getClientByName(String name) {
        // Finder klient ud fra navn
        for (ClientHandler client : clients) {
            if (client.getName().equalsIgnoreCase(name)) {
                return client;
            }
        }
        return null;
    }

    public synchronized List<String> getUserList() {
        // Returnerer en liste over alle brugernavne
        List<String> userList = new ArrayList<>();
        for (ClientHandler client : clients) {
            userList.add(client.getName());
        }
        return userList;
    }

    public synchronized List<String> getPrivateSubList(String clientName) {
        // Returnerer en liste over andre brugere end den anmodende klient
        List<String> privateSubList = new ArrayList<>();
        for (ClientHandler client : clients) {
            if (!client.getName().equals(clientName)) {
                privateSubList.add(client.getName());
            }
        }
        return privateSubList;
    }

    public synchronized void sendPrivateMessage(String senderName, String receiverName, String message) {
        // Finder modtager og sender en privat besked
        ClientHandler receiver = getClientByName(receiverName);
        if (receiver != null) {
            receiver.sendMessage("Private message from " + senderName + ": " + message);
        } else {
            ClientHandler sender = getClientByName(senderName);
            if (sender != null) {
                sender.sendMessage("Error: User " + receiverName + " not found.");
            }
        }
    }

    public synchronized void removeClient(ClientHandler client) {
        // Fjerner en klient fra listen
        clients.remove(client);
    }

    public synchronized void addInappropriateWord(String word) {
        // Tilføjer et nyt bandord, hvis det ikke findes i forvejen
        if (!inappropriateWords.contains(word)) {
            inappropriateWords.add(word);
            broadcast("New inappropriate word added: " + word);
        }
    }


    public void shutdown() {
        try { //Sender en besked til alle klienter om, at serveren lukker ned.
            broadcast("Server is shutting down. All clients will be disconnected.");

            //Lukker alle klienters forbindelser.
            for (ClientHandler client : clients) {
                try {
                    client.clientSocket.close();
                    client.out.close();
                    client.in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            //Lukker serverens socket, hvis den ikke allerede er lukket.
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }

            threadPool.shutdown(); // Lukker thread poolen efter alle klienter er håndteret

        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    //--------------------------------------------------------------------------------------------------------------------------

    //ClientHandler håndterer én klient og kører i sin egen tråd.
    //Implementerer IObserver, så den kan modtage beskeder fra serveren.

    public static class ClientHandler implements Runnable, IObserver {
        private Socket clientSocket; //Forbindelsen til klienten.
        private PrintWriter out;    //Bruges til at sende beskeder til klienten.
        private BufferedReader in;  //Bruges til at læse beskeder fra klienten.
        private IObservable server; //Refererer til serveren.
        private List<ClientHandler> clients; //Liste over alle klienter.
        private String name = "GUEST"; //Standardnavn for en ny klient.

        //Konstruktor, der sætter forbindelsen op og opretter input/output-strømme.
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
        public void run() { //Læser beskeder fra klienten løbende.
            try {
                String msg;
                while ((msg = in.readLine()) != null) { // Læser beskeder fra klienten
                    // Tjek om beskeden indeholder et bandord
                    boolean containsInappropriateWord = false;
                    for (String word : inappropriateWords) {
                        if (msg.contains(word)) {
                            containsInappropriateWord = true;
                            break; // Stopper efter første fund af et bandord
                        }
                    }

                    // Hvis beskeden indeholder et bandord, send besked og spring over udførelsen af kommandoen
                    if (containsInappropriateWord) {
                        sendMessage("You cannot use inappropriate words!");
                        continue; // Spring beskeden over
                    }

                    // Tjek om beskeden er et bandord, der skal tilføjes
                    if (msg.startsWith("**")) {
                        String word = msg.substring(2).trim(); // Fjerner "**" fra starten af ordet
                        getServer().addInappropriateWord(word); // Tilføjer ordet til bandord listen
                        sendMessage("The word '" + word + "' has been added to the inappropriate words list.");
                        continue; // Spring over kommandoeksekvering
                    }

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