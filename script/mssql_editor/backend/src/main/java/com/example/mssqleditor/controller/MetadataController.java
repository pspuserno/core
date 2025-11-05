package com.example.mssqleditor.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v3")
@Tag(name = "Metadata API")
public class MetadataController {

    private final JdbcTemplate jdbcTemplate;

    public MetadataController(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @GetMapping("/table-names")
    @Operation(summary = "List available user tables")
    public List<String> getTableNames() {
        return jdbcTemplate.query("SELECT TABLE_NAME FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_TYPE = 'BASE TABLE' ORDER BY TABLE_NAME",
                (rs, rowNum) -> rs.getString("TABLE_NAME"));
    }
}
