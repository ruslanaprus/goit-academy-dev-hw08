package org.example.service;

import org.example.model.Client;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

public class ClientOperations {
    private static final Logger logger = LoggerFactory.getLogger(ClientOperations.class);

    public static void performCrudOperations(ClientService clientService) {
        performCreateOperations(clientService);
        performReadOperations(clientService);
        performUpdateOperations(clientService);
        performDeleteOperations(clientService);
    }

    private static void performReadOperations(ClientService clientService) {
        int testId = 5;

        logger.info("clientById={} is:", testId);
        Optional<Client> clientName = clientService.getById(testId);
        clientName.ifPresentOrElse(
                name -> logger.info("Client: {}", name),
                () -> logger.warn("No client found with ID: {}", testId)
        );

        logger.info("Listing all clients:");
        clientService.listAll().ifPresentOrElse(
                clients -> {
                    logger.info("Client(s) found: {}", clients.size());
                    clients.forEach(client -> logger.info(client.toString()));
                },
                () -> logger.warn("No clients found")
        );
    }

    private static void performCreateOperations(ClientService clientService) {
        logger.info("Creating a new client");
        Optional<Client> result = clientService.create(new Client("Impawsible trails Inc."));
        result.ifPresentOrElse(
                client -> logger.info("Client added with ID: {}", client.getId()),
                () -> logger.warn("Client addition failed")
        );
    }

    private static void performUpdateOperations(ClientService clientService) {
        int clientId = 7;
        String newName = "Kitten Mittens";
        logger.info("Updating client with ID {} to new name: {}", clientId, newName);

        Optional<Long> result = clientService.setName(clientId, newName);
        result.ifPresentOrElse(
                affectedRows -> logger.info("Client with ID {} successfully updated. Rows affected: {}", clientId, affectedRows),
                () -> logger.warn("Failed to update client with ID {}", clientId)
        );
    }

    private static void performDeleteOperations(ClientService clientService) {
        int deleteId = 6;
        logger.info("Deleting client with ID: {}", deleteId);

        Optional<Long> result = clientService.deleteById(deleteId);
        result.ifPresentOrElse(
                affectedRows -> logger.info("Client with ID {} successfully deleted. Rows affected: {}", deleteId, affectedRows),
                () -> logger.warn("Failed to delete client with ID {}", deleteId)
        );
    }
}

