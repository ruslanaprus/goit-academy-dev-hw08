package org.example.service;

import com.codahale.metrics.MetricRegistry;
import org.example.db.ConnectionManager;
import org.example.db.SQLExecutor;
import org.example.mapper.WorkerJsonMapper;
import org.example.mapper.JsonEntityMapper;
import org.example.model.Level;
import org.example.model.Worker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.example.constants.Constants.*;

public class WorkerService implements BaseService {
    private static final Logger logger = LoggerFactory.getLogger(WorkerService.class);
    private final ConnectionManager connectionManager;
    private final MetricRegistry metricRegistry;

    public WorkerService(ConnectionManager connectionManager, MetricRegistry metricRegistry) {
        this.connectionManager = connectionManager;
        this.metricRegistry = metricRegistry;
    }

    @Override
    public String getContextPath() {
        return "/workers";
    }

    public JsonEntityMapper getJsonEntityMapper() {
        return new WorkerJsonMapper();
    }

    public Optional<Worker> create(Worker worker) {
        try {
            validateWorkerFields(worker.getName(), worker.getDateOfBirth(), worker.getEmail(), worker.getLevel(), worker.getSalary());
            logger.info("Creating worker with name {}", worker.getName());

            try (Connection connection = connectionManager.getConnection();
                 PreparedStatement statement = connection.prepareStatement(INSERT_INTO_WORKERS, Statement.RETURN_GENERATED_KEYS)) {

                logger.info("prepared statement: {}", statement);
                statement.setString(1, worker.getName());
                statement.setDate(2, Date.valueOf(worker.getDateOfBirth()));
                statement.setString(3, worker.getEmail());
                statement.setString(4, worker.getLevel().toString());
                statement.setInt(5, worker.getSalary());

                int affectedRows = statement.executeUpdate();
                logger.info("affected rows: {}", affectedRows);

                if (affectedRows > 0) {
                    try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                        if (generatedKeys.next()) {
                            long generatedId = generatedKeys.getLong(1);
                            Worker createdWorker = new Worker(generatedId, worker.getName(), worker.getDateOfBirth(), worker.getEmail(), worker.getLevel(), worker.getSalary());
                            return Optional.of(createdWorker);
                        }
                    }
                } else {
                    logger.error("Failed to insert the worker, no rows affected.");
                }
            }
        } catch (SQLException e) {
            logger.error("SQL error while adding a worker to the database. SQLState: {}, ErrorCode: {}", e.getSQLState(), e.getErrorCode(), e);
        } catch (IllegalArgumentException e) {
            logger.error("Invalid worker fields: {}", e.getMessage());
        }

        return Optional.empty();
    }

    public Optional<Worker> getById(long id) {
        return new SQLExecutor(connectionManager.getConnection(), metricRegistry)
                .executeSingleQuery(
                        GET_WORKER_BY_ID,
                        "Error retrieving worker by id",
                        rs -> {
                            long workerId = rs.getLong("id");
                            String name = rs.getString("name");
                            LocalDate dateOfBirth = rs.getDate("birthday").toLocalDate();
                            String email = rs.getString("email");
                            Level level = Level.valueOf(rs.getString("level").toUpperCase());
                            int salary = rs.getInt("salary");
                            return new Worker(workerId, name, dateOfBirth, email, level, salary);
                        },
                        ps -> {
                            try {
                                ps.setLong(1, id);
                            } catch (SQLException e) {
                                throw new RuntimeException(e);
                            }
                        }
                );
    }

    public Optional<List<Worker>> listAll() {
        String errorMessage = "Failed to execute listAll workers query";

        return new SQLExecutor(connectionManager.getConnection(), metricRegistry).executeQuery(
                LIST_ALL_WORKERS,
                errorMessage,
                rs -> {
                    long id = rs.getLong("id");
                    String name = rs.getString("name");
                    LocalDate dateOfBirth = rs.getDate("birthday").toLocalDate();
                    String email = rs.getString("email");
                    Level level = Level.valueOf(rs.getString("level").toUpperCase());
                    int salary = rs.getInt("salary");
                    return new Worker(id, name, dateOfBirth, email, level, salary);
                }
        );
    }

    public Optional<Long> setName(long id, String name) {
        try (Connection connection = connectionManager.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(SET_NEW_WORKERS_NAME)) {

            logger.info("prepared statement{}", preparedStatement);
            preparedStatement.setString(1, name);
            preparedStatement.setLong(2, id);

            int affectedRows = preparedStatement.executeUpdate();
            if (affectedRows > 0) {
                logger.info("Successfully updated {} row(s) for client with id {}", affectedRows, id);
                return Optional.of(id);
            } else {
                logger.error("Failed to update client with id {}, no rows affected", id);
                return Optional.empty();
            }
        } catch (SQLException e) {
            logger.error("SQL error while updating client with id {}. SQLState: {}, ErrorCode: {}", id, e.getSQLState(), e.getErrorCode(), e);
            return Optional.empty();
        }
    }

    public Optional<Long> deleteById(long id) {
        try (Connection connection = connectionManager.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(DELETE_WORKER_BY_ID)) {

            preparedStatement.setLong(1, id);

            long affectedRows = preparedStatement.executeUpdate();
            if (affectedRows > 0) {
                logger.info("Successfully deleted {} row(s) for client with id {}", affectedRows, id);
                return Optional.of(affectedRows);
            } else {
                logger.error("Failed to delete client with id {}, no rows affected", id);
                return Optional.empty();
            }
        } catch (SQLException e) {
            logger.error("SQL error while deleting client with id {}. SQLState: {}, ErrorCode: {}", id, e.getSQLState(), e.getErrorCode(), e);
        }
        return Optional.empty();
    }

    private void validateWorkerFields(String name, LocalDate dateOfBirth, String email, Level level, int salary) {
        logger.info("start validation");
        if (name == null || name.length() < 2 || name.length() > 1000) {
            throw new IllegalArgumentException("Worker name must be between 2 and 1000 characters.");
        }
        if (email == null || !email.contains("@")) {
            throw new IllegalArgumentException("Invalid email address.");
        }
        if (level == null) {
            throw new IllegalArgumentException("Worker level cannot be null.");
        }
        if (salary <= 0) {
            throw new IllegalArgumentException("Salary must be greater than zero.");
        }
    }

}
