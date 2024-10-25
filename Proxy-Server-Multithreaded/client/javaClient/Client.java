package client.javaClient;
import java.net.*;
import java.util.*;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

//public class Client {
//    public static void main(String args[]) throws IOException {
//        String hostname="localhost";
//        int serverPort=8083;
//
//        Socket clientSocket=new Socket(hostname,serverPort);
//        System.out.println("Connected to server:"+hostname+" "+serverPort);
//        // send data to server
//        String body = "{\"username\": \"johndoe\", \"email\": \"johndoe@example.com\"}";
//        String httpGETRequest = "GET /?include=details&expand=all HTTP/1.1\r\n"
//                              + "Host: localhost\r\n"
//                              + "User-Agent: JavaClient/1.0\r\n"
//                              + "Content-Type: application/json\r\n"
//                              + "Content-Length: " + body.length() + "\r\n"
//                              + "\r\n"
//                              + body;
//
//
//        String httpPOSTRequest = "POST /?include=details&expand=all HTTP/1.1\r\n"
//                               + "Host: example.com\r\n"
//                               + "User-Agent: JavaClient/1.0\r\n"
//                               + "Content-Type: application/json\r\n"
//                               + "Content-Length: " + body.length() + "\r\n"
//                               + "\r\n"
//                               + body;
//
//        String httpPUTRequest = "PUT /?include=details&expand=all HTTP/1.1\r\n"
//                              + "Host: example.com\r\n"
//                              + "User-Agent: JavaClient/1.0\r\n"
//                              + "Content-Type: application/json\r\n"
//                              + "Content-Length: " + body.length() + "\r\n"
//                              + "\r\n"
//                              + body;
//
//        String httpDELETERequest = "DELETE /?include=details&expand=all HTTP/1.1\r\n"
//                                 + "Host: example.com\r\n"
//                                 + "User-Agent: JavaClient/1.0\r\n"
//                                 + "Content-Type: application/json\r\n"
//                                 + "Content-Length: " + body.length() + "\r\n"
//                                 + "\r\n"
//                                 + body;
//
//        OutputStream output=clientSocket.getOutputStream();
//
//        // sent time to server
//        Date date = new Date();
//
//        output.write(httpPOSTRequest.getBytes());
//        output.flush();
//
//
//
//        InputStream inputStream = clientSocket.getInputStream();
//
//
//        byte[] data = new byte[1024];
//        inputStream.read(data);
//        System.out.println("Hello Got Data");
//        System.out.println("Data from server: "+new String(data));
//        System.out.println("Time taken to get response from server: "+(new Date().getTime()-date.getTime())+"ms");
//
//        clientSocket.close();
//    }
//}
// equivalent curl command for this request:-

// curl -X POST "http://example.com/api/users/123/profile?include=details&expand=all" \
//      -H "Host: example.com" \
//      -H "User-Agent: JavaClient/1.0" \
//      -H "Content-Type: application/json" \
//      -d '{"username": "johndoe", "email": "johndoe@example.com"}'


public class Client {
    public static void main(String args[]) throws IOException {
        String hostname = "localhost";
        int serverPort = 8083;

        Socket clientSocket = new Socket(hostname, serverPort);
        System.out.println("Connected to server: " + hostname + " " + serverPort);

        Scanner scanner = new Scanner(System.in);

        System.out.print("Enter HTTP method (GET, POST, PUT, DELETE): ");
        String httpMethod = scanner.nextLine().trim().toUpperCase();

        String body = "";
        if (!httpMethod.equals("GET")) {
            System.out.print("Enter JSON body (leave empty for none): ");
            body = scanner.nextLine();
        }

        String requestLine = "/" + "?include=details&expand=all HTTP/1.1\r\n";
        String hostHeader = "Host: localhost\r\n"; // Change to target host if necessary
        String userAgentHeader = "User-Agent: JavaClient/1.0\r\n";
        String contentTypeHeader = "Content-Type: application/json\r\n";
        String contentLengthHeader = "Content-Length: " + body.length() + "\r\n";

        String httpRequest = httpMethod + " " + requestLine
                + hostHeader
                + userAgentHeader
                + contentTypeHeader
                + contentLengthHeader
                + "\r\n"
                + body;

        OutputStream output = clientSocket.getOutputStream();

        // Send the request
        Date date = new Date();
        output.write(httpRequest.getBytes());
        output.flush();

        // Receiving response from the server
        InputStream inputStream = clientSocket.getInputStream();
        byte[] data = new byte[1024];
        int bytesRead = inputStream.read(data);

        if (bytesRead != -1) {
            System.out.println("Hello Got Data");
            System.out.println("Data from server: " + new String(data, 0, bytesRead));
            System.out.println("Time taken to get response from server: " + (new Date().getTime() - date.getTime()) + "ms");
        } else {
            System.out.println("No data received from server.");
        }

        clientSocket.close();
        scanner.close();
    }
}

