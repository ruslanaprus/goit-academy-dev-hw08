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
//        logger.info("Listing all clients:");
//
//        clientService.listAll().forEach(System.out::println);
//
        int testId = 5;
//        logger.info("clientById={} is {}", testId, clientService.getById(testId));

        logger.info("clientById={} is:", testId);
        Optional<String> clientName = clientService.getClientById(testId);
        clientName.ifPresentOrElse(
                name -> logger.info("Client name: {}", name),
                () -> logger.warn("No client found with ID: {}", testId)
        );

        logger.info("Listing all clients:");
        clientService.listAllClients().ifPresentOrElse(
                clients -> {
                    logger.info("Client(s) found: {}", clients.size());
                    clients.forEach(client -> logger.info(client.toString()));
                },
                () -> logger.warn("No clients found")
        );
    }

    private static void performCreateOperations(ClientService clientService) {
//        logger.info("Creating a new client");
//        clientService.create("Milky Meow Co.");

        logger.info("Creating a new client");
        Optional<Client> result = clientService.createClient("Impawsible trails Inc.");
        result.ifPresentOrElse(
                client -> logger.info("Client added with ID: {}", client.getId()),
                () -> logger.warn("Client addition failed")
        );
    }

    private static void performUpdateOperations(ClientService clientService) {
        int clientId = 7;
        String newName = "Kitten Mittens";
        logger.info("Updating client with ID {} to new name: {}", clientId, newName);
       // clientService.setName(clientId, "Kitten Mittens");
    }

    private static void performDeleteOperations(ClientService clientService) {
        int deleteId = 6;
        logger.info("Deleting client with ID: {}", deleteId);
      //  clientService.deleteById(deleteId);
    }
}

