package src;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
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
      try {
        clientSocket.close();
      } catch (IOException e) {
        System.out.println("Error closing client socket: " + e.getMessage());
      }
    }
  }

  private void handleRequest(InputStream inputStream) throws IOException {
    this.requestData = new RequestData(inputStream);

    RequestMethod method = requestData.getMethod();
    switch (method) {
      case RequestMethod.GET:
        System.out.println("Received GET request for " + requestData.getUrl() + " file");
        handleGetRequest();
        break;
      case RequestMethod.POST:
        System.out.println("Received POST request\n");
        handlePostRequest();
        break;
      default:
        sendResponse(StatusCode.BAD_REQUEST);
        throw new IllegalArgumentException("Invalid request type. " + method + " method is not supported");
    }
  }

  private void handleGetRequest() throws IOException {
    try {
      String path = requestData.getUrl();
      byte[] data = fileManager.getFileAsByteArray(path);
      String mimeType = fileManager.getMimeType(path);
      sendResponse(StatusCode.OK, mimeType, data);
    } catch (NoSuchFileException e) {
      sendResponse(StatusCode.NOT_FOUND);
    } catch (AccessDeniedException e) {
      sendResponse(StatusCode.UNAUTHORIZED);
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

  private byte[] createStatusLine(StatusCode statusCode) {
    return String.format("HTTP/1.1 %3d %s\r\n", statusCode.getCode(), statusCode.getPhrase()).getBytes();
  }

  private byte[] createContentTypeLine(String contentType) {
    return String.format("Content-Type: %s\r\n", contentType).getBytes();
  }

  private byte[] createBody(byte[] data) {
    String contentLengthLine = String.format("Content-Length: %d\r\n", data.length);
    String bodyLine = new String(data, StandardCharsets.UTF_8);

    return (contentLengthLine + "\r\n" + bodyLine).getBytes(StandardCharsets.UTF_8);
  }

  private void sendResponse(StatusCode statusCode, String mimeType, byte[] data) throws IOException {
    System.out.println("Sending back the following response:");
    System.out.println(statusCode.getCode() + " " + statusCode.getPhrase() + " HTTP/1.1");
    System.out.println("Content-Type: " + mimeType);
    System.out.println("Content-Length: " + data.length);

    out.write(createStatusLine(statusCode));
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

  private void sendResponse(StatusCode statusCode) throws IOException {
    out.write(createStatusLine(statusCode));
    out.write(createContentTypeLine("text/plain"));
    out.write(createBody(new byte[0]));
    out.flush();
    out.close();
  }
}
