package server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

class Server {
  private static String pathToPublic;
  private static int portNumber;

  public static void main(String[] args) throws IOException {
    if (args.length != 2) {
      System.err.println("Invalid number of arguments provided. The program will now terminate.");
      System.exit(1);
    }

    portNumber = Integer.parseInt(args[0]);
    pathToPublic = args[1];

    ServerSocket serverSocket = new ServerSocket(portNumber);
    System.out.println("Server started successfully!");
    System.out.println("Listening on port " + portNumber + "\n");

    while (true) {
      try {
        Socket clientSocket = serverSocket.accept();
        new ClientHandler(clientSocket).start();
      } catch (IOException e) {
        System.out.println("Exception caught when trying to listen on port " + portNumber);
        System.out.println("Error: " + e.getMessage());
        serverSocket.close();
        System.exit(1);
      }
    }
  }
}