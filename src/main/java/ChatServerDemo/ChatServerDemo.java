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

    private static IObservable server = new ChatServerDemo();

    private ChatServerDemo() {}

    public static synchronized IObservable getInstance() {
        if (server == null) {
            server = new ChatServerDemo();
        }
        return server;
    }

   // private static volatile IObservable server = getInstance();

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

        @Override
        public void run() {
            try {
                String msg;
                while ((msg = in.readLine()) != null) {
                    //System.out.println(msg);
                    if(msg.startsWith("#JOIN")){
                       String name = msg.split(" ")[1];
                       server.broadcast(" A new person joined the chat. Welcome to " + name);
                       this.name = name;
                    } else{
                        server.broadcast("Broadcasting message: " + msg);
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
