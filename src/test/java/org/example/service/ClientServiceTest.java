package org.example.service;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import org.example.crud.ClientService;
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

        assertTrue(clients.isPresent() && clients.get().isEmpty());
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
        Client testClient = new Client("Test Client");

        // Mocking the prepareStatement and other SQL related interactions
        when(mockConnection.prepareStatement(anyString(), eq(Statement.RETURN_GENERATED_KEYS)))
                .thenReturn(mockPreparedStatement);

        // Simulate 1 row affected by the insert
        when(mockPreparedStatement.executeUpdate()).thenReturn(1);

        // Mock the ResultSet to simulate generated keys with both id and name
        when(mockPreparedStatement.getGeneratedKeys()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true);  // Simulate that a row exists in the ResultSet
        when(mockResultSet.getLong("id")).thenReturn(1L);  // Simulate the generated ID
        when(mockResultSet.getString("name")).thenReturn("Test Client");  // Simulate the client name

        // Execute the service method
        Optional<Client> createdClient = clientService.create(testClient);

        // Verify that the client was created successfully and returned with an ID and name
        assertTrue(createdClient.isPresent());
        assertEquals(1L, createdClient.get().getId());
        assertEquals("Test Client", createdClient.get().getName());

        // Verify that the PreparedStatement executeUpdate and getGeneratedKeys methods were called
        verify(mockPreparedStatement).executeUpdate();
        verify(mockPreparedStatement).getGeneratedKeys();
    }

    @Test
    void testCreateClientNoRowsAffected() throws SQLException {
        when(mockConnection.prepareStatement(anyString(), anyInt())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeUpdate()).thenReturn(0);

        Optional<Client> result = clientService.create(new Client("Test Client"));

        assertFalse(result.isPresent());

        verify(mockPreparedStatement).setString(1, "Test Client");
        verify(mockPreparedStatement).executeUpdate();
    }

    @Test
    void testCreateClientSQLException() throws SQLException {
        when(mockConnection.prepareStatement(anyString(), anyInt())).thenThrow(new SQLException("Database error"));

        Optional<Client> result = clientService.create(new Client("Test Client"));

        assertFalse(result.isPresent());
    }

    @Test
    void testCreateClientInvalidName() {
        Optional<Client> result = clientService.create(new Client(""));

        assertFalse(result.isPresent());
    }

    // ---- Get Client with Optional Tests ----

    @Test
    void testGetClientByIdSuccess() throws SQLException {
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true);
        when(mockResultSet.getString("name")).thenReturn("Test Client");

        Optional<Client> clientName = clientService.getById(100L);

        assertTrue(clientName.isPresent());
        assertEquals("Test Client", clientName.get().getName());

        verify(mockPreparedStatement).setLong(1, 100L);
        verify(mockPreparedStatement).executeQuery();
    }

    @Test
    void testGetClientByIdNoResult() throws SQLException {
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(false);

        Optional<Client> clientName = clientService.getById(100L);

        assertFalse(clientName.isPresent());
    }

    @Test
    void testGetClientByIdSQLException() throws SQLException {
        when(mockConnection.prepareStatement(anyString())).thenThrow(new SQLException("Database error"));

        Optional<Client> clientName = clientService.getById(100L);

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
        // Mock the PreparedStatement and ResultSet behavior
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);

        // Simulate an empty ResultSet by returning false on next()
        when(mockResultSet.next()).thenReturn(false);

        // Call the method under test
        Optional<List<Client>> result = clientService.listAll();

        // Assert that the result is present but contains an empty list
        assertTrue(result.isPresent());
        assertTrue(result.get().isEmpty());

        // Verify that the executeQuery method was called on the PreparedStatement
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
