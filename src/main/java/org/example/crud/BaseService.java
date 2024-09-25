package org.example.crud;

import org.example.mapper.JsonEntityMapper;

public interface BaseService {
    String getContextPath();
    JsonEntityMapper getJsonEntityMapper();
}