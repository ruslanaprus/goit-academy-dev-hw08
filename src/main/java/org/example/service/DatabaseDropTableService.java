package org.example.service;

import com.codahale.metrics.MetricRegistry;
import org.example.db.ConnectionManager;
import org.example.db.SQLExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

public class DatabaseDropTableService {
    private static final Logger logger = LoggerFactory.getLogger(DatabaseDropTableService.class);
    private final ConnectionManager connectionManager;
    private final MetricRegistry metricRegistry;

    public DatabaseDropTableService(ConnectionManager connectionManager, MetricRegistry metricRegistry) {
        this.connectionManager = connectionManager;
        this.metricRegistry = metricRegistry;
    }

    public void dropAllTables(String sqlFilePath) {
        Path path = Paths.get(sqlFilePath);
        try (SQLExecutor executor = new SQLExecutor(connectionManager.getConnection(), metricRegistry)) {
            String sqlContent = new String(Files.readAllBytes(path));
            executor.executeBatch(sqlContent);
        } catch (IOException e) {
            logger.error("Failed to read SQL file: {}", e.getMessage());
        }
    }

    public void dropAllTables() {
        String fetchTablesSQL = "SELECT tablename FROM pg_tables WHERE schemaname = 'public'";
        try (Connection connection = connectionManager.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(fetchTablesSQL)) {

            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                String tableName = resultSet.getString("tablename");
                dropTable(connection, tableName);
            }
        } catch (Exception e) {
            logger.error("Failed to drop all tables: {}", e.getMessage());
        }
    }

    private void dropTable(Connection connection, String tableName) {
        if (!tableName.matches("[a-zA-Z_][a-zA-Z0-9_]*")) {
            logger.error("Invalid table name: '{}'", tableName);
            return;
        }

        String dropTableSQL = "DROP TABLE IF EXISTS " + tableName + " CASCADE";
        try (PreparedStatement preparedStatement = connection.prepareStatement(dropTableSQL)) {
            preparedStatement.executeUpdate();
            logger.info("Table '{}' dropped successfully.", tableName);
        } catch (Exception e) {
            logger.error("Failed to drop table '{}': {}", tableName, e.getMessage());
        }
    }

}