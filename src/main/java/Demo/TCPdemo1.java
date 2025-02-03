package Demo;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class TCPdemo1 {
    private PrintWriter out;
    private BufferedReader in;
    private ServerSocket server;
    private Socket clientHandler;

    public void start(int port){
        try {
            server = new ServerSocket(port);
            System.out.println("Server is started and runing on port " + port);
            clientHandler = server.accept();
            out = new PrintWriter(clientHandler.getOutputStream());
            in = new BufferedReader(new InputStreamReader(clientHandler.getInputStream()));
            out.println("Hello new client. Greeting from the server");
            String inputLine;

            while((inputLine = in.readLine()) != null){
                System.out.println("Message from client:  " + inputLine);
                if("bye".equals(inputLine)) {
                    out.println("Good bye. i am shutting sown");
                    break;
                }
                    out.println("Echo: " + inputLine);

            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        new TCPdemo1().start(12345);
    }

}
