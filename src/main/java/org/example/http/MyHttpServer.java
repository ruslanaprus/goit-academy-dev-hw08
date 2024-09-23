package org.example.http;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.example.formatter.JsonFormatter;
import org.example.service.BaseService;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

public class MyHttpServer implements HttpHandler {
    private final BaseService service;
    private final JsonFormatter jsonFormatter;

    public MyHttpServer(BaseService service, JsonFormatter jsonFormatter) {
        this.service = service;
        this.jsonFormatter = jsonFormatter;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();
        String path = exchange.getRequestURI().getPath();
        try {
            switch (method) {
                case "GET":
                    handleGetRequest(exchange, path);
                    break;
                case "POST":
                    handlePostRequest(exchange);
                    break;
                case "PUT":
                    handlePutRequest(exchange, path);
                    break;
                case "DELETE":
                    handleDeleteRequest(exchange, path);
                    break;
                default:
                    sendResponse(exchange, 405, "Method Not Allowed");
            }
        } catch (Exception e) {
            handleServerError(exchange, e);
        }
    }

    // Handle GET request (Fetch by ID or list all)
    private void handleGetRequest(HttpExchange exchange, String path) throws Exception {
        String[] pathComponents = path.split("/");

        if (pathComponents.length == 3) {
            long id = Long.parseLong(pathComponents[2]);
            Method method = service.getClass().getMethod("getClientById", long.class);
            Optional<?> result = (Optional<?>) method.invoke(service, id);
            if (result.isPresent()) {
                sendResponse(exchange, 200, jsonFormatter.objectToJson(result.get()));
            } else {
                sendResponse(exchange, 404, "Resource not found");
            }
        } else {
            Method listMethod = service.getClass().getMethod("listAllClients");
            Optional<?> result = (Optional<?>) listMethod.invoke(service);
            if (result.isPresent()) {
                sendResponse(exchange, 200, jsonFormatter.objectToJson(result.get()));
            } else {
                sendResponse(exchange, 404, "No resources found");
            }
        }
    }

    // Handle POST request (Create a new resource)
    private void handlePostRequest(HttpExchange exchange) throws Exception {
        Object requestBody = jsonFormatter.jsonToObject(exchange, service.getClass());
        Method method = service.getClass().getMethod("createClient", requestBody.getClass());

        Optional<?> result = (Optional<?>) method.invoke(service, requestBody);
        if (result.isPresent()) {
            sendResponse(exchange, 201, jsonFormatter.objectToJson(result.get()));
        } else {
            sendResponse(exchange, 400, "Failed to create resource");
        }
    }

    // Handle PUT request (Update a resource by ID)
    private void handlePutRequest(HttpExchange exchange, String path) throws Exception {
        String[] pathComponents = path.split("/");

        if (pathComponents.length == 3) {
            long id = Long.parseLong(pathComponents[2]);
            Object requestBody = jsonFormatter.jsonToObject(exchange, service.getClass());
            Method method = service.getClass().getMethod("setClientName", long.class, requestBody.getClass());

            Optional<Boolean> updateSuccess = (Optional<Boolean>) method.invoke(service, id, requestBody);
            if (updateSuccess.isPresent() && updateSuccess.get()) {
                sendResponse(exchange, 200, "Resource updated successfully");
            } else {
                sendResponse(exchange, 404, "Resource not found or update failed");
            }
        } else {
            sendResponse(exchange, 400, "Bad Request");
        }
    }

    // Handle DELETE request (Delete a resource by ID)
    private void handleDeleteRequest(HttpExchange exchange, String path) throws Exception {
        String[] pathComponents = path.split("/");

        if (pathComponents.length == 3) {
            long id = Long.parseLong(pathComponents[2]);
            Method method = service.getClass().getMethod("deleteClientById", long.class);

            Optional<Boolean> deleteSuccess = (Optional<Boolean>) method.invoke(service, id);
            if (deleteSuccess.isPresent() && deleteSuccess.get()) {
                sendResponse(exchange, 200, "Resource deleted successfully");
            } else {
                sendResponse(exchange, 404, "Resource not found or deletion failed");
            }
        } else {
            sendResponse(exchange, 400, "Bad Request");
        }
    }

    // Send the response to the client
    private void sendResponse(HttpExchange exchange, int statusCode, String response) throws IOException {
        exchange.sendResponseHeaders(statusCode, response.getBytes(StandardCharsets.UTF_8).length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(response.getBytes(StandardCharsets.UTF_8));
        }
    }

    // Handle server error
    private void handleServerError(HttpExchange exchange, Exception e) throws IOException {
        String errorMessage = "Internal server error: " + e.getMessage();
        sendResponse(exchange, 500, errorMessage);
    }
}