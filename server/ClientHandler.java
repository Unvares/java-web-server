package server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

public class ClientHandler extends Thread {
    private final Socket clientSocket;

    ClientHandler(Socket socket) {
        this.clientSocket = socket;
    }

    public void run() {
        try (BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));) {
            // placeholder: prints the request and ends the thread
            // to be replaced by the desires functionality
            while (true) {
                String inputLine = in.readLine();
                System.out.println(inputLine);
                if (inputLine.isEmpty())
                    break;
            }
        } catch (IOException e) {
            System.out.println("Exception caught when trying to handle the client request");
            System.out.println("Error: " + e.getMessage());
            System.out.println("Interrupting " + this.getName());
            System.out.println();
            interrupt();
        } finally {
            try {
                clientSocket.close();
            } catch (IOException e) {
                System.out.println("Error closing client socket: " + e.getMessage());
            }
        }
    }
}
