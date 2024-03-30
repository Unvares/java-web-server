package src;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;

import src.enums.RequestMethod;

public class ClientHandler extends Thread {
  private final FileManager fileManager;
  private final DatabaseManager databaseManager;

  private final Socket clientSocket;
  private final OutputStream out;

  ClientHandler(Socket socket, FileManager fileManager, DatabaseManager databaseManager) throws IOException {
    this.clientSocket = socket;
    this.out = clientSocket.getOutputStream();
    this.fileManager = fileManager;
    this.databaseManager = databaseManager;
  }

  public void run() {
    try (InputStream in = clientSocket.getInputStream()) {
      handleRequest(in);
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

  private void handleRequest(InputStream inputStream) throws IOException {
    BufferedReader in = new BufferedReader(new InputStreamReader(inputStream));

    RequestMethod method = getRequestMethod(in);
    switch (method) {
      case RequestMethod.GET:
        handleGetRequest(in);
        break;
      case RequestMethod.POST:
        handlePostRequest(in);
        break;
      default:
        sendError(400, "Bad Request: Invalid request type");
    }
  }

  private RequestMethod getRequestMethod(BufferedReader in) {
    // Return the request type as an enum (GET or POST)
    return RequestMethod.GET;
  }

  private String getFilePath(String request) {
    // Return the file path as a string
    return "";
  }

  private String[] getCredentials(String request) {
    // Return the username and password as a string array
    return new String[2];
  }

  private void handleGetRequest(BufferedReader in) {
    // Clients may request various data types. Our server only serves files.
    // For scalability, the solution should be implemented as if server other
    // types of data, which means that we determine the requested type of data
    // based on the URL and query parameters, then use FileManager for retrieval
    // and sendResponse to send the data back
  }

  private void handlePostRequest(BufferedReader in) {
    // handle the request based on Content-Type and URL, use getCredentials to
    // extract credentials from the request, FilePath to extract data from
    // database/users.json, FilePath to write data to resources/ and
    // sendResponse to sendData back
  }

  private void HandleFileUpload() {
    // use FileManager to upload the file
  }

  private void handleLogin() {
    // use database manager to extract data from database/users.json
  }

  private void sendResponse(int statusCode, byte[] data) throws IOException {
    // Send a response back to the client
    out.close();
  }

  private void sendError(int statusCode, String message) throws IOException {
    // Send an error back to the client
    out.close();
  }
}
