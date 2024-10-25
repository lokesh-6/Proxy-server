package requestHandler.httpHandler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;
import java.util.*;

import configurations.Config;
import requestPackets.HttpRequestPacket;
import requestParsers.HttpRequestParser;





public class HttpHandler implements Runnable {
    private HttpRequestPacket httpPacket;
    private HttpClient client;
    private HttpResponse<String> response;
    private OutputStream output;

    public HttpHandler(Socket clientSocket) throws IOException {
        this.output = clientSocket.getOutputStream();
        client = HttpClient.newHttpClient();
        BufferedReader input = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

        StringBuilder request = new StringBuilder();
        String line;
        int contentLength = 0;
        boolean isGetRequest = false;

// Read request headers
        while ((line = input.readLine()) != null && !line.isBlank()) {
            request.append(line).append("\r\n");

            // Check if the request method is GET
            if (line.startsWith("GET")) {
                isGetRequest = true;
            }

            // Find the Content-Length header if it's a POST/PUT request
            if (line.startsWith("Content-Length")) {
                contentLength = Integer.parseInt(line.split(": ")[1]);
            }
        }

        request.append("\r\n");

// Only read the body if it's not a GET request and Content-Length is greater than 0
        if (!isGetRequest && contentLength > 0) {
            char[] body = new char[contentLength];
            input.read(body, 0, contentLength);
            request.append(body);
        }

// Parse the request
        String httpRequest = request.toString();
        System.out.println("Received HTTP request hello:\n" + httpRequest);
        this.httpPacket = HttpRequestParser.parseRequest(httpRequest);

    }

    @Override
    public void run() {
        try {
            System.out.println("In side Requet Call");
            System.out.println("method=" + httpPacket.method+"here");
            
            if (httpPacket.method.equals("GET")) {
                getRequest();
            } else if (httpPacket.method.equals("POST")) {
                System.out.println("In side PostRequet Call");
                postRequest();
            } else if (httpPacket.method.equals("PUT")) {
                putRequest();
            } else if (httpPacket.method.equals("DELETE")) {
                deleteRequest();
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }finally {
            try {
                sendResponse();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private HttpResponse<String> getResponse() {
        return response;
    }

    private void sendResponse() throws IOException{
        output.write((String.valueOf(getResponse().statusCode())+" "+getResponse().body()).getBytes());
        output.flush();
    }

    private void getRequest() throws IOException, InterruptedException {
        System.out.println("In side GetRequet");
        HttpRequest request = buildRequestBuilder().GET().build();

        response = client.send(request, HttpResponse.BodyHandlers.ofString());

        System.out.println("Status Code: " + response.statusCode());
        System.out.println("Response Body: " + response.body());
    }

    private void postRequest() throws IOException, InterruptedException {
        System.out.println("In side PostRequest"+" "+httpPacket.body);
        HttpRequest request = buildRequestBuilder()
                .POST(HttpRequest.BodyPublishers.ofString(httpPacket.body))
                .build();
        System.out.println("Requesting to client");
        response = client.send(request, HttpResponse.BodyHandlers.ofString());

        System.out.println("Status Code: " + response.statusCode());
        System.out.println("Response Body: " + response.body());

    }

    private void putRequest() throws IOException, InterruptedException {
        HttpRequest request = buildRequestBuilder()
                .PUT(HttpRequest.BodyPublishers.ofString(httpPacket.body))
                .build();
        response = client.send(request, HttpResponse.BodyHandlers.ofString());

        System.out.println("Status Code: " + response.statusCode());
        System.out.println("Response Body: " + response.body());
    }

    private void deleteRequest() throws IOException, InterruptedException {
        HttpRequest request = buildRequestBuilder().DELETE().build();
        response = client.send(request, HttpResponse.BodyHandlers.ofString());
        System.out.println("Status Code: " + response.statusCode());
        System.out.println("Response Body: " + response.body());
    }

    private HttpRequest.Builder buildRequestBuilder() {
        System.out.println("I am inside BuildRequest Builder");

        String fullUrl = Config.httpServerHost + httpPacket.path + "?" + buildQueryString();
        System.out.println("this is the FUll URL modi  "+fullUrl);

        System.out.println("this is the FUll URL modi  "+httpPacket.headers);
        HttpRequest.Builder request = HttpRequest.newBuilder();
        try {
            request.uri(URI.create(fullUrl));
            System.out.println("this is the FUll URL modi  "+request);
            if (httpPacket.headers != null) {
                System.out.println("Setting headers:");
                // Define a set of restricted header names
                Set<String> restrictedHeaders = Set.of("Expect", "Connection", "Host", "Proxy-Authorization", "TE", "Trailer", "Upgrade");

                httpPacket.headers.forEach((key, value) -> {
                    if (!restrictedHeaders.contains(key)) {
                        request.header(key, value);
                    } else {
                        System.out.println("Skipping restricted header: " + key);
                    }
                });
                System.out.println("Headers set successfully.");

            } else {
                System.out.println("httpPacket.headers is null");
            }
        } catch (Exception e) {
            System.out.println("An error occurred while setting headers: " + e.getMessage());
            e.printStackTrace();
        }

        System.out.println("this is the request  "+request);
        return request;
    }

    private String buildQueryString() {
        return httpPacket.queryParams.entrySet().stream()
                .map(e -> e.getKey() + "=" + URLEncoder.encode(e.getValue(), StandardCharsets.UTF_8))
                .collect(Collectors.joining("&"));
    }
}
