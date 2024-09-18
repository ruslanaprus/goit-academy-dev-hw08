package org.example;

import com.codahale.metrics.MetricRegistry;
import org.example.constants.DatabaseType;
import org.example.db.ConnectionManager;
import org.example.db.Database;
import org.example.db.Postgresql;
import org.example.db.SQLite;
import org.example.model.Client;
import org.example.log.MetricsLogger;
import org.example.service.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

        clientService.listAll().stream().toList().forEach(System.out::println);

        clientService.createClient(new Client("Kitten"));

        clientService.deleteById(8);

        clientService.setName(10, "Paws Box");

        System.out.println("clientService.getById(10) = " + clientService.getById(10));

        clientService.listAllClients().ifPresent(clients -> {
            logger.info("Client(s) found: {}", clients.size());
            clients.forEach(client -> logger.info(client.toString()));
        });
    }
}