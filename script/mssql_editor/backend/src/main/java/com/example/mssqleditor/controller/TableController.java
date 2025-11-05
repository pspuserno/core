package com.example.mssqleditor.controller;

import com.example.mssqleditor.model.ColumnMetadata;
import com.example.mssqleditor.model.HistoryEntry;
import com.example.mssqleditor.model.UndoRequest;
import com.example.mssqleditor.model.UpdateRequest;
import com.example.mssqleditor.service.RecordService;
import com.example.mssqleditor.service.TableMetadataService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/tables")
@Tag(name = "Table API", description = "Endpoints for browsing and editing MSSQL tables")
public class TableController {

    private final RecordService recordService;
    private final TableMetadataService metadataService;

    public TableController(RecordService recordService, TableMetadataService metadataService) {
        this.recordService = recordService;
        this.metadataService = metadataService;
    }

    @GetMapping("/{tableName}/columns")
    @Operation(summary = "Fetch metadata for a table")
    public List<ColumnMetadata> getColumns(@PathVariable String tableName) {
        return metadataService.getColumns(tableName);
    }

    @GetMapping("/{tableName}/records")
    @Operation(summary = "List records of a table")
    public List<Map<String, Object>> getRecords(@PathVariable String tableName,
                                                @RequestParam(defaultValue = "100") int limit,
                                                @RequestParam(defaultValue = "0") int offset) {
        return recordService.findAll(tableName, limit, offset);
    }

    @PutMapping("/{tableName}/records")
    @Operation(summary = "Update a record and capture history")
    public ResponseEntity<Void> updateRecord(@PathVariable String tableName, @RequestBody UpdateRequest request) {
        recordService.updateRecord(tableName, request.primaryKey(), request.values(), request.changedBy());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{tableName}/history")
    @Operation(summary = "List change history for a record")
    public List<HistoryEntry> getHistory(@PathVariable String tableName, @RequestParam Map<String, Object> primaryKey) {
        return recordService.getHistory(tableName, primaryKey);
    }

    @PostMapping("/history/{historyId}/undo")
    @Operation(summary = "Undo a specific change entry")
    public ResponseEntity<Void> undoChange(@PathVariable long historyId, @RequestBody UndoRequest request) {
        recordService.undoChange(historyId, request.changedBy());
        return ResponseEntity.noContent().build();
    }
}
