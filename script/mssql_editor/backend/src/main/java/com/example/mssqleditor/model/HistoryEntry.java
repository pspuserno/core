package com.example.mssqleditor.model;

import java.time.OffsetDateTime;
import java.util.Map;

public record HistoryEntry(
        long id,
        String tableName,
        Map<String, Object> primaryKey,
        Map<String, Object> previousValues,
        Map<String, Object> newValues,
        String changedBy,
        OffsetDateTime changedAt
) {
}
