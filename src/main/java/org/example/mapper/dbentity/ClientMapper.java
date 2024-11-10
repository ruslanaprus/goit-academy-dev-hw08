package org.example.mapper.dbentity;

import org.example.model.Client;

import java.sql.PreparedStatement;
import java.sql.SQLException;

public class ClientMapper implements EntityMapper<Client> {

    @Override
    public void mapToStatement(PreparedStatement statement, Client client) throws SQLException {
        statement.setString(1, client.getName());
    }
}