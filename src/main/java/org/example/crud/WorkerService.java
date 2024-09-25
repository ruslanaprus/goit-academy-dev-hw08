package org.example.crud;

import com.codahale.metrics.MetricRegistry;
import org.example.db.ConnectionManager;
import org.example.mapper.WorkerJsonMapper;
import org.example.mapper.JsonEntityMapper;
import org.example.mapper.WorkerMapper;
import org.example.model.Level;
import org.example.model.Worker;

import java.sql.*;
import java.time.LocalDate;

import static org.example.constants.Constants.*;

public class WorkerService extends AbstractGenericService<Worker> {
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
        workerMapper.mapToStatement(ps, worker);
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
}