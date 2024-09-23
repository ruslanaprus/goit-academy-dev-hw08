package org.example.service;

import org.example.mapper.JsonEntityMapper;

public interface BaseService {
    String getContextPath();
    JsonEntityMapper getJsonEntityMapper();
}