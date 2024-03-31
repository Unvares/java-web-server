package src;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

import src.enums.RequestMethod;
import src.enums.StatusCode;

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
    } catch (IOException | RuntimeException e) {
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
        System.out.println("Received GET request\n");
        handleGetRequest(in);
        break;
      case RequestMethod.POST:
        System.out.println("Received POST request\n");
        handlePostRequest(in);
        break;
      default:
        sendResponse(StatusCode.BAD_REQUEST, StatusCode.BAD_REQUEST.getPhrase(), new byte[0]);
        throw new IllegalArgumentException("Invalid request type. " + method + " method is not supported");
    }
  }

  private RequestMethod getRequestMethod(BufferedReader in) throws IOException {
    String method = in.readLine().split(" ")[0];
    switch (method) {
      case "GET":
        return RequestMethod.GET;
      case "POST":
        return RequestMethod.POST;
      default:
        throw new IllegalArgumentException("Invalid request method. Only GET and POST are supported.");
    }
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

  private void handleFileUpload() {
    // use FileManager to upload the file
  }

  private void handleLogin() {
    // use database manager to extract data from database/users.json
  }

  private byte[] createStatusLine(StatusCode statusCode, String phrase) {
    return String.format("HTTP/1.1 %3d %s\r\n", statusCode.getCode(), phrase).getBytes();
  }

  private byte[] createContentTypeLine(String contentType) {
    return String.format("Content-Type: %s\r\n", contentType).getBytes();
  }

  private byte[] createBody(byte[] data) {
    String contentLengthLine = String.format("Content-Length: %d\r\n", data.length);
    String bodyLine = new String(data, StandardCharsets.UTF_8);

    return (contentLengthLine + "\r\n" + bodyLine).getBytes(StandardCharsets.UTF_8);
  }

  private void sendResponse(StatusCode statusCode, String phrase, byte[] data) throws IOException {
    out.write(createStatusLine(statusCode, phrase));

    switch (statusCode) {
      case StatusCode.OK:
        break;
      case StatusCode.UNAUTHORIZED:
        break;
      case StatusCode.FOUND:
        break;
      case StatusCode.BAD_REQUEST:
        out.write(createContentTypeLine("text/plain"));
        break;
      case StatusCode.NOT_FOUND:
        break;
      case StatusCode.INTERNAL_SERVER_ERROR:
        break;
      default:
        throw new IllegalArgumentException("Invalid status code");
    }
    out.write(createBody(data));
    out.flush();
    out.close();
  }
}
