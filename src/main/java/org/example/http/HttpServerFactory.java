package org.example.http;

import com.fasterxml.jackson.databind.ser.std.StdKeySerializers;
import com.sun.net.httpserver.HttpServer;
import org.example.formatter.JsonFormatter;
import org.example.service.BaseService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.InetSocketAddress;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.example.constants.Constants.DEFAULT_PORT;

public class HttpServerFactory {
    private static final Logger logger = LoggerFactory.getLogger(HttpServerFactory.class);

    private final List<BaseService> services;
    private final JsonFormatter jsonFormatter;

    public HttpServerFactory(List<BaseService> services, JsonFormatter jsonFormatter) {
        this.services = services;
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
        for (BaseService service : services) {
            String contextPath = getContextPath(service);
            if (contextPath != null) {
                server.createContext(contextPath, new MyHttpServer(service, jsonFormatter));
                logger.info("Context '{}' created for service '{}'", contextPath, service.getClass().getSimpleName());
            }
        }
    }

    private String getContextPath(BaseService service) {
        try {
            Method method = service.getClass().getMethod("getContextPath");
            return (String) method.invoke(service);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            logger.error("Failed to retrieve context path for service: {}", service.getClass().getSimpleName(), e);
            return null;
        }
    }

    public void stopServer(HttpServer server) {
        if (server != null) {
            server.stop(0);
            logger.info("HTTP server stopped");
        }
    }
}