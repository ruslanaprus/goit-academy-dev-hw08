package org.example.service;

import com.codahale.metrics.MetricRegistry;
import org.example.db.ConnectionManager;
import org.example.db.SQLExecutor;
import org.example.model.Client;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.example.constants.Constants.LIST_ALL_CLIENTS;

public class ClientService {
    private static final Logger logger = LoggerFactory.getLogger(ClientService.class);
    private final ConnectionManager connectionManager;
    private final MetricRegistry metricRegistry;

    public ClientService(ConnectionManager connectionManager, MetricRegistry metricRegistry) {
        this.connectionManager = connectionManager;
        this.metricRegistry = metricRegistry;
    }

    public long create(String name){
        return 0;
    }

    public String getById(long id){
        return "";
    }

    public void setName(long id, String name){
    }

    public void deleteById(long id){

    }

    public List<Client> listAll(){
        List<Client> clients = new ArrayList<>();

        try (Connection connection = connectionManager.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(LIST_ALL_CLIENTS)) {

            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                Client client = new Client();
                client.setId(resultSet.getLong("id"));
                client.setName(resultSet.getString("name"));
                clients.add(client);
            }
        } catch (SQLException e) {
            logger.error("Error fetching clients from a database", e);
        }

        return clients;
    }

    public Optional<List<Client>> listAllClients() {
        String errorMessage = "Failed to execute findMaxSalaryWorker query";

        try (SQLExecutor executor = new SQLExecutor(connectionManager.getConnection(), metricRegistry)) {
            return executor.executeQuery(
                    LIST_ALL_CLIENTS,
                    errorMessage,
                    rs -> new Client(rs.getLong("id"), rs.getString("name"))
            );
        }
    }
}
