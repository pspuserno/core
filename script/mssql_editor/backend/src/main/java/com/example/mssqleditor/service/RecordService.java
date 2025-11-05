package com.example.mssqleditor.service;

import com.example.mssqleditor.model.ColumnMetadata;
import com.example.mssqleditor.model.HistoryEntry;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class RecordService {

    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final TableMetadataService metadataService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public RecordService(NamedParameterJdbcTemplate jdbcTemplate, TableMetadataService metadataService) {
        this.jdbcTemplate = jdbcTemplate;
        this.metadataService = metadataService;
    }

    public List<Map<String, Object>> findAll(String tableName, int limit, int offset) {
        String sql = "SELECT * FROM " + tableName + " ORDER BY 1 OFFSET :offset ROWS FETCH NEXT :limit ROWS ONLY";
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("limit", limit)
                .addValue("offset", offset);
        return jdbcTemplate.query(sql, params, (rs, rowNum) -> {
            Map<String, Object> row = new LinkedHashMap<>();
            int columnCount = rs.getMetaData().getColumnCount();
            for (int i = 1; i <= columnCount; i++) {
                row.put(rs.getMetaData().getColumnLabel(i), rs.getObject(i));
            }
            return row;
        });
    }

    @Transactional
    public void updateRecord(String tableName, Map<String, Object> primaryKey, Map<String, Object> values, String changedBy) {
        if (primaryKey == null || primaryKey.isEmpty()) {
            throw new IllegalArgumentException("Primary key is required for updates");
        }
        List<ColumnMetadata> columns = metadataService.getColumns(tableName);
        List<String> primaryKeyColumns = columns.stream()
                .filter(ColumnMetadata::primaryKey)
                .map(ColumnMetadata::name)
                .toList();
        if (primaryKeyColumns.isEmpty()) {
            throw new IllegalArgumentException("Table has no primary key metadata");
        }
        if (!primaryKeyColumns.stream().allMatch(primaryKey::containsKey)) {
            throw new IllegalArgumentException("Primary key values missing required columns");
        }
        if (values == null || values.isEmpty()) {
            throw new IllegalArgumentException("No values provided for update");
        }

        Map<String, Object> existing = fetchByPrimaryKey(tableName, primaryKey)
                .orElseThrow(() -> new IllegalArgumentException("Record not found"));

        String setClause = values.keySet().stream()
                .map(col -> col + " = :" + col)
                .collect(Collectors.joining(", "));
        String whereClause = primaryKey.keySet().stream()
                .map(col -> col + " = :pk_" + col)
                .collect(Collectors.joining(" AND "));

        MapSqlParameterSource params = new MapSqlParameterSource();
        values.forEach(params::addValue);
        primaryKey.forEach((key, value) -> params.addValue("pk_" + key, value));

        String updateSql = "UPDATE " + tableName + " SET " + setClause + " WHERE " + whereClause;
        jdbcTemplate.update(updateSql, params);

        Map<String, Object> updatedRecord = fetchByPrimaryKey(tableName, primaryKey)
                .orElseThrow(() -> new IllegalStateException("Updated record could not be reloaded"));

        recordHistory(tableName, primaryKey, existing, updatedRecord, changedBy);
    }

    @Transactional(readOnly = true)
    public List<HistoryEntry> getHistory(String tableName, Map<String, Object> primaryKey) {
        String sql = "SELECT id, table_name, primary_key, previous_values, new_values, changed_by, changed_at " +
                "FROM change_history WHERE table_name = :table AND primary_key = :pk ORDER BY changed_at DESC";
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("table", tableName)
                .addValue("pk", toJson(stringifyMap(primaryKey)));
        return jdbcTemplate.query(sql, params, (rs, rowNum) -> new HistoryEntry(
                rs.getLong("id"),
                rs.getString("table_name"),
                fromJsonMap(rs.getString("primary_key")),
                fromJsonMap(rs.getString("previous_values")),
                fromJsonMap(rs.getString("new_values")),
                rs.getString("changed_by"),
                Optional.ofNullable(rs.getObject("changed_at", Timestamp.class))
                        .map(ts -> ts.toInstant().atOffset(ZoneOffset.UTC))
                        .orElse(OffsetDateTime.now())
        ));
    }

    @Transactional
    public void undoChange(long historyId, String changedBy) {
        HistoryEntry entry = jdbcTemplate.queryForObject(
                "SELECT id, table_name, primary_key, previous_values, new_values, changed_by, changed_at FROM change_history WHERE id = :id",
                new MapSqlParameterSource("id", historyId),
                (rs, rowNum) -> new HistoryEntry(
                        rs.getLong("id"),
                        rs.getString("table_name"),
                        fromJsonMap(rs.getString("primary_key")),
                        fromJsonMap(rs.getString("previous_values")),
                        fromJsonMap(rs.getString("new_values")),
                        rs.getString("changed_by"),
                        Optional.ofNullable(rs.getObject("changed_at", Timestamp.class))
                                .map(ts -> ts.toInstant().atOffset(ZoneOffset.UTC))
                                .orElse(OffsetDateTime.now())
                ));

        Map<String, Object> previousValues = new HashMap<>(entry.previousValues());
        if (previousValues.isEmpty()) {
            throw new IllegalStateException("Cannot undo change without previous values");
        }
        applyUpdate(entry.tableName(), entry.primaryKey(), previousValues);
        recordHistory(entry.tableName(), entry.primaryKey(), entry.newValues(), previousValues, changedBy);
    }

    private Optional<Map<String, Object>> fetchByPrimaryKey(String tableName, Map<String, Object> primaryKey) {
        String whereClause = primaryKey.keySet().stream()
                .map(col -> col + " = :" + col)
                .collect(Collectors.joining(" AND "));
        String sql = "SELECT * FROM " + tableName + " WHERE " + whereClause;
        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject(sql, new MapSqlParameterSource(primaryKey), (rs, rowNum) -> {
                Map<String, Object> row = new LinkedHashMap<>();
                for (int i = 1; i <= rs.getMetaData().getColumnCount(); i++) {
                    row.put(rs.getMetaData().getColumnLabel(i), rs.getObject(i));
                }
                return row;
            }));
        } catch (DataAccessException e) {
            return Optional.empty();
        }
    }

    private void applyUpdate(String tableName, Map<String, Object> primaryKey, Map<String, Object> values) {
        String setClause = values.keySet().stream()
                .map(col -> col + " = :" + col)
                .collect(Collectors.joining(", "));
        String whereClause = primaryKey.keySet().stream()
                .map(col -> col + " = :pk_" + col)
                .collect(Collectors.joining(" AND "));

        MapSqlParameterSource params = new MapSqlParameterSource();
        values.forEach(params::addValue);
        primaryKey.forEach((key, value) -> params.addValue("pk_" + key, value));
        String updateSql = "UPDATE " + tableName + " SET " + setClause + " WHERE " + whereClause;
        jdbcTemplate.update(updateSql, params);
    }

    private void recordHistory(String tableName,
                               Map<String, Object> primaryKey,
                               Map<String, Object> oldValues,
                               Map<String, Object> newValues,
                               String changedBy) {
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("table_name", tableName)
                .addValue("primary_key", toJson(stringifyMap(primaryKey)))
                .addValue("previous_values", toJson(oldValues))
                .addValue("new_values", toJson(newValues))
                .addValue("changed_by", changedBy);
        String sql = "INSERT INTO change_history (table_name, primary_key, previous_values, new_values, changed_by) " +
                "VALUES (:table_name, :primary_key, :previous_values, :new_values, :changed_by)";
        jdbcTemplate.update(sql, params);
    }

    private Map<String, Object> stringifyMap(Map<String, Object> value) {
        if (value == null) {
            return Map.of();
        }
        return value.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue() == null ? null : String.valueOf(e.getValue()), (a, b) -> b, LinkedHashMap::new));
    }

    private String toJson(Map<String, Object> value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Unable to serialize map to JSON", e);
        }
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> fromJsonMap(String json) {
        if (json == null) {
            return Map.of();
        }
        try {
            return objectMapper.readValue(json, Map.class);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Unable to parse JSON", e);
        }
    }
}
