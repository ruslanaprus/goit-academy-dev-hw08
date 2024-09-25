package org.example.http;

import com.sun.net.httpserver.HttpServer;
import org.example.formatter.JsonFormatter;
import org.example.crud.BaseService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import static org.example.constants.Constants.DEFAULT_PORT;

public class HttpServerFactory {
    private static final Logger logger = LoggerFactory.getLogger(HttpServerFactory.class);

    private final Map<String, BaseService<?>> serviceMap;
    private final JsonFormatter jsonFormatter;

    public HttpServerFactory(List<BaseService<?>> services, JsonFormatter jsonFormatter) {
        this.serviceMap = services.stream().collect(Collectors.toMap(BaseService::getContextPath, service -> service));
        this.jsonFormatter = jsonFormatter;
    }

    public void startServer() {
        try {
            HttpServer server = HttpServer.create(new InetSocketAddress(DEFAULT_PORT), 0);
            setupContexts(server);
            ExecutorService executorService = Executors.newCachedThreadPool();
            server.setExecutor(executorService);
            server.start();
            logger.info("HTTP server started on port {}", DEFAULT_PORT);
        } catch (IOException e) {
            logger.error("Failed to start HTTP server on port {}", DEFAULT_PORT, e);
            throw new IllegalStateException("Could not start HTTP server", e);
        }
    }

    private void setupContexts(HttpServer server) {
        serviceMap.forEach((contextPath, service) -> {
            server.createContext(contextPath, new MyHttpServer(serviceMap, jsonFormatter));
            logger.info("Context '{}' created for service '{}'", contextPath, service.getClass().getSimpleName());
        });
    }

    public void stopServer(HttpServer server) {
        if (server != null) {
            server.stop(0);
            logger.info("HTTP server stopped");
        }
    }
}