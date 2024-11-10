package org.example.mapper.json;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.model.Client;

public class ClientJsonMapper implements JsonEntityMapper<Client> {
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public Client fromJson(String json) {
        try {
            return objectMapper.readValue(json, Client.class);
        } catch (Exception e) {
            throw new RuntimeException("Failed to map JSON to Client", e);
        }
    }

    @Override
    public String toJson(Client client) {
        try {
            return objectMapper.writeValueAsString(client);
        } catch (Exception e) {
            throw new RuntimeException("Failed to map Client to JSON", e);
        }
    }
}