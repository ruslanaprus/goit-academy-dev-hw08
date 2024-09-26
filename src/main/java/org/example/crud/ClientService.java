package org.example.crud;

import com.codahale.metrics.MetricRegistry;
import org.example.db.ConnectionManager;
import org.example.mapper.ClientJsonMapper;
import org.example.mapper.ClientMapper;
import org.example.mapper.JsonEntityMapper;
import org.example.model.Client;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;

import static org.example.constants.Constants.*;

public class ClientService extends AbstractGenericService<Client> {
    private static final Logger logger = LoggerFactory.getLogger(ClientService.class);
    private final ClientMapper clientMapper;

    public ClientService(ConnectionManager connectionManager, MetricRegistry metricRegistry) {
        super(connectionManager, metricRegistry);
        clientMapper = new ClientMapper();
    }

    @Override
    protected String getInsertSQL() {
        return INSERT_INTO_CLIENTS;
    }

    @Override
    protected PreparedStatement prepareCreateStatement(PreparedStatement ps, Client client) throws SQLException {
        try {
            validateName(client.getName());
            clientMapper.mapToStatement(ps, client);
        } catch (IllegalArgumentException e) {
            logger.error("Client validation failed: {}", e.getMessage());
            throw new SQLException("Client validation failed: " + e.getMessage());
        }
        return ps;
    }

    @Override
    protected Client createEntityFromResultSet(ResultSet rs) throws SQLException {
        long id = rs.getLong("id");
        String name = rs.getString("name");
        return new Client(id, name);
    }

    @Override
    protected String getSelectByIdSQL() {
        return GET_CLIENT_BY_ID;
    }

    @Override
    protected String getListAllSQL() {
        return LIST_ALL_CLIENTS;
    }

    @Override
    protected String getUpdateNameSQL() {
        return SET_NEW_CLIENTS_NAME;
    }

    @Override
    protected String getDeleteByIdSQL() {
        return DELETE_CLIENT_BY_ID;
    }

    @Override
    public String getContextPath() {
        return "/clients";
    }

    @Override
    public JsonEntityMapper getJsonEntityMapper() {
        return new ClientJsonMapper();
    }

    private void validateName(String name) {
        try {
            if (name == null || name.length() < 2 || name.length() > 1000) {
                throw new IllegalArgumentException("Client name must be between 2 and 1000 characters.");
            }
        } catch (IllegalArgumentException e) {
            logger.error("Validation error: {}", e.getMessage());
            throw e;
        }
    }
}