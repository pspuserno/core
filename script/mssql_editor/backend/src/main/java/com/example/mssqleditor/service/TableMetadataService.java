package com.example.mssqleditor.service;

import com.example.mssqleditor.model.ColumnMetadata;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TableMetadataService {

    private final JdbcTemplate jdbcTemplate;

    public TableMetadataService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<ColumnMetadata> getColumns(String tableName) {
        String sql = "SELECT c.COLUMN_NAME, c.DATA_TYPE, c.IS_NULLABLE, " +
                "CASE WHEN k.COLUMN_NAME IS NOT NULL THEN 1 ELSE 0 END AS IS_PRIMARY_KEY " +
                "FROM INFORMATION_SCHEMA.COLUMNS c " +
                "LEFT JOIN INFORMATION_SCHEMA.KEY_COLUMN_USAGE k ON c.TABLE_SCHEMA = k.TABLE_SCHEMA " +
                "AND c.TABLE_NAME = k.TABLE_NAME AND c.COLUMN_NAME = k.COLUMN_NAME " +
                "LEFT JOIN INFORMATION_SCHEMA.TABLE_CONSTRAINTS tc ON tc.CONSTRAINT_NAME = k.CONSTRAINT_NAME " +
                "AND tc.CONSTRAINT_TYPE = 'PRIMARY KEY' " +
                "WHERE c.TABLE_NAME = ? ORDER BY c.ORDINAL_POSITION";
        return jdbcTemplate.query(sql, (rs, rowNum) -> new ColumnMetadata(
                rs.getString("COLUMN_NAME"),
                rs.getString("DATA_TYPE"),
                "YES".equals(rs.getString("IS_NULLABLE")),
                rs.getInt("IS_PRIMARY_KEY") == 1
        ), tableName);
    }
}
