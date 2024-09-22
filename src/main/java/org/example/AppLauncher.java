package org.example;

import com.codahale.metrics.MetricRegistry;
import org.example.constants.DatabaseType;
import org.example.db.*;
import org.example.http.HttpServerFactory;
import org.example.log.MetricsLogger;
import org.example.service.*;

public class AppLauncher {

    public static void main(String[] args) {
        MetricRegistry metricRegistry = new MetricRegistry();
        MetricsLogger.startLogging(metricRegistry);

        DatabaseType dbType = DatabaseType.POSTGRES;
        Database database = DatabaseFactory.createDatabase(dbType, metricRegistry);
        ConnectionManager connectionManager = ConnectionManager.getInstance(database, metricRegistry);

        ClientService clientService = new ClientService(connectionManager, metricRegistry);

        // Run CRUD operations
        ClientOperations.performCrudOperations(clientService);

        // Start HTTP server
        HttpServerFactory.startHttpServer(clientService);
    }
}