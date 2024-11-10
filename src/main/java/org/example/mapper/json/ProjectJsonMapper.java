package org.example.mapper.json;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import org.example.model.Project;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class ProjectJsonMapper implements JsonEntityMapper<Project> {
    private final ObjectMapper objectMapper = new ObjectMapper();
    private static final JavaTimeModule module = new JavaTimeModule();
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public ProjectJsonMapper() {
        module.addSerializer(LocalDate.class, new LocalDateSerializer(formatter));
        objectMapper.registerModule(module);
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
    }

    @Override
    public Project fromJson(String json) {
        try {
            return objectMapper.readValue(json, Project.class);
        } catch (Exception e) {
            throw new RuntimeException("Failed to map JSON to Project", e);
        }
    }

    @Override
    public String toJson(Project project) {
        try {
            return objectMapper.writeValueAsString(project);
        } catch (Exception e) {
            throw new RuntimeException("Failed to map Project to JSON", e);
        }
    }
}