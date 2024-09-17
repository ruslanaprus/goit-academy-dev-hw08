package org.example.service;

import com.codahale.metrics.MetricRegistry;
import org.example.db.ConnectionManager;
import org.example.mapper.EntityMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.List;

public class GenericDatabaseService<T> {
    private static final Logger logger = LoggerFactory.getLogger(GenericDatabaseService.class);
    private final ConnectionManager connectionManager;
    private final MetricRegistry metricRegistry;

    public GenericDatabaseService(ConnectionManager connectionManager, MetricRegistry metricRegistry) {
        this.connectionManager = connectionManager;
        this.metricRegistry = metricRegistry;
    }

    public void insertEntities(String sql, List<T> entities, EntityMapper<T> mapper) {
        try (Connection connection = connectionManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            for (T entity : entities) {
                mapper.mapToStatement(statement, entity);
                statement.addBatch();
            }
            statement.executeBatch();
            logger.info("Entities inserted successfully.");
        } catch (SQLException e) {
            logger.error("Failed to insert entities: " + e.getMessage(), e);
            throw new RuntimeException("Entity insertion failed due to: " + e.getSQLState(), e);
        }
    }

    public long insertEntity(String sql, T entity, EntityMapper<T> mapper) {
        try (Connection connection = connectionManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            mapper.mapToStatement(statement, entity);
            int affectedRows = statement.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        return generatedKeys.getLong(1);
                    }
                }
            } else {
                logger.error("Failed to insert entity, no rows affected.");
            }
        } catch (SQLException e) {
            logger.error("Failed to insert entity: " + e.getMessage(), e);
            throw new RuntimeException("Entity insertion failed: " + e.getSQLState(), e);
        }
        return -1;
    }
}