package org.example.service;

import com.codahale.metrics.MetricRegistry;
import org.example.db.ConnectionManager;

public class DatabaseServiceFactory {
    public static DatabaseInitService createDatabaseInitService(ConnectionManager connectionManager, MetricRegistry metricRegistry) {
        return new DatabaseInitService(connectionManager, metricRegistry);
    }

    public static DatabaseDropTableService createDatabaseDropTableService(ConnectionManager connectionManager, MetricRegistry metricRegistry) {
        return new DatabaseDropTableService(connectionManager, metricRegistry);
    }

    public static DatabasePopulateService createDatabasePopulateService(ConnectionManager connectionManager, MetricRegistry metricRegistry) {
        return new DatabasePopulateService(connectionManager, metricRegistry);
    }

    public static DatabaseQueryService createDatabaseQueryService(ConnectionManager connectionManager, MetricRegistry metricRegistry) {
        return new DatabaseQueryService(connectionManager, metricRegistry);
    }
}