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

        Optional<Client> result = clientService.create("Test Client");

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

        Optional<Client> result = clientService.create("Test Client");

        assertFalse(result.isPresent());

        verify(mockPreparedStatement).setString(1, "Test Client");
        verify(mockPreparedStatement).executeUpdate();
    }

    @Test
    void testCreateClientSQLException() throws SQLException {
        when(mockConnection.prepareStatement(anyString(), anyInt())).thenThrow(new SQLException("Database error"));

        Optional<Client> result = clientService.create("Test Client");

        assertFalse(result.isPresent());
    }

    @Test
    void testCreateClientInvalidName() {
        Optional<Client> result = clientService.create(null);

        assertFalse(result.isPresent());
    }

    // ---- Get Client with Optional Tests ----

    @Test
    void testGetClientByIdSuccess() throws SQLException {
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true);
        when(mockResultSet.getString("name")).thenReturn("Test Client");

        Optional<String> clientName = clientService.getById(100L);

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

        Optional<String> clientName = clientService.getById(100L);

        assertFalse(clientName.isPresent());
    }

    @Test
    void testGetClientByIdSQLException() throws SQLException {
        when(mockConnection.prepareStatement(anyString())).thenThrow(new SQLException("Database error"));

        Optional<String> clientName = clientService.getById(100L);

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

        Optional<List<Client>> result = clientService.listAll();

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

        Optional<List<Client>> result = clientService.listAll();

        assertTrue(result.isEmpty());
        verify(mockPreparedStatement).executeQuery();
    }

    @Test
    void testListAllClientsSQLException() throws SQLException {
        when(mockConnection.prepareStatement(anyString())).thenThrow(new SQLException("SQL Error"));

        Optional<List<Client>> result = clientService.listAll();

        assertTrue(result.isEmpty());
        verify(mockConnection).prepareStatement(anyString());
    }

}
