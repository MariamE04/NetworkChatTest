package Demo;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class TCPdemo {
    public void start(int port) {
        try {
           ServerSocket server = new ServerSocket(port);
            Socket client = server.accept();
            PrintWriter out;
            BufferedReader in;
            out = new PrintWriter(client.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(client.getInputStream()));
            out.println("Hello from the TCP Server");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        new TCPdemo().start(12345);

    }
}
