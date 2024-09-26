package org.example.crud;

import com.codahale.metrics.MetricRegistry;
import org.example.db.ConnectionManager;
import org.example.mapper.WorkerJsonMapper;
import org.example.mapper.JsonEntityMapper;
import org.example.mapper.WorkerMapper;
import org.example.model.Level;
import org.example.model.Worker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.time.LocalDate;

import static org.example.constants.Constants.*;

public class WorkerService extends AbstractGenericService<Worker> {
    private static final Logger logger = LoggerFactory.getLogger(WorkerService.class);
    private final WorkerMapper workerMapper;

    public WorkerService(ConnectionManager connectionManager, MetricRegistry metricRegistry) {
        super(connectionManager, metricRegistry);
        workerMapper = new WorkerMapper();
    }

    @Override
    protected String getInsertSQL() {
        return INSERT_INTO_WORKERS;
    }

    @Override
    protected PreparedStatement prepareCreateStatement(PreparedStatement ps, Worker worker) throws SQLException {
        try {
            validateWorkerFields(worker.getName(), worker.getDateOfBirth(), worker.getEmail(), worker.getLevel(), worker.getSalary());
            workerMapper.mapToStatement(ps, worker);
        } catch (IllegalArgumentException e) {
            logger.error("Worker validation failed: {}", e.getMessage());
            throw new SQLException("Worker validation failed: " + e.getMessage());
        }
        return ps;
    }

    @Override
    protected Worker createEntityFromResultSet(ResultSet rs) throws SQLException {
        long workerId = rs.getLong("id");
        String name = rs.getString("name");
        LocalDate dateOfBirth = rs.getDate("birthday").toLocalDate();
        String email = rs.getString("email");
        Level level = Level.valueOf(rs.getString("level").toUpperCase());
        int salary = rs.getInt("salary");
        return new Worker(workerId, name, dateOfBirth, email, level, salary);
    }

    @Override
    protected String getSelectByIdSQL() {
        return GET_WORKER_BY_ID;
    }

    @Override
    protected String getListAllSQL() {
        return LIST_ALL_WORKERS;
    }

    @Override
    protected String getUpdateNameSQL() {
        return SET_NEW_WORKERS_NAME;
    }

    @Override
    protected String getDeleteByIdSQL() {
        return DELETE_WORKER_BY_ID;
    }

    @Override
    public String getContextPath() {
        return "/workers";
    }

    @Override
    public JsonEntityMapper getJsonEntityMapper() {
        return new WorkerJsonMapper();
    }

    private void validateWorkerFields(String name, LocalDate dateOfBirth, String email, Level level, int salary) {
        try {
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
        } catch (IllegalArgumentException e) {
            logger.error("Validation error: {}", e.getMessage());
            throw e;
        }
    }

}