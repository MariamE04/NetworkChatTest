package SimpleTCPJava;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class Server implements Runnable {

    private ArrayList<ConnectionHandler> connections;
    private  ServerSocket server;
    private boolean done;

    public Server(){
        connections = new ArrayList<>();
        done = false;
    }

    @Override
    public void run() {
        try {
            server = new ServerSocket(9999);
            while(!done) {
                Socket client = server.accept();
                ConnectionHandler handler = new ConnectionHandler(client);
                connections.add(handler);
            }

        } catch (IOException e) {
            ///TODO: handle
        }
    }

    public void broadcast(String message){
        for(ConnectionHandler ch: connections){
            if(ch != null){
                ch.sendMessage(message);
            }
        }
    }

    public void shutdown() throws IOException {
        done = true;
        if(!server.isClosed()){
            server.close();
        }
    }


    //this class will handle klient-connection
    class ConnectionHandler implements Runnable{
        private Socket client;
        private BufferedReader in;
        private PrintWriter out;
        private String nickname;

        public ConnectionHandler(Socket socket){
            this.client = client;
        }

        @Override
        public void run() {
            try{
                out = new PrintWriter(client.getOutputStream(),true);
                in = new BufferedReader(new InputStreamReader(client.getInputStream()));
                out.println("Please enter a nickname: ");
                nickname = in.readLine();
                System.out.println(nickname + " connected!");
                broadcast(nickname + " Joined the chat!");
                String message;

                while ((message = in.readLine()) != null){
                    if(message.startsWith("/nick")){
                        String[] messageSplit = message.split("",2);
                        if(messageSplit.length == 2){
                            broadcast(nickname + " renamed themeselves to " + messageSplit[1]);
                            System.out.println(nickname + " renamed themeselves to " + messageSplit[1]);
                            nickname = messageSplit[1];
                            out.println("Successfully changed nickname to: " + nickname);
                        } else{
                            out.println("No nickname provieded");
                        }

                    } else if(message.startsWith("/quit")){
                        /// TODO: quit

                    } else{
                        broadcast(nickname + " : " + message);
                    }
                }

            } catch (IOException e) {
                /// TODO: handle
            }

        }

        public void sendMessage(String message){
            out.println(message);
        }
    }

}
