package de.hype.hypenotify.server;

import com.google.gson.JsonObject;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

public class TimerServer {
    public static void main(String[] args) throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress(8085), 0);
        server.createContext("/hypenotify/checkTimer", new CheckTimerHandler());
        server.setExecutor(Executors.newCachedThreadPool());
        System.out.println("TimerServer started on port 8085");
        server.start();
    }

    // Handler for the /checkTimer endpoint.
    static class CheckTimerHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) {
            try {
                if ("GET".equals(exchange.getRequestMethod())) {
                    String query = exchange.getRequestURI().getQuery();
                    int timerId = 0;
                    long timerTime = 0;
                    if (query != null) {
                        String[] params = query.split("&");
                        for (String param : params) {
                            String[] keyValue = param.split("=");
                            if (keyValue[0].equals("id")) {
                                timerId = Integer.parseInt(keyValue[1]);
                            } else if (keyValue[0].equals("time")) {
                                timerTime = Long.parseLong(keyValue[1]);
                            }
                        }
                    }
                    // For demonstration, assume timers with odd IDs are invalid.
                    boolean valid = (timerId % 2 == 0);
                    JsonObject jsonResponse = new JsonObject();
                    jsonResponse.addProperty("valid", valid);
                    if (!valid) {
                        // Provide a replacement timer: two minutes from now.
                        long replacementTime = System.currentTimeMillis() + 2 * 60 * 1000;
                        jsonResponse.addProperty("replacementTimer", replacementTime);
                    }
                    String response = jsonResponse.toString();
                    exchange.getResponseHeaders().set("Content-Type", "application/json");
                    exchange.sendResponseHeaders(200, response.getBytes().length);
                    OutputStream os = exchange.getResponseBody();
                    os.write(response.getBytes());
                    os.close();
                } else {
                    exchange.sendResponseHeaders(405, -1); // Method Not Allowed.
                }
                try {
                    exchange.sendResponseHeaders(500, -1);
                } catch (Exception ex) {
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}