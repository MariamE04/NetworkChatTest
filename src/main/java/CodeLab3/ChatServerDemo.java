package CodeLab3;

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
    private static volatile IObservable server = getInstance(); // Volatil variabel sikrer, at ændringer er synlige på tværs af tråde.
    private List<ClientHandler> clients = new ArrayList<>(); // Liste til at gemme klienter, der er forbundet til serveren.
    private ServerSocket serverSocket;


    // Privat konstruktor forhindrer direkte instansiering af klassen udefra.
    private ChatServerDemo() {
    }

    // Singleton-mønster: Sikrer, at der kun er én instans af serveren.
    public static synchronized IObservable getInstance() {
        if (server == null) {  // Hvis serveren ikke eksisterer, opretter vi en ny instans.
            server = new ChatServerDemo();
        }
        return server;
    }

    public static void main(String[] args) {
        new ChatServerDemo().startServer(8080); // Starter serveren på port 8080.
    }

    public void startServer(int port) {
        try {
            this.serverSocket = new ServerSocket(port); // Opretter en server-socket, der lytter på den angivne port.

            while (true) { // Uendelig løkke til at acceptere nye klientforbindelser.
                Socket clientSocket = serverSocket.accept(); // Venter på en klientforbindelse.
                ClientHandler clientHandler = new ClientHandler(clientSocket, this, clients); // Opretter en ny ClientHandler til klienten.
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

    // I ChatServerDemo klassen
    public synchronized List<ClientHandler> getClients() {
        return clients; // Giver adgang til listen over klienter
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

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    //----------------------------------------------------------------------------------------------------------------------------------------------

    public class ClientHandler implements Runnable, IObserver {
        private Socket clientSocket;
        private PrintWriter out;
        private BufferedReader in;
        private IObservable server;
        private List<ClientHandler> clients;
        private String name = "GUEST";
        private Command currentCommand;  // Skift fra CommandStrategy til Command

        public ClientHandler(Socket socket, IObservable server, List<ClientHandler> clients) throws IOException {
            this.clientSocket = socket;
            this.server = server;
            this.clients = clients;
            out = new PrintWriter(clientSocket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        }

        @Override
        public void run() {
            try {
                String msg;
                while ((msg = in.readLine()) != null) {
                    if (msg.isEmpty()) continue; // Undgå at behandle tomme beskeder

                    if (containsBadWord(msg)) {
                        out.println("Your message contains inappropriate words and was not sent.");
                        continue;
                    }

                    // Vælg den rette kommando
                    if (msg.startsWith("#JOIN")) {
                        currentCommand = new JoinCommand();
                    } else if (msg.startsWith("#MESSAGE")) {
                        currentCommand = new MessageCommand();
                    } else if (msg.startsWith("#PRIVATE")) {
                        currentCommand = new PrivateMessageCommand();
                    } else if (msg.startsWith("#GETLIST")) {
                        currentCommand = new GetListCommand();
                    } else if (msg.startsWith("#HELP")) {
                        currentCommand = new HelpCommand();
                    } else if (msg.startsWith("#STOPSERVER")) {
                        currentCommand = new StopServerCommand();
                    } else {
                        out.println("Invalid command. Type #HELP for a list of commands.");
                        continue;
                    }

                    // Kør den valgte kommando
                    if (currentCommand != null) {
                        currentCommand.execute(this, msg);
                    }
                }
            } catch (IOException e) {
                System.out.println("Client disconnected: " + getName());
                try {
                    clientSocket.close();
                    out.close();
                    in.close();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        }

        private boolean containsBadWord(String message) {
            // Logik for at checke for upassende ord
            return false; // Placeholder
        }

        @Override
        public void notify(String msg) {
            out.println(msg);
        }

        public void sendMessage(String message) {
            out.println(message);
        }

        public IObservable getServer() {
            return server;
        }

        public PrintWriter getOut() {
            return out;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }
}

