package src;

import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

class Server {
  private final FileManager fileManager;
  private final DatabaseManager databaseManager;

  private final ServerSocket serverSocket;
  private final File rootDirectory;
  private final int portNumber;

  public static void main(String[] args) throws IOException {
    if (args.length != 2) {
      System.err.println("Error: Invalid number of arguments provided. The server will now terminate.");
      System.exit(1);
    }

    try {
      Server server = new Server(args[0], args[1]);
      server.run();
    } catch (NumberFormatException e) {
      System.err.println("Error: The provided port number is invalid. The server will now terminate.");
    } catch (IOException e) {
      System.err.println("Error: The port number is already in use. The server will now terminate.");
    } catch (IllegalArgumentException e) {
      System.err.println("Error: " + e.getMessage() + " The server will now terminate.");
    }
  }

  Server(String portNumber, String pathToPublic) throws IOException {
    this.portNumber = parsePortNumber(portNumber);
    this.rootDirectory = validateRootDirectory(pathToPublic);
    this.serverSocket = createServerSocket();
    this.fileManager = new FileManager(rootDirectory);
    this.databaseManager = new DatabaseManager();
  }

  private int parsePortNumber(String port) {
    return Integer.parseInt(port);
  }

  private ServerSocket createServerSocket() throws IOException {
    return new ServerSocket(portNumber);
  }

  private File validateRootDirectory(String directoryPath) {
    File directory = new File(directoryPath);
    if (!(directory.exists() && directory.isDirectory())) {
      throw new IllegalArgumentException("The provided directory is invalid or does not exist.");
    }
    return directory;
  }

  private void run() {
    System.out.println("Server started successfully!");
    System.out.println("Listening on port " + portNumber + "\n");

    while (true) {
      try {
        // clientSocket is closed in ClientHandler
        Socket clientSocket = serverSocket.accept();
        new ClientHandler(clientSocket, fileManager, databaseManager).start();
      } catch (IOException e) {
        System.err.println("Exception caught when trying to listen on port " + portNumber);
        System.err.println("Error: " + e.getMessage());
        closeServerSocket();
        System.exit(1);
      }
    }
  }

  private void closeServerSocket() {
    if (serverSocket != null) {
      try {
        serverSocket.close();
      } catch (IOException e) {
        System.err.println("Error closing server socket: " + e.getMessage());
      }
    }
  }
}