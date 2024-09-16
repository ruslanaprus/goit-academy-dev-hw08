package org.example.db;

import com.codahale.metrics.MetricRegistry;
import org.example.config.ConfigLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

public class ConnectionManager {
    private static final Logger logger = LoggerFactory.getLogger(ConnectionManager.class);
    private static ConnectionManager instance;
    private DataSource dataSource;

    private ConnectionManager(Database database, MetricRegistry metricRegistry) {
        initDataSource(database, metricRegistry);
    }

    public static synchronized ConnectionManager getInstance(Database database, MetricRegistry metricRegistry) {
        if (instance == null) {
            instance = new ConnectionManager(database, metricRegistry);
        }
        return instance;
    }

    private void initDataSource(Database database, MetricRegistry metricRegistry) {
        try {
            ConfigLoader configLoader = new ConfigLoader();
            this.dataSource = database.createDataSource(configLoader);
        } catch (Exception e) {
            logger.error("Failed to initialize DataSource", e);
            throw new IllegalStateException("DataSource is not initialized. Ensure initDataSource() was successful.");
        }
    }

    public Connection getConnection() {
        try {
            return dataSource.getConnection();
        } catch (SQLException e) {
            logger.error("Failed to obtain database connection", e);
            throw new IllegalStateException("Database connection is unavailable. Please contact support.");
        }
    }
}
