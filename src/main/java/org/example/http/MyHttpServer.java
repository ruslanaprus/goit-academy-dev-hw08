package org.example.http;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.example.formatter.JsonFormatter;
import org.example.mapper.JsonEntityMapper;
import org.example.service.BaseService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Optional;

public class MyHttpServer implements HttpHandler {

    private static final Logger logger = LoggerFactory.getLogger(MyHttpServer.class);
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

        try {
            if (id.isPresent()) {
                invokeMethod(exchange, service, "getById", new Class<?>[]{long.class}, new Object[]{id.get()});
            } else {
                Method listMethod = service.getClass().getMethod("listAll");
                Optional<?> result = (Optional<?>) listMethod.invoke(service);

                if (result.isPresent()) {
                    sendJsonResponse(exchange, 200, jsonFormatter.objectToJson(result.get()));
                } else {
                    sendResponse(exchange, 404, "No resources found");
                }
            }
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException("Failed to handle GET request", e);
        }
    }

    private void handlePostRequest(HttpExchange exchange, BaseService service) throws Exception {
        JsonEntityMapper<?> mapper = service.getJsonEntityMapper();
        Object requestBody = jsonFormatter.jsonToObject(exchange, mapper);
        logger.info("requestBody: {}", jsonFormatter.objectToJson(requestBody, (JsonEntityMapper<Object>) mapper));

        Method createMethod = findCreateMethod(service, requestBody.getClass());

        if (createMethod == null) {
            sendResponse(exchange, 400, "No suitable 'create' method found in service");
            return;
        }

        Optional<?> result = (Optional<?>) createMethod.invoke(service, requestBody);

        if (result.isPresent()) {
            Object responseBody = result.get();

            // Since BaseService doesn't have a mapper method for specific classes, we reuse the same mapper for responseBody
            @SuppressWarnings("unchecked")
            JsonEntityMapper<Object> responseMapper = (JsonEntityMapper<Object>) service.getJsonEntityMapper();

            logger.info("result: {}", jsonFormatter.objectToJson(responseBody, responseMapper));

            sendJsonResponse(exchange, 201, jsonFormatter.objectToJson(responseBody, responseMapper));
        } else {
            sendResponse(exchange, 400, "Failed to create resource");
        }
    }

    /**
     * Utility method to find the 'create' method in the service class that matches the deserialized request body type.
     */
    private Method findCreateMethod(BaseService service, Class<?> requestBodyClass) {
        // Iterate over all declared methods in the service class
        for (Method method : service.getClass().getDeclaredMethods()) {
            if (method.getName().equals("create") && method.getParameterCount() == 1) {
                // Check if the parameter type matches the requestBodyClass
                if (method.getParameterTypes()[0].isAssignableFrom(requestBodyClass)) {
                    return method;
                }
            }
        }
        return null;
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
                sendJsonResponse(exchange, 200, jsonFormatter.objectToJson(result.get(), service.getJsonEntityMapper()));
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

    private void sendResponse(HttpExchange exchange, int statusCode, String response) {
        try {
            exchange.sendResponseHeaders(statusCode, response.getBytes(StandardCharsets.UTF_8).length);
            exchange.getResponseBody().write(response.getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            exchange.close();
        }
    }

    private void sendJsonResponse(HttpExchange exchange, int statusCode, String jsonResponse) {
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        sendResponse(exchange, statusCode, jsonResponse);
    }

    private Optional<Long> extractIdFromPath(String path) {
        String[] segments = path.split("/");
        if (segments.length > 2) {
            try {
                return Optional.of(Long.parseLong(segments[2]));
            } catch (NumberFormatException e) {
                return Optional.empty();
            }
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
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("Failed to extract name from entity", e);
        }
    }
}
