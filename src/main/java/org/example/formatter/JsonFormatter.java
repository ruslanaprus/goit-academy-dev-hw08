package org.example.formatter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import com.sun.net.httpserver.HttpExchange;
import org.example.model.Client;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class JsonFormatter {
    private static ObjectMapper objectMapper = new ObjectMapper();
    private static JavaTimeModule module = new JavaTimeModule();
    private static DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public JsonFormatter() {
        module.addSerializer(LocalDate.class, new LocalDateSerializer(formatter));
        objectMapper.registerModule(module);
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
    }

    public String objectToJson(List<Client> list){
        try {
            return objectMapper.writeValueAsString(list);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public Client jsonToObject(HttpExchange exchange){
        try {
            return objectMapper.readValue(exchange.getRequestBody(), Client.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
