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
      handleRequest(in);
    } catch (IOException | RuntimeException e) {
      System.out.println("Exception caught when trying to handle the client request");
      System.out.println("Error: " + e.getMessage());
      System.out.println("Interrupting " + this.getName());
      System.out.println();
      e.printStackTrace();
      interrupt();
    } finally {
      sendResponse();
      closeClientSocket();
    }
  }

  private void handleRequest(InputStream inputStream) throws IOException {
    this.requestData = new RequestData(inputStream, fileManager);

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
      byte[] data = fileManager.getContentAsBytes(path);
      String mimeType = fileManager.getMimeType(path);
      writeResponse(StatusCode.OK, mimeType, data);
    } catch (NoSuchFileException e) {
      writeResponse(StatusCode.NOT_FOUND);
    } catch (AccessDeniedException e) {
      writeResponse(StatusCode.UNAUTHORIZED);
    }
  }

  private void handlePostRequest() {
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
    System.out.println();

    out.write(statusLine.getBytes());
    out.write(contentTypeLine.getBytes());
    out.write(contentLengthLine.getBytes());
    out.write(data);
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
