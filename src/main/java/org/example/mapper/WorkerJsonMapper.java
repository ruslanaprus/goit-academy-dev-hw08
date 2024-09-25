package org.example.mapper;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import org.example.model.Worker;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class WorkerJsonMapper implements JsonEntityMapper<Worker> {
    private final ObjectMapper objectMapper = new ObjectMapper();
    private static final JavaTimeModule module = new JavaTimeModule();
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public WorkerJsonMapper() {
        module.addSerializer(LocalDate.class, new LocalDateSerializer(formatter));
        objectMapper.registerModule(module);
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
    }

    @Override
    public Worker fromJson(String json) {
        try {
            return objectMapper.readValue(json, Worker.class);
        } catch (Exception e) {
            throw new RuntimeException("Failed to map JSON to Worker", e);
        }
    }

    @Override
    public String toJson(Worker worker) {
        try {
            return objectMapper.writeValueAsString(worker);
        } catch (Exception e) {
            throw new RuntimeException("Failed to map Worker to JSON", e);
        }
    }
}