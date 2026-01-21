package com.example.doctypeeditor.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChangeRecord {
    private Integer rowId;
    private String fieldName;
    private String oldValue;
    private String newValue;
}
