package org.example.service;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import org.example.db.ConnectionManager;
import org.example.model.Client;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.sql.*;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class ClientServiceTest {

    private ClientService clientService;
    @Mock
    private ConnectionManager mockConnectionManager;
    @Mock
    private MetricRegistry mockMetricRegistry;
    @Mock
    private Timer mockTimer;
    @Mock
    private Timer.Context mockTimerContext;
    @Mock
    private Connection mockConnection;
    @Mock
    private PreparedStatement mockPreparedStatement;
    @Mock
    private ResultSet mockResultSet;

    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);

        when(mockConnectionManager.getConnection()).thenReturn(mockConnection);
        when(mockMetricRegistry.timer(anyString())).thenReturn(mockTimer);
        when(mockTimer.time()).thenReturn(mockTimerContext);

        clientService = new ClientService(mockConnectionManager, mockMetricRegistry);
    }

    // ---- Create Tests ----

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
    void testCreateNoRowsAffected() throws SQLException {
        when(mockConnection.prepareStatement(anyString(), anyInt())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeUpdate()).thenReturn(0);

        long clientId = clientService.create("Test Client");

        assertEquals(-1, clientId);
        verify(mockPreparedStatement).setString(1, "Test Client");
        verify(mockPreparedStatement).executeUpdate();
    }

    @Test
    void testCreateSQLException() throws SQLException {
        when(mockConnection.prepareStatement(anyString(), anyInt())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeUpdate()).thenThrow(new SQLException());

        long clientId = clientService.create("Test Client");

        assertEquals(-1, clientId);
    }

    @Test
    void testCreateInvalidName() throws SQLException {
        long clientId = clientService.create(null);

        assertEquals(-1, clientId);
    }

    // ---- GetById Tests ----

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
    void testGetByIdNotFound() throws SQLException {
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(false);

        String clientName = clientService.getById(100L);

        assertNull(clientName);
        verify(mockPreparedStatement).setLong(1, 100L);
        verify(mockPreparedStatement).executeQuery();
    }

    @Test
    void testGetByIdSQLException() throws SQLException {
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery()).thenThrow(new SQLException("Database error"));

        String clientName = clientService.getById(100L);

        assertNull(clientName);
    }

    // ---- SetName Tests ----

    @Test
    void testSetName() throws SQLException {
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeUpdate()).thenReturn(1);

        clientService.setName(100L, "Updated Client");

        verify(mockPreparedStatement).setString(1, "Updated Client");
        verify(mockPreparedStatement).setLong(2, 100L);
        verify(mockPreparedStatement).executeUpdate();
    }

    @Test
    void testSetNameNoRowsAffected() throws SQLException {
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeUpdate()).thenReturn(0);

        clientService.setName(100L, "Updated Client");

        verify(mockPreparedStatement).setString(1, "Updated Client");
        verify(mockPreparedStatement).setLong(2, 100L);
        verify(mockPreparedStatement).executeUpdate();
    }

    @Test
    void testSetNameSQLException() throws SQLException {
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeUpdate()).thenThrow(new SQLException("Database error"));

        clientService.setName(100L, "Updated Client");
    }

    @Test
    void testSetNameInvalidName() {
        clientService.setName(100L, null);
    }

    // ---- DeleteById Tests ----

    @Test
    void testDeleteById() throws SQLException {
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeUpdate()).thenReturn(1);

        clientService.deleteById(100L);

        verify(mockPreparedStatement).setLong(1, 100L);
        verify(mockPreparedStatement).executeUpdate();
    }

    @Test
    void testDeleteByIdNoRowsAffected() throws SQLException {
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeUpdate()).thenReturn(0);

        clientService.deleteById(100L);

        verify(mockPreparedStatement).setLong(1, 100L);
        verify(mockPreparedStatement).executeUpdate();
    }

    @Test
    void testDeleteByIdSQLException() throws SQLException {
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeUpdate()).thenThrow(new SQLException("Database error"));

        clientService.deleteById(100L);
    }

    // ---- ListAll Tests ----

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

    @Test
    void testListAllNoClients() throws SQLException {
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(false);

        var clients = clientService.listAll();

        assertTrue(clients.isEmpty());
    }

    @Test
    void testListAllSQLException() throws SQLException {
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery()).thenThrow(new SQLException("Database error"));

        var clients = clientService.listAll();

        assertTrue(clients.isEmpty());
    }

    // ---- Create Client with Optional Tests ----

    @Test
    void testCreateClientSuccess() throws SQLException {
        when(mockConnection.prepareStatement(anyString(), anyInt())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeUpdate()).thenReturn(1);
        when(mockPreparedStatement.getGeneratedKeys()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true);
        when(mockResultSet.getLong(1)).thenReturn(100L);

        Optional<Client> result = clientService.createClient("Test Client");

        assertTrue(result.isPresent());
        assertEquals(100L, result.get().getId());
        assertEquals("Test Client", result.get().getName());

        verify(mockPreparedStatement).setString(1, "Test Client");
        verify(mockPreparedStatement).executeUpdate();
    }

    @Test
    void testCreateClientNoRowsAffected() throws SQLException {
        when(mockConnection.prepareStatement(anyString(), anyInt())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeUpdate()).thenReturn(0);

        Optional<Client> result = clientService.createClient("Test Client");

        assertFalse(result.isPresent());

        verify(mockPreparedStatement).setString(1, "Test Client");
        verify(mockPreparedStatement).executeUpdate();
    }

    @Test
    void testCreateClientSQLException() throws SQLException {
        when(mockConnection.prepareStatement(anyString(), anyInt())).thenThrow(new SQLException("Database error"));

        Optional<Client> result = clientService.createClient("Test Client");

        assertFalse(result.isPresent());
    }

    @Test
    void testCreateClientInvalidName() {
        Optional<Client> result = clientService.createClient(null);

        assertFalse(result.isPresent());
    }

    // ---- Get Client with Optional Tests ----

    @Test
    void testGetClientByIdSuccess() throws SQLException {
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true);
        when(mockResultSet.getString("name")).thenReturn("Test Client");

        Optional<String> clientName = clientService.getClientById(100L);

        assertTrue(clientName.isPresent());
        assertEquals("Test Client", clientName.get());

        verify(mockPreparedStatement).setLong(1, 100L);
        verify(mockPreparedStatement).executeQuery();
    }

    @Test
    void testGetClientByIdNoResult() throws SQLException {
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(false);

        Optional<String> clientName = clientService.getClientById(100L);

        assertFalse(clientName.isPresent());
    }

    @Test
    void testGetClientByIdSQLException() throws SQLException {
        when(mockConnection.prepareStatement(anyString())).thenThrow(new SQLException("Database error"));

        Optional<String> clientName = clientService.getClientById(100L);

        assertFalse(clientName.isPresent());
    }

    // ---- List all Clients with Optional Tests ----

    @Test
    void testListAllClientsSuccess() throws SQLException {
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.isBeforeFirst()).thenReturn(true);
        when(mockResultSet.next()).thenReturn(true).thenReturn(false);
        when(mockResultSet.getLong("id")).thenReturn(1L);
        when(mockResultSet.getString("name")).thenReturn("Test Client");

        Optional<List<Client>> result = clientService.listAllClients();

        assertTrue(result.isPresent());
        assertEquals(1, result.get().size());
        assertEquals("Test Client", result.get().get(0).getName());
        verify(mockPreparedStatement).executeQuery();
    }

    @Test
    void testListAllClientsEmptyResultSet() throws SQLException {
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.isBeforeFirst()).thenReturn(false);

        Optional<List<Client>> result = clientService.listAllClients();

        assertTrue(result.isEmpty());
        verify(mockPreparedStatement).executeQuery();
    }

    @Test
    void testListAllClientsSQLException() throws SQLException {
        when(mockConnection.prepareStatement(anyString())).thenThrow(new SQLException("SQL Error"));

        Optional<List<Client>> result = clientService.listAllClients();

        assertTrue(result.isEmpty());
        verify(mockConnection).prepareStatement(anyString());
    }

}
