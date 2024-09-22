package org.example.http;

import com.sun.net.httpserver.HttpServer;
import org.example.formatter.JsonFormatter;
import org.example.service.ClientService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;

public class HttpServerFactory {
    private static final Logger logger = LoggerFactory.getLogger(HttpServerFactory.class);

    public static void startHttpServer(ClientService clientService) {
        int port = 9001;
        JsonFormatter jsonFormatter = new JsonFormatter();

        HttpServer server = null;
        try {
            server = HttpServer.create(new InetSocketAddress(port), 0);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        server.createContext("/clients", new MyHttpServer(clientService, jsonFormatter));
        server.setExecutor(null);
        server.start();

        logger.info("Server started on port " + port);
    }
}

