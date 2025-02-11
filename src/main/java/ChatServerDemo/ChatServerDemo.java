package ChatServerDemo;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

// Klassen ChatServerDemo implementerer IObservable, hvilket betyder, at den kan sende beskeder til klienter.
public class ChatServerDemo implements IObservable {

    // Privat konstruktor forhindrer direkte instansiering af klassen udefra.
    private ChatServerDemo() {}

    // Singleton-mønster: Sikrer, at der kun er én instans af serveren.
    public static synchronized IObservable getInstance() {
        if (server == null) {  // Hvis serveren ikke eksisterer, opretter vi en ny instans.
            server = new ChatServerDemo();
        }
        return server;
    }

    private static volatile IObservable server = getInstance(); // Volatil variabel sikrer, at ændringer er synlige på tværs af tråde.

    private List<ClientHandler> clients = new ArrayList<>(); // Liste til at gemme klienter, der er forbundet til serveren.

    public static void main(String[] args) {
        new ChatServerDemo().startServer(8080); // Starter serveren på port 8080.
    }

    public void startServer(int port) {
        try {
            ServerSocket serverSocket = new ServerSocket(port); // Opretter en server-socket, der lytter på den angivne port.

            while (true) { // Uendelig løkke til at acceptere nye klientforbindelser.
                Socket clientSocket = serverSocket.accept(); // Venter på en klientforbindelse.
                ClientHandler clientHandler = new ClientHandler(clientSocket, this); // Opretter en ny ClientHandler til klienten.
                clients.add(clientHandler); // Tilføjer klienten til listen over aktive klienter.
                new Thread(clientHandler).start(); // Starter klienthåndteringen i en ny tråd.
            }
        } catch (IOException e) {
            e.printStackTrace(); // Udskriver fejl, hvis der opstår problemer.
        }
    }

    @Override
    public void broadcast(String msg) {
        for (ClientHandler ch : clients) { // Går igennem alle klienter og sender beskeden.
            ch.notify(msg);
        }
    }

    public synchronized ClientHandler getClientByName(String name) {
        for (ClientHandler client : clients) { // Går igennem alle klienter.
            if (client.getName().equalsIgnoreCase(name)) { // Finder klienten med det ønskede navn.
                return client;
            }
        }
        return null; // Returnerer null, hvis ingen klient med det navn findes.
    }

    // Indre klasse, der håndterer en klientforbindelse. Implementerer både Runnable (for tråde) og IObserver (for beskeder).
    private static class ClientHandler implements Runnable, IObserver {
        private Socket clientSocket; // Klientens socket-forbindelse.
        private PrintWriter out; // Skriver data til klienten.
        private BufferedReader in; // Læser data fra klienten.
        private IObservable server; // Reference til serveren.
        private String name = null; // Klientens navn.

        public ClientHandler(Socket socket, IObservable server) throws IOException {
            this.clientSocket = socket;
            this.server = server;
            out = new PrintWriter(clientSocket.getOutputStream(), true); // Opretter en PrintWriter til at sende data.
            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream())); // Opretter en BufferedReader til at læse data.
        }

        public String getName() {
            return name; // Returnerer klientens navn.
        }

        @Override
        public void run() {
            try {
                String msg;
                while ((msg = in.readLine()) != null) { // Læser beskeder fra klienten.
                    if (msg.startsWith("#JOIN")) {
                        // Håndterer, når en ny klient slutter sig til chatten.
                        this.name = msg.split(" ")[1]; // Henter klientens navn.
                        server.broadcast("A new person joined the chat. Welcome to " + name);
                    } else if (msg.startsWith("#MESSAGE")) {
                        // Håndterer en offentlig besked.
                        String messageContent = msg.substring(9).trim(); // Fjerner kommandoen og henter selve beskeden.
                        server.broadcast(name + ": " + messageContent);
                    } else if (msg.startsWith("#LEAVE")) {
                        // Håndterer, når en klient forlader chatten.
                        server.broadcast(name + " left the chat.");
                        out.close(); // Lukker forbindelsen til klienten.
                    } else if (msg.startsWith("#PRIVATE")) {
                        // Håndterer privatbeskeder.
                        String[] parts = msg.split(" ", 3); // Opdeler beskeden i dele: kommando, modtager og selve beskeden.
                        if (parts.length == 3) {
                            String recipientName = parts[1]; // Modtagerens navn.
                            String privateMessageContent = parts[2]; // Beskedens indhold.
                            ClientHandler recipient = ((ChatServerDemo) server).getClientByName(recipientName); // Finder modtageren.
                            if (recipient != null) {
                                recipient.notify(name + " (private): " + privateMessageContent); // Sender beskeden til modtageren.
                            } else {
                                out.println("User " + recipientName + " not found."); // Hvis modtageren ikke findes.
                            }
                        } else {
                            out.println("Invalid private message format. Use: #PRIVATE <nickname> <message>");
                        }
                    } else {
                        // Håndterer almindelige beskeder.
                        server.broadcast(name + ": " + msg);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace(); // Udskriver fejl, hvis der opstår problemer.
            }
        }

        @Override
        public void notify(String msg) {
            System.out.println(msg); // Udskriver beskeden i serverens terminal.
            out.println(msg); // Sender beskeden til klienten.
        }
    }
}

