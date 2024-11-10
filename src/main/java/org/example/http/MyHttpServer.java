package org.example.http;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.example.crud.BaseService;
import org.example.formatter.JsonFormatter;
import org.example.mapper.json.JsonEntityMapper;
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
    private final Map<String, BaseService<?>> serviceMap;
    private final JsonFormatter jsonFormatter;

    public MyHttpServer(Map<String, BaseService<?>> serviceMap, JsonFormatter jsonFormatter) {
        this.serviceMap = serviceMap;
        this.jsonFormatter = jsonFormatter;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();
        String path = exchange.getRequestURI().getPath();
        String context = extractContextFromPath(path);

        logger.info("Received {} request for path: {}", method, path);

        BaseService<?> service = serviceMap.get(context);
        if (service == null) {
            logger.warn("Service not found for path: {}", context);
            sendResponse(exchange, 404, "Service not found");
            return;
        }

        try {
            switch (method) {
                case "GET" -> handleGetRequest(exchange, service, path);
                case "POST" -> handlePostRequest(exchange, service);
                case "PUT" -> handlePutRequest(exchange, service, path);
                case "DELETE" -> handleDeleteRequest(exchange, service, path);
                default -> {
                    logger.warn("Unsupported method: {}", method);
                    sendResponse(exchange, 405, "Method Not Allowed");
                }
            }
        } catch (Exception e) {
            logger.error("Error processing request", e);
            handleServerError(exchange, e);
        }
    }

    private void handleGetRequest(HttpExchange exchange, BaseService<?> service, String path) throws IOException {
        Optional<Long> id = extractIdFromPath(path);

        try {
            if (id.isPresent()) {
                logger.info("Fetching resource by ID: {}", id.get());
                invokeMethod(exchange, service, "getById", new Class<?>[]{long.class}, new Object[]{id.get()});
            } else {
                logger.info("Fetching all resources for service: {}", service.getClass().getSimpleName());
                Method listMethod = service.getClass().getMethod("listAll");
                Optional<?> result = (Optional<?>) listMethod.invoke(service);

                if (result.isPresent()) {
                    sendJsonResponse(exchange, 200, jsonFormatter.objectToJson(result.get()));
                } else {
                    logger.warn("No resources found for service: {}", service.getClass().getSimpleName());
                    sendResponse(exchange, 404, "No resources found");
                }
            }
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            logger.error("Failed to handle GET request", e);
            throw new RuntimeException("Failed to handle GET request", e);
        }
    }

    private void handlePostRequest(HttpExchange exchange, BaseService<?> service) throws Exception {
        JsonEntityMapper<?> mapper = service.getJsonEntityMapper();
        Object requestBody = jsonFormatter.jsonToObject(exchange, mapper);
        logger.info("POST request received. Body: {}", jsonFormatter.objectToJson(requestBody));

        Method createMethod = findCreateMethod(service, requestBody.getClass());

        if (createMethod == null) {
            logger.warn("No suitable 'create' method found in service: {}", service.getClass().getSimpleName());
            sendResponse(exchange, 400, "No suitable 'create' method found in service");
            return;
        }

        Optional<?> result = (Optional<?>) createMethod.invoke(service, requestBody);

        if (result.isPresent()) {
            Object responseBody = result.get();
            logger.info("Resource created: {}", jsonFormatter.objectToJson(responseBody));
            sendJsonResponse(exchange, 201, jsonFormatter.objectToJson(responseBody));
        } else {
            logger.warn("Failed to create resource for service: {}", service.getClass().getSimpleName());
            sendResponse(exchange, 400, "Failed to create resource");
        }
    }

    private Method findCreateMethod(BaseService<?> service, Class<?> requestBodyClass) {
        Class<?> currentClass = service.getClass();

        while (currentClass != null) {
            for (Method method : currentClass.getDeclaredMethods()) {
                if (method.getName().equals("create") && method.getParameterCount() == 1) {
                    if (method.getParameterTypes()[0].isAssignableFrom(requestBodyClass)) {
                        return method;
                    }
                }
            }
            currentClass = currentClass.getSuperclass();
        }
        return null;
    }

    private void handlePutRequest(HttpExchange exchange, BaseService<?> service, String path) throws IOException {
        extractIdFromPath(path).ifPresentOrElse(
                id -> {
                    try {
                        Map<String, String> requestBodyMap = jsonFormatter.jsonToObject(exchange, Map.class);
                        String newName = requestBodyMap.get("name");

                        if (newName == null || newName.isEmpty()) {
                            logger.warn("PUT request missing 'name' field for ID: {}", id);
                            sendResponse(exchange, 400, "Name field is required");
                            return;
                        }

                        Optional<Long> result = (Optional<Long>) service.getClass().getMethod("setName", long.class, String.class)
                                .invoke(service, id, newName);

                        result.map(affectedRows -> {
                            logger.info("Updated {} rows for ID: {}", affectedRows, id);
                            sendResponse(exchange, 200, "Updated rows: " + affectedRows);
                            return affectedRows;
                        }).orElseGet(() -> {
                            logger.warn("No rows updated for ID: {}", id);
                            sendResponse(exchange, 404, "Entity not found or no rows updated");
                            return 0L;
                        });

                    } catch (ReflectiveOperationException e) {
                        logger.error("Error updating entity with ID: {}", id, e);
                        handleServerError(exchange, e);
                    }
                },
                () -> {
                    logger.warn("Invalid or missing ID in PUT request");
                    sendResponse(exchange, 400, "Invalid or missing ID");
                }
        );
    }

    private void handleDeleteRequest(HttpExchange exchange, BaseService<?> service, String path) throws IOException {
        extractIdFromPath(path).ifPresentOrElse(
                id -> {
                    try {
                        Optional<Long> result = (Optional<Long>) service.getClass().getMethod("deleteById", long.class)
                                .invoke(service, id);

                        result.map(affectedRows -> {
                            logger.info("Deleted {} rows for ID: {}", affectedRows, id);
                            sendResponse(exchange, 200, "Deleted rows: " + affectedRows);
                            return affectedRows;
                        }).orElseGet(() -> {
                            logger.warn("No rows deleted for ID: {}", id);
                            sendResponse(exchange, 404, "Entity not found or no rows deleted");
                            return 0L;
                        });

                    } catch (ReflectiveOperationException e) {
                        logger.error("Error deleting entity with ID: {}", id, e);
                        handleServerError(exchange, e);
                    }
                },
                () -> {
                    logger.warn("Invalid ID in DELETE request");
                    sendResponse(exchange, 400, "Invalid ID");
                }
        );
    }

    private void invokeMethod(HttpExchange exchange, BaseService<?> service, String methodName, Class<?>[] paramTypes, Object[] params) throws IOException {
        try {
            Method method = service.getClass().getMethod(methodName, paramTypes);
            Optional<?> result = (Optional<?>) method.invoke(service, params);

            if (result.isPresent()) {
                logger.info("{} method executed successfully", methodName);
                sendJsonResponse(exchange, 200, jsonFormatter.objectToJson(result.get(), service.getJsonEntityMapper()));
            } else {
                logger.warn("{} method failed or entity not found", methodName);
                sendResponse(exchange, 404, "Entity not found or operation failed");
            }
        } catch (Exception e) {
            logger.error("Error executing method: {}", methodName, e);
            handleServerError(exchange, e);
        }
    }

    private void handleServerError(HttpExchange exchange, Exception e) {
        logger.error("Internal server error", e);
        sendResponse(exchange, 500, "Internal Server Error: " + e.getMessage());
    }

    private void sendResponse(HttpExchange exchange, int statusCode, String response) {
        try {
            exchange.sendResponseHeaders(statusCode, response.getBytes(StandardCharsets.UTF_8).length);
            exchange.getResponseBody().write(response.getBytes(StandardCharsets.UTF_8));
            logger.info("Response sent with status code: {}", statusCode);
        } catch (IOException e) {
            logger.error("Error sending response", e);
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
                logger.warn("Failed to parse ID from path: {}", path);
            }
        }
        return Optional.empty();
    }

    private String extractContextFromPath(String path) {
        String[] segments = path.split("/");
        return segments.length > 1 ? "/" + segments[1] : "";
    }
}