package org.example.crud;

import com.codahale.metrics.MetricRegistry;
import org.example.db.ConnectionManager;
import org.example.db.SQLExecutor;
import org.example.mapper.ClientJsonMapper;
import org.example.mapper.JsonEntityMapper;
import org.example.model.Client;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.List;
import java.util.Optional;

import static org.example.constants.Constants.*;

public class ClientService extends AbstractGenericService<Client> {

    public ClientService(ConnectionManager connectionManager, MetricRegistry metricRegistry) {
        super(connectionManager, metricRegistry);
    }

    @Override
    protected String getInsertSQL() {
        return INSERT_INTO_CLIENTS;
    }

    @Override
    protected PreparedStatement prepareCreateStatement(PreparedStatement ps, Client client) throws SQLException {
        ps.setString(1, client.getName());
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
}

//public class ClientService implements BaseService {
//    private static final Logger logger = LoggerFactory.getLogger(ClientService.class);
//    private final ConnectionManager connectionManager;
//    private final MetricRegistry metricRegistry;
//
//    public ClientService(ConnectionManager connectionManager, MetricRegistry metricRegistry) {
//        this.connectionManager = connectionManager;
//        this.metricRegistry = metricRegistry;
//    }
//
//    @Override
//    public String getContextPath() {
//        return "/clients";
//    }
//
//    public JsonEntityMapper getJsonEntityMapper() {
//        return new ClientJsonMapper();
//    }
//
//    /**
//     * Following methods are using Optional to provides a way of handling cases where data may be missing or the operation fails.
//     */
//    public Optional<Client> create(Client client) {
//        try {
//            validateName(client.getName());
//            try (Connection connection = connectionManager.getConnection();
//                 PreparedStatement statement = connection.prepareStatement(INSERT_INTO_CLIENTS, Statement.RETURN_GENERATED_KEYS)) {
//
//                statement.setString(1, client.getName());
//                int affectedRows = statement.executeUpdate();
//
//                if (affectedRows > 0) {
//                    try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
//                        if (generatedKeys.next()) {
//                            long generatedId = generatedKeys.getLong(1);
//                            Client newClient = new Client(generatedId, client.getName());
//                            return Optional.of(newClient);
//                        }
//                    }
//                } else {
//                    logger.error("Failed to insert the client, no rows affected.");
//                }
//            }
//        } catch (SQLException e) {
//            logger.error("SQL error while adding a client to the database. SQLState: {}, ErrorCode: {}", e.getSQLState(), e.getErrorCode(), e);
//        } catch (IllegalArgumentException e) {
//            logger.error("Invalid client name: {}", e.getMessage());
//        }
//
//        return Optional.empty();
//    }
//
//    public Optional<Client> getById(long id) {
//        try (Connection connection = connectionManager.getConnection();
//             PreparedStatement preparedStatement = connection.prepareStatement(GET_CLIENT_BY_ID)) {
//            preparedStatement.setLong(1, id);
//            ResultSet resultSet = preparedStatement.executeQuery();
//
//            if (resultSet.next()) {
//                Client client = new Client();
//                client.setId(resultSet.getLong("id"));
//                client.setName(resultSet.getString("name"));
//                return Optional.of(client);
//            } else {
//                logger.warn("No client found with id {}", id);
//            }
//        } catch (SQLException e) {
//            logger.error("SQL error while getting client by id {}. SQLState: {}, ErrorCode: {}", id, e.getSQLState(), e.getErrorCode(), e);
//        }
//        return Optional.empty();
//    }
//
//    public Optional<List<Client>> listAll() {
//        String errorMessage = "Failed to execute findMaxSalaryWorker query";
//
//        try (SQLExecutor executor = new SQLExecutor(connectionManager.getConnection(), metricRegistry)) {
//            return executor.executeQuery(
//                    LIST_ALL_CLIENTS,
//                    errorMessage,
//                    rs -> new Client(rs.getLong("id"), rs.getString("name"))
//            );
//        }
//    }
//
//    public Optional<Long> setName(long id, String name) {
//        try {
//            validateName(name);
//            try (Connection connection = connectionManager.getConnection();
//                 PreparedStatement preparedStatement = connection.prepareStatement(SET_NEW_CLIENTS_NAME)) {
//
//                preparedStatement.setString(1, name);
//                preparedStatement.setLong(2, id);
//
//                long affectedRows = preparedStatement.executeUpdate();
//                if (affectedRows > 0) {
//                    logger.info("Successfully updated {} row(s) for client with id {}", affectedRows, id);
//                    return Optional.of(affectedRows);
//                } else {
//                    logger.error("Failed to update client with id {}, no rows affected", id);
//                    return Optional.empty();
//                }
//            }
//        } catch (SQLException e) {
//            logger.error("SQL error while updating client with id {}. SQLState: {}, ErrorCode: {}", id, e.getSQLState(), e.getErrorCode(), e);
//        } catch (IllegalArgumentException e) {
//            logger.error("Invalid client name: {}", e.getMessage());
//        }
//        return Optional.empty();
//    }
//
//    public Optional<Long> deleteById(long id) {
//        try (Connection connection = connectionManager.getConnection();
//             PreparedStatement preparedStatement = connection.prepareStatement(DELETE_CLIENT_BY_ID)) {
//
//            preparedStatement.setLong(1, id);
//
//            long affectedRows = preparedStatement.executeUpdate();
//            if (affectedRows > 0) {
//                logger.info("Successfully deleted {} row(s) for client with id {}", affectedRows, id);
//                return Optional.of(affectedRows);
//            } else {
//                logger.error("Failed to delete client with id {}, no rows affected", id);
//                return Optional.empty();
//            }
//        } catch (SQLException e) {
//            logger.error("SQL error while deleting client with id {}. SQLState: {}, ErrorCode: {}", id, e.getSQLState(), e.getErrorCode(), e);
//        }
//        return Optional.empty();
//    }
//
//    private void validateName(String name) {
//        if (name == null || name.length() < 2 || name.length() > 1000) {
//            throw new IllegalArgumentException("Client name must be between 2 and 1000 characters.");
//        }
//    }
//
//}
