package src;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.file.AccessDeniedException;
import java.nio.file.NoSuchFileException;

import src.enums.RequestMethod;
import src.enums.StatusCode;

public class ClientHandler extends Thread {
  private final FileManager fileManager;
  private final DatabaseManager databaseManager;
  private RequestData requestData;

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
      this.requestData = new RequestData(in, fileManager);
      handleRequest();
    } catch (IOException | RuntimeException e) {
      System.err.println("Exception caught when trying to handle the client request");
      System.err.println("Error: " + e.getMessage());
      System.err.println("Interrupting " + this.getName());
      System.err.println();
      e.printStackTrace();
      try {
        writeResponse(StatusCode.INTERNAL_SERVER_ERROR);
      } catch (IOException e1) {
        e1.printStackTrace();
      }
      sendResponse();
      interrupt();
    } finally {
      sendResponse();
      closeClientSocket();
    }
  }

  private void handleRequest() throws IOException {
    RequestMethod method = requestData.getMethod();
    switch (method) {
      case RequestMethod.GET:
        System.out.println("Received GET request for " + requestData.getUrl());
        handleGetRequest();
        break;
      case RequestMethod.POST:
        System.out.println("Received POST request for " + requestData.getUrl());
        handlePostRequest();
        break;
      default:
        writeResponse(StatusCode.BAD_REQUEST);
        throw new IllegalArgumentException("Invalid request type. " + method + " method is not supported");
    }
  }

  private void handleGetRequest() throws IOException {
    try {
      String path = requestData.getUrl();
      switch (path) {
        case "/clown.png":
          handleRedirection("redirect/clown.png");
          break;
        default:
          byte[] data = fileManager.getContentAsBytes(path);
          String mimeType = fileManager.getMimeType(path);
          writeResponse(StatusCode.OK, mimeType, data);
      }
    } catch (NoSuchFileException e) {
      writeResponse(StatusCode.NOT_FOUND);
    } catch (AccessDeniedException e) {
      writeResponse(StatusCode.UNAUTHORIZED);
    }
  }

  private void handlePostRequest() throws IOException {
    String path = requestData.getUrl();
    switch (path) {
      case "/login":
        handleLogin();
        break;
      default:
        throw new IllegalArgumentException("Invalid path: " + path);
    }
  }

  private void handleRedirection(String path) throws IOException {
    String mimeType = fileManager.getMimeType(path);
    writeResponse(StatusCode.FOUND, mimeType, path);
  }

  private void handleLogin() throws IOException {
    String[] credentials = getCredentials(requestData.getBodyAsString());
    boolean result = databaseManager.validateCredentials(credentials[0], credentials[1]);
    if (result == true)
      writeResponse(StatusCode.OK);
    if (result == false)
      writeResponse(StatusCode.UNAUTHORIZED);
  }

  private String[] getCredentials(String body) {
    String[] credentials = body.split("&");
    String username = "";
    String password = "";
    for (String credential : credentials) {
      if (credential.startsWith("username=")) {
        username = credential.split("=")[1];
      } else if (credential.startsWith("password=")) {
        password = credential.split("=")[1];
      }
    }
    return new String[] { username, password };
  }

  private String createStatusLine(StatusCode statusCode) {
    int status = statusCode.getCode();
    String phrase = statusCode.getPhrase();
    return String.format("HTTP/1.1 %3d %s\r\n", status, phrase);
  }

  private String createContentTypeLine(String contentType) {
    return "Content-Type: " + contentType + "\r\n";
  }

  private String createContentLengthLine(byte[] data) {
    return "Content-Length: " + data.length + "\r\n\r\n";
  }

  private void writeResponse(StatusCode statusCode, String mimeType, byte[] data) throws IOException {
    String statusLine = createStatusLine(statusCode);
    String contentTypeLine = createContentTypeLine(mimeType);
    String contentLengthLine = createContentLengthLine(data);

    System.out.println("Sending back the following response:");
    System.out.print(statusLine);
    System.out.print(contentTypeLine);
    System.out.print(contentLengthLine);

    out.write(statusLine.getBytes());
    out.write(contentTypeLine.getBytes());
    out.write(contentLengthLine.getBytes());
    out.write(data);
  }

  private void writeResponse(StatusCode statusCode, String mimeType, String redirectUrl) throws IOException {
    String statusLine = createStatusLine(statusCode);
    String locationLine = "Location: " + redirectUrl + "\r\n";
    String contentTypeLine = createContentTypeLine(mimeType);

    System.out.println("Sending back the following response:");
    System.out.print(statusLine);
    System.out.print(locationLine);
    System.out.print(contentTypeLine + "\n");

    out.write(statusLine.getBytes());
    out.write(locationLine.getBytes());
    out.write(contentTypeLine.getBytes());
  }

  private void writeResponse(StatusCode statusCode) throws IOException {
    writeResponse(statusCode, "text/plain", new byte[0]);
  }

  private void sendResponse() {
    try {
      out.flush();
      out.close();
    } catch (IOException e) {
      System.err.println("Error closing output stream: " + e.getMessage());
    }
  }

  private void closeClientSocket() {
    try {
      clientSocket.close();
    } catch (IOException e) {
      System.err.println("Error closing client socket: " + e.getMessage());
    }
  }
}
