import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

class Server {
  static private String pathToPublic;

  public static void main(String[] args) throws IOException {
    if (args.length != 2) {
      System.err.println("Invalid number of arguments provided. The program will now terminate.");
      System.exit(1);
    }

    pathToPublic = args[1];

    int portNumber = Integer.parseInt(args[0]);
    ServerSocket serverSocket = new ServerSocket(Integer.parseInt(args[0]));
    System.out.println("Server started successfully!");
    System.out.println("Listening on port " + portNumber + "\n");

    while (true) {
      try (
          Socket clientSocket = serverSocket.accept();
          PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
          BufferedReader in = new BufferedReader(
              new InputStreamReader(clientSocket.getInputStream()));) {
        while (true) {
          String inputLine = in.readLine();
          System.out.println(inputLine);
          if (inputLine.isEmpty())
            break;
        }
      } catch (IOException e) {
        System.out.println("Exception caught when trying to listen on port " + portNumber);
        System.out.println("Error: " + e.getMessage());
        serverSocket.close();
        System.out.println(Thread.getAllStackTraces().keySet());
        System.exit(1);
      }
    }
  }
}