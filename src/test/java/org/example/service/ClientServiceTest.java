package org.example.service;

import com.codahale.metrics.MetricRegistry;
import org.example.db.ConnectionManager;
import org.example.model.Client;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ClientServiceTest {

    private ClientService clientService;
    private ConnectionManager mockConnectionManager;
    private MetricRegistry metricRegistry;
    private Connection mockConnection;
    private PreparedStatement mockPreparedStatement;
    private ResultSet mockResultSet;

    @BeforeEach
    void setUp() {
        mockConnectionManager = mock(ConnectionManager.class);
        metricRegistry = new MetricRegistry();
        mockConnection = mock(Connection.class);
        mockPreparedStatement = mock(PreparedStatement.class);
        mockResultSet = mock(ResultSet.class);

        when(mockConnectionManager.getConnection()).thenReturn(mockConnection);

        clientService = new ClientService(mockConnectionManager, metricRegistry);
    }

    @Test
    void testCreate() throws SQLException {
        when(mockConnection.prepareStatement(anyString(), anyInt())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeUpdate()).thenReturn(1);
        when(mockPreparedStatement.getGeneratedKeys()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true);
        when(mockResultSet.getLong(1)).thenReturn(100L);

        long clientId = clientService.create("Test Client");

        assertEquals(100L, clientId);
        verify(mockPreparedStatement).setString(1, "Test Client");
        verify(mockPreparedStatement).executeUpdate();
    }

    @Test
    void testGetById() throws SQLException {
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true);
        when(mockResultSet.getString("name")).thenReturn("Test Client");

        String clientName = clientService.getById(100L);

        assertEquals("Test Client", clientName);
        verify(mockPreparedStatement).setLong(1, 100L);
        verify(mockPreparedStatement).executeQuery();
    }

    @Test
    void testDeleteById() throws SQLException {
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeUpdate()).thenReturn(1);

        clientService.deleteById(100L);

        verify(mockPreparedStatement).setLong(1, 100L);
        verify(mockPreparedStatement).executeUpdate();
    }

    @Test
    void testCreateClient() throws SQLException {
        when(mockConnection.prepareStatement(anyString(), anyInt())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeUpdate()).thenReturn(1);
        when(mockPreparedStatement.getGeneratedKeys()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true);
        when(mockResultSet.getLong(1)).thenReturn(1L);

        Optional<Client> client = clientService.createClient("Test Client");

        assertTrue(client.isPresent());
        assertEquals("Test Client", client.get().getName());
    }

    @Test
    void testGetClientById() throws SQLException {
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true);
        when(mockResultSet.getLong("id")).thenReturn(1L);
        when(mockResultSet.getString("name")).thenReturn("Test Client");

        Optional<String> clientName = clientService.getClientById(1L);

        assertTrue(clientName.isPresent());
        assertEquals("Test Client", clientName.get());
    }

    @Test
    void testListAllClients() throws SQLException {
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true, true, false);
        when(mockResultSet.getLong("id")).thenReturn(1L, 2L);
        when(mockResultSet.getString("name")).thenReturn("Client A", "Client B");

        var clients = clientService.listAll();

        assertEquals(2, clients.size());
        assertEquals("Client A", clients.get(0).getName());
        assertEquals("Client B", clients.get(1).getName());
    }

}
