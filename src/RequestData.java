package src;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

import src.enums.RequestMethod;

public class RequestData {
    private final String url;
    private final RequestMethod method;
    private final String protocol;
    private final Map<String, String> headers;
    private final Map<String, String> queryParams;

    RequestData(InputStream inputStream) throws IOException {
        BufferedReader in = new BufferedReader(new InputStreamReader(inputStream));
        String[] parameters = in.readLine().split(" ");
        this.url = parameters[0];
        this.method = parseMethod(parameters[1]);
        this.protocol = parameters[2];
        this.headers = parseHeaders(in);
        this.queryParams = parseQueryParams(in);
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

}
