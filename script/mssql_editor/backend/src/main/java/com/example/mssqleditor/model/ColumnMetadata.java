package com.example.mssqleditor.model;

public record ColumnMetadata(String name, String dataType, boolean nullable, boolean primaryKey) {
}
