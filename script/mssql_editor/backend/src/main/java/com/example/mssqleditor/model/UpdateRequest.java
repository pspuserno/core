package com.example.mssqleditor.model;

import java.util.Map;

public record UpdateRequest(Map<String, Object> primaryKey, Map<String, Object> values, String changedBy) {
}
