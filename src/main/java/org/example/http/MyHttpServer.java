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
        extractIdFromPath(path).ifPresentOrElse(
                id -> {
                    try {
                        Map<String, String> requestBodyMap = jsonFormatter.jsonToObject(exchange, Map.class);
                        String newName = requestBodyMap.get("name");

                        if (newName == null || newName.isEmpty()) {
                            sendResponse(exchange, 400, "Name field is required");
                            return;
                        }

                        Optional<Long> result = (Optional<Long>) service.getClass().getMethod("setName", long.class, String.class)
                                .invoke(service, id, newName);

                        result.map(affectedRows -> {
                            sendResponse(exchange, 200, "Updated rows: " + affectedRows);
                            return affectedRows;
                        }).orElseGet(() -> {
                            sendResponse(exchange, 404, "Entity not found or no rows updated");
                            return 0L;
                        });

                    } catch (ReflectiveOperationException e) {
                        handleServerError(exchange, e);
                    } catch (RuntimeException e) {
                        sendResponse(exchange, 400, "Invalid request body: " + e.getMessage());
                    }
                },
                () -> sendResponse(exchange, 400, "Invalid or missing ID")
        );
    }

    private void handleDeleteRequest(HttpExchange exchange, BaseService service, String path) throws IOException {
        extractIdFromPath(path).ifPresentOrElse(
                id -> {
                    try {
                        Optional<Long> result = (Optional<Long>) service.getClass().getMethod("deleteById", long.class)
                                .invoke(service, id);

                        result.map(affectedRows -> {
                            sendResponse(exchange, 200, "Deleted rows: " + affectedRows);
                            return affectedRows;
                        }).orElseGet(() -> {
                            sendResponse(exchange, 404, "Entity not found or no rows deleted");
                            return 0L;
                        });

                    } catch (ReflectiveOperationException e) {
                        handleServerError(exchange, e);
                    }
                },
                () -> sendResponse(exchange, 400, "Invalid ID")
        );
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

    private void handleServerError(HttpExchange exchange, Exception e) {
        sendResponse(exchange, 500, "Internal Server Error: " + e.getMessage());
    }

    private void sendResponse(HttpExchange exchange, int statusCode, String responseText) {
        byte[] responseBytes = responseText.getBytes(StandardCharsets.UTF_8);
        try {
            exchange.sendResponseHeaders(statusCode, responseBytes.length);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        try {
            exchange.getResponseBody().write(responseBytes);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
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