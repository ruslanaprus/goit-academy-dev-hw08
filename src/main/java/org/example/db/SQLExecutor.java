package org.example.db;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SQLExecutor implements AutoCloseable {
    private static final Logger logger = LoggerFactory.getLogger(SQLExecutor.class);
    private final Connection connection;
    private final MetricRegistry metricRegistry;

    public SQLExecutor(Connection connection, MetricRegistry metricRegistry) {
        this.connection = connection;
        this.metricRegistry = metricRegistry;
    }

    /**
     * Executes SQL statements that modify the database (INSERT, UPDATE, DELETE, DDL).
     */
    public void executeUpdate(String sql) {
        logger.info("Executing SQL update...");
        Timer.Context context = metricRegistry.timer("sql-update-timer").time();
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.executeUpdate();
            logger.info("SQL update executed successfully");
        } catch (SQLException e) {
            logger.error("Failed to execute SQL update", e);
            throw new RuntimeException("SQL update execution failed", e);
        } finally {
            context.stop();
        }
    }

    public void executeBatch(String sql) {
        logger.info("Executing SQL using PreparedStatement...");
        Timer.Context context = metricRegistry.timer("sql-batch-query-timer").time();
        String[] sqlStatements = sql.split(";");
        try {
            for (String sqlStatement : sqlStatements) {
                sqlStatement = sqlStatement.trim();
                if (!sqlStatement.isEmpty()) {
                    try (PreparedStatement preparedStatement = connection.prepareStatement(sqlStatement)) {
                        preparedStatement.executeUpdate();
                    }
                }
            }
            logger.info("SQL executed successfully using PreparedStatement");
        } catch (SQLException e) {
            logger.error("Failed to execute SQL", e);
            throw new RuntimeException("SQL execution failed", e);
        } finally {
            context.stop();
        }
    }

    public <T> Optional<List<T>> executeQuery(String sqlFilePath, String errorMessage, ResultSetMapper<T> mapper) {
        List<T> result = new ArrayList<>();

        Path path = Paths.get(sqlFilePath);
        Timer.Context context = metricRegistry.timer("sql-query-timer").time();
        try {
            String sql = new String(Files.readAllBytes(path));

            try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
                try (ResultSet rs = preparedStatement.executeQuery()) {
                    if (!rs.isBeforeFirst()) {
                        return Optional.empty();
                    }
                    while (rs.next()) {
                        result.add(mapper.map(rs));
                    }
                }
            }
        } catch (SQLException | IOException e) {
            logger.error(errorMessage, e);
        } finally {
            context.stop();
        }

        return Optional.of(result);
    }

    @Override
    public void close() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                logger.info("Connection closed successfully");
            }
        } catch (SQLException e) {
            logger.error("Failed to close the connection", e);
        }
    }
}