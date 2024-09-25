package org.example;

import com.codahale.metrics.MetricRegistry;
import org.example.constants.DatabaseType;
import org.example.crud.*;
import org.example.db.*;
import org.example.formatter.JsonFormatter;
import org.example.http.HttpServerFactory;
import org.example.log.MetricsLogger;

import java.util.ArrayList;
import java.util.List;

public class AppLauncher {

    public static void main(String[] args) {
        MetricRegistry metricRegistry = new MetricRegistry();
        MetricsLogger.startLogging(metricRegistry);

        DatabaseType dbType = DatabaseType.POSTGRES;
        Database database = DatabaseFactory.createDatabase(dbType, metricRegistry);
        ConnectionManager connectionManager = ConnectionManager.getInstance(database, metricRegistry);

        // Initialize services
        ClientService clientService = new ClientService(connectionManager, metricRegistry);
        WorkerService workerService = new WorkerService(connectionManager, metricRegistry);
        ProjectService projectService = new ProjectService(connectionManager, metricRegistry);

        // Add services to a list
        List<BaseService> services = new ArrayList<>();
        services.add(clientService);
        services.add(workerService);
        services.add(projectService);

        // Start HTTP server
        JsonFormatter jsonFormatter = new JsonFormatter();
        HttpServerFactory httpServerFactory = new HttpServerFactory(services, jsonFormatter);
        httpServerFactory.startServer();

        // Run CRUD operations (as needed)
        ClientOperations.performCrudOperations(clientService);
    }
}