package org.example.http;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.example.formatter.JsonFormatter;
import org.example.model.Client;
import org.example.service.ClientService;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;

public class MyHttpServer implements HttpHandler {
    private final ClientService clientService;
    private final JsonFormatter jsonFormatter;

    public MyHttpServer(ClientService clientService, JsonFormatter jsonFormatter) {
        this.clientService = clientService;
        this.jsonFormatter = jsonFormatter;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();
        try {
            switch (method) {
                case "GET":
                    handleGetRequest(exchange);
                    break;
                case "POST":
                    handlePostRequest(exchange);
                    break;
                case "PUT":
                    handlePutRequest(exchange);
                    break;
                case "DELETE":
                    handleDeleteRequest(exchange);
                    break;
                default:
                    sendResponse(exchange, 405, "Method Not Allowed");
            }
        } catch (Exception e) {
            handleServerError(exchange, e);
        }
    }

    // Handle GET request (Fetch a client by ID or all clients)
    private void handleGetRequest(HttpExchange exchange) throws IOException {
        String path = exchange.getRequestURI().getPath();
        String[] pathComponents = path.split("/");

        if (pathComponents.length == 3 && pathComponents[1].equals("clients")) {
            long clientId = Long.parseLong(pathComponents[2]);
            Optional<String> clientOptional = clientService.getClientById(clientId);

            if (clientOptional.isPresent()) {
                sendResponse(exchange, 200, clientOptional.get());
            } else {
                sendResponse(exchange, 404, "Client not found");
            }
        } else if (path.equals("/clients")) {
            Optional<List<Client>> clientsOptional = clientService.listAllClients();
            if (clientsOptional.isPresent()) {
                sendResponse(exchange, 200, jsonFormatter.objectToJson(clientsOptional.get()));
            } else {
                sendResponse(exchange, 404, "No clients found");
            }
        } else {
            sendResponse(exchange, 400, "Bad Request");
        }
    }

    // Handle POST request (Create a new client)
    private void handlePostRequest(HttpExchange exchange) throws IOException {
        Client client = jsonFormatter.jsonToObject(exchange);

        Optional<Client> createdClient = clientService.createClient(client.getName());
        if (createdClient.isPresent()) {
            sendResponse(exchange, 201, jsonFormatter.objectToJson(List.of(createdClient.get())));
        } else {
            sendResponse(exchange, 400, "Failed to create client");
        }
    }

    // Handle PUT request (Update a client's name)
    private void handlePutRequest(HttpExchange exchange) throws IOException {
        String path = exchange.getRequestURI().getPath();
        String[] pathComponents = path.split("/");

        if (pathComponents.length == 3 && pathComponents[1].equals("clients")) {
            long clientId = Long.parseLong(pathComponents[2]);
            Client client = jsonFormatter.jsonToObject(exchange);

            Optional<Boolean> updateSuccess = clientService.setClientName(clientId, client.getName());
            if (updateSuccess.isPresent() && updateSuccess.get()) {
                sendResponse(exchange, 200, "Client updated successfully");
            } else {
                sendResponse(exchange, 404, "Client not found or update failed");
            }
        } else {
            sendResponse(exchange, 400, "Bad Request");
        }
    }

    // Handle DELETE request (Delete a client by ID)
    private void handleDeleteRequest(HttpExchange exchange) throws IOException {
        String path = exchange.getRequestURI().getPath();
        String[] pathComponents = path.split("/");

        if (pathComponents.length == 3 && pathComponents[1].equals("clients")) {
            long clientId = Long.parseLong(pathComponents[2]);

            Optional<Boolean> deleteSuccess = clientService.deleteClientById(clientId);
            if (deleteSuccess.isPresent() && deleteSuccess.get()) {
                sendResponse(exchange, 200, "Client deleted successfully");
            } else {
                sendResponse(exchange, 404, "Client not found or deletion failed");
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
