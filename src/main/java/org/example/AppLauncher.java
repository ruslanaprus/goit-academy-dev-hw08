package org.example;

import com.codahale.metrics.MetricRegistry;
import org.example.constants.DatabaseType;
import org.example.db.ConnectionManager;
import org.example.db.Database;
import org.example.db.Postgresql;
import org.example.db.SQLite;
import org.example.log.MetricsLogger;
import org.example.model.Client;
import org.example.service.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

import static org.example.constants.Constants.*;

public class AppLauncher {
    private static final Logger logger = LoggerFactory.getLogger(AppLauncher.class);

    public static void main(String[] args) {
        MetricRegistry metricRegistry = new MetricRegistry();
        MetricsLogger.startLogging(metricRegistry);

        runDatabaseOperations(DatabaseType.POSTGRES, metricRegistry);
    }

    private static void runDatabaseOperations(DatabaseType dbType, MetricRegistry metricRegistry) {
        Database database;

        database = switch (dbType) {
            case SQLITE -> new SQLite(SQLITE_DB_PATH, metricRegistry);
            case POSTGRES -> new Postgresql(metricRegistry);
        };

        ConnectionManager connectionManager = ConnectionManager.getInstance(database, metricRegistry);

        ClientService clientService = DatabaseServiceFactory.manageClients(connectionManager, metricRegistry);

        performCreateOperations(clientService);
        performUpdateOperations(clientService);
        performDeleteOperations(clientService);
        performReadOperations(clientService);
    }

    private static void performReadOperations(ClientService clientService) {
        logger.info("Listing all clients:");

        clientService.listAll().forEach(System.out::println);

        int testId = 5;
        logger.info("clientById={} is {}", testId, clientService.getById(testId));

        Optional<String> clientName = clientService.getClientById(testId);
        clientName.ifPresentOrElse(
                name -> logger.info("Client name: {}", name),
                () -> logger.warn("No client found with ID: {}", testId)
        );

        clientService.listAllClients().ifPresentOrElse(
                clients -> {
                    logger.info("Client(s) found: {}", clients.size());
                    clients.forEach(client -> logger.info(client.toString()));
                },
                () -> logger.warn("No clients found")
        );
    }

    private static void performCreateOperations(ClientService clientService) {
        logger.info("Creating a new client");
        clientService.create("Milky Meow Co.");

        Optional<Client> result = clientService.createClient("TestClient");
        result.ifPresentOrElse(
                client -> logger.info("Client added with ID: {}", client.getId()),
                () -> logger.warn("Client addition failed")
        );
    }

    private static void performUpdateOperations(ClientService clientService) {
        int clientId = 13;
        String newName = "Kitten";
        logger.info("Updating client with ID {} to new name: {}", clientId, newName);
        clientService.setName(13, "Kitten");
    }

    private static void performDeleteOperations(ClientService clientService) {
        int deleteId = 21;
        logger.info("Deleting client with ID: {}", deleteId);
        clientService.deleteById(deleteId);
    }
}
