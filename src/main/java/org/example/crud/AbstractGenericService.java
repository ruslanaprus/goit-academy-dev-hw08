package org.example.crud;

import com.codahale.metrics.MetricRegistry;
import org.example.db.ConnectionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public abstract class AbstractGenericService<T> implements BaseService<T> {
    protected final ConnectionManager connectionManager;
    protected final MetricRegistry metricRegistry;
    private static final Logger logger = LoggerFactory.getLogger(AbstractGenericService.class);

    public AbstractGenericService(ConnectionManager connectionManager, MetricRegistry metricRegistry) {
        this.connectionManager = connectionManager;
        this.metricRegistry = metricRegistry;
    }

    protected abstract String getInsertSQL();
    protected abstract PreparedStatement prepareCreateStatement(PreparedStatement ps, T entity) throws SQLException;
    protected abstract T createEntityFromResultSet(ResultSet rs) throws SQLException;
    protected abstract String getSelectByIdSQL();
    protected abstract String getListAllSQL();
    protected abstract String getUpdateNameSQL();
    protected abstract String getDeleteByIdSQL();

    @Override
    public Optional<T> create(T entity) {
        try (Connection connection = connectionManager.getConnection();
             PreparedStatement ps = connection.prepareStatement(getInsertSQL(), Statement.RETURN_GENERATED_KEYS)) {

            prepareCreateStatement(ps, entity);
            int affectedRows = ps.executeUpdate();
            logger.info("Insert query executed, affected rows: {}", affectedRows);

            if (affectedRows > 0) {
                try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        return Optional.of(createEntityFromResultSet(generatedKeys));
                    }
                }
            } else {
                logger.warn("Insert query affected 0 rows.");
            }
        } catch (SQLException e) {
            logger.error("Error executing create query", e);
        }
        return Optional.empty();
    }

    @Override
    public Optional<T> getById(long id) {
        try (Connection connection = connectionManager.getConnection();
             PreparedStatement ps = connection.prepareStatement(getSelectByIdSQL())) {
            ps.setLong(1, id);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                logger.info("Entity found for ID: {}", id);
                return Optional.of(createEntityFromResultSet(rs));
            } else {
                logger.warn("No entity found for ID: {}", id);
            }
        } catch (SQLException e) {
            logger.error("Error executing getById query for ID: {}", id, e);
        }
        return Optional.empty();
    }

    @Override
    public Optional<List<T>> listAll() {
        List<T> results = new ArrayList<>();
        try (Connection connection = connectionManager.getConnection();
             PreparedStatement ps = connection.prepareStatement(getListAllSQL());
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                results.add(createEntityFromResultSet(rs));
            }

            if (results.isEmpty()) {
                logger.warn("No entities found.");
            } else {
                logger.info("Total entities found: {}", results.size());
            }

            return Optional.of(results);
        } catch (SQLException e) {
            logger.error("Error executing listAll query", e);
        }
        return Optional.empty();
    }

    @Override
    public Optional<Long> setName(long id, String name) {
        logger.info("Updating name for entity ID: {} to '{}'", id, name);
        try (Connection connection = connectionManager.getConnection();
             PreparedStatement ps = connection.prepareStatement(getUpdateNameSQL())) {

            ps.setString(1, name);
            ps.setLong(2, id);
            int affectedRows = ps.executeUpdate();

            if (affectedRows > 0) {
                logger.info("Successfully updated name for entity ID: {}", id);
                return Optional.of((long) affectedRows);
            } else {
                logger.warn("No entity found to update name for ID: {}", id);
            }
        } catch (SQLException e) {
            logger.error("Error executing update name query for ID: {}", id, e);
        }
        return Optional.empty();
    }

    @Override
    public Optional<Long> deleteById(long id) {
        try (Connection connection = connectionManager.getConnection();
             PreparedStatement ps = connection.prepareStatement(getDeleteByIdSQL())) {

            ps.setLong(1, id);
            int affectedRows = ps.executeUpdate();
            if (affectedRows > 0) {
                logger.info("Successfully deleted entity with ID: {}", id);
                return Optional.of((long) affectedRows);
            } else {
                logger.warn("No entity found to delete with ID: {}", id);
            }
        } catch (SQLException e) {
            logger.error("Error executing delete query for ID: {}", id, e);
        }
        return Optional.empty();
    }
}