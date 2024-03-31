package src;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import src.enums.RequestMethod;

public class RequestData {
  private final FileManager fileManager;

  private final String url;
  private final RequestMethod method;
  private final String protocol;
  private final Map<String, String> headers;
  private final Map<String, String> queryParams;
  private final byte[] body;

  RequestData(InputStream inputStream, FileManager fileManager) throws IOException {
    this.fileManager = fileManager;

    BufferedReader in = new BufferedReader(new InputStreamReader(inputStream));
    String[] parameters = in.readLine().split(" ");
    this.method = parseMethod(parameters[0]);
    this.url = parseUrl(parameters[1]);
    this.protocol = parameters[2];
    this.queryParams = parseQueryParams(in);
    this.headers = parseHeaders(in);
    this.body = parseBody(in);
  }

  private RequestMethod parseMethod(String method) {
    switch (method) {
      case "GET":
        return RequestMethod.GET;
      case "POST":
        return RequestMethod.POST;
      default:
        throw new IllegalArgumentException("Invalid method: " + method);
    }
  }

  private String parseUrl(String url) throws IOException {
    if (fileManager.isDirectory(url)) {
      return url.endsWith("/") ? url + "index.html" : url + "/index.html";
    }
    return url;
  }

  private Map<String, String> parseHeaders(BufferedReader in) throws IOException {
    Map<String, String> headers = new HashMap<>();

    String line;
    while ((line = in.readLine()) != null && !line.isEmpty()) {
      if (line.contains(":")) {
        String[] header = line.split(": ");
        headers.put(header[0], header[1]);
      }
    }

    return headers;
  }

  private Map<String, String> parseQueryParams(BufferedReader in) {
    Map<String, String> queryParams = new HashMap<>();

    if (this.url.contains("?")) {
      String[] urlParts = this.url.split("\\?");
      String query = urlParts[1];
      for (String param : query.split("&")) {
        String[] keyValue = param.split("=");
        queryParams.put(keyValue[0], keyValue[1]);
      }
    }

    return queryParams;
  }

  private byte[] parseBody(BufferedReader in) throws IOException {
    String header = headers.get("Content-Length");
    if (header == null)
      return new byte[0];
    int contentLength = Integer.parseInt(header);
    ByteArrayOutputStream body = new ByteArrayOutputStream();
    for (int i = 0; i < contentLength; i++) {
      body.write(in.read());
    }
    return body.toByteArray();
  }

  public String getUrl() {
    return this.url;
  }

  public RequestMethod getMethod() {
    return this.method;
  }

  public String getProtocol() {
    return this.protocol;
  }

  public Map<String, String> getHeaders() {
    return new HashMap<>(this.headers);
  }

  public Map<String, String> getQueryParams() {
    return new HashMap<>(this.queryParams);
  }

  public String getBodyAsString() {
    return new String(body, StandardCharsets.UTF_8);
  }

  public byte[] getBodyAsBytes() {
    return body;
  }

}
