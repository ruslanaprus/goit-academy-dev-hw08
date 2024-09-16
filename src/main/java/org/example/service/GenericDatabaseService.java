package org.example.service;

import com.codahale.metrics.MetricRegistry;
import org.example.db.ConnectionManager;
import org.example.mapper.EntityMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
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
}