package org.example.http;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.example.formatter.JsonFormatter;
import org.example.service.BaseService;

import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Optional;

public class MyHttpServer implements HttpHandler {

    private final Map<String, BaseService> serviceMap;
    private final JsonFormatter jsonFormatter;

    public MyHttpServer(Map<String, BaseService> serviceMap, JsonFormatter jsonFormatter) {
        this.serviceMap = serviceMap;
        this.jsonFormatter = jsonFormatter;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();
        String path = exchange.getRequestURI().getPath();
        String context = extractContextFromPath(path);

        BaseService service = serviceMap.get(context);
        if (service == null) {
            sendResponse(exchange, 404, "Service not found");
            return;
        }

        try {
            switch (method) {
                case "GET" -> handleGetRequest(exchange, service, path);
                case "POST" -> handlePostRequest(exchange, service);
                case "PUT" -> handlePutRequest(exchange, service, path);
                case "DELETE" -> handleDeleteRequest(exchange, service, path);
                default -> sendResponse(exchange, 405, "Method Not Allowed");
            }
        } catch (Exception e) {
            handleServerError(exchange, e);
        }
    }

    private void handleGetRequest(HttpExchange exchange, BaseService service, String path) throws IOException {
        Optional<Long> id = extractIdFromPath(path);
        if (id.isPresent()) {
            invokeMethod(exchange, service, "getById", new Class<?>[]{long.class}, new Object[]{id.get()});
        } else {
            invokeMethod(exchange, service, "listAll", new Class<?>[]{}, new Object[]{});
        }
    }

    private void handlePostRequest(HttpExchange exchange, BaseService service) throws IOException {
        Object entity = jsonFormatter.jsonToObject(exchange, service.getJsonEntityMapper());
        invokeMethod(exchange, service, "create", new Class<?>[]{String.class}, new Object[]{getNameFromEntity(entity)});
    }

    private void handlePutRequest(HttpExchange exchange, BaseService service, String path) throws IOException {
        Optional<Long> id = extractIdFromPath(path);
        if (id.isPresent()) {
            Object entity = jsonFormatter.jsonToObject(exchange, service.getJsonEntityMapper());
            invokeMethod(exchange, service, "setName", new Class<?>[]{long.class, String.class}, new Object[]{id.get(), getNameFromEntity(entity)});
        } else {
            sendResponse(exchange, 400, "Invalid ID");
        }
    }

    private void handleDeleteRequest(HttpExchange exchange, BaseService service, String path) throws IOException {
        Optional<Long> id = extractIdFromPath(path);
        if (id.isPresent()) {
            invokeMethod(exchange, service, "deleteById", new Class<?>[]{long.class}, new Object[]{id.get()});
        } else {
            sendResponse(exchange, 400, "Invalid ID");
        }
    }

    private void invokeMethod(HttpExchange exchange, BaseService service, String methodName, Class<?>[] paramTypes, Object[] params) throws IOException {
        try {
            Method method = service.getClass().getMethod(methodName, paramTypes);
            Optional<?> result = (Optional<?>) method.invoke(service, params);

            if (result.isPresent()) {
                sendResponse(exchange, 200, jsonFormatter.objectToJson(result.get(), service.getJsonEntityMapper()));
            } else {
                sendResponse(exchange, 404, "Entity not found or operation failed");
            }
        } catch (Exception e) {
            handleServerError(exchange, e);
        }
    }

    private void handleServerError(HttpExchange exchange, Exception e) throws IOException {
        sendResponse(exchange, 500, "Internal Server Error: " + e.getMessage());
    }

    private void sendResponse(HttpExchange exchange, int statusCode, String responseText) throws IOException {
        byte[] responseBytes = responseText.getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(statusCode, responseBytes.length);
        exchange.getResponseBody().write(responseBytes);
        exchange.close();
    }

    private Optional<Long> extractIdFromPath(String path) {
        try {
            String[] segments = path.split("/");
            if (segments.length > 2) {
                return Optional.of(Long.parseLong(segments[2]));
            }
        } catch (NumberFormatException e) {
            return Optional.empty();
        }
        return Optional.empty();
    }

    private String extractContextFromPath(String path) {
        String[] segments = path.split("/");
        return segments.length > 1 ? "/" + segments[1] : "";
    }

    private String getNameFromEntity(Object entity) {
        try {
            Method getNameMethod = entity.getClass().getMethod("getName");
            return (String) getNameMethod.invoke(entity);
        } catch (Exception e) {
            throw new RuntimeException("Failed to get name from entity", e);
        }
    }
}