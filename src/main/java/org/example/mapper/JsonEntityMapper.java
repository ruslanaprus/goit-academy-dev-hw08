package org.example.mapper;

public interface JsonEntityMapper<T> {
    T fromJson(String json);
    String toJson(T entity);
}