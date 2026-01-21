package com.example.doctypeeditor.service;

import com.example.doctypeeditor.dto.ChangeRecord;
import com.example.doctypeeditor.entity.DocTypeDetails;
import com.example.doctypeeditor.repository.DocTypeDetailsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class DocTypeDetailsService {

    @Autowired
    private DocTypeDetailsRepository repository;

    public List<DocTypeDetails> findAll() {
        return repository.findAll();
    }

    public Optional<DocTypeDetails> findById(Integer id) {
        return repository.findById(id);
    }

    @Transactional
    public DocTypeDetails save(DocTypeDetails docTypeDetails) {
        return repository.save(docTypeDetails);
    }

    /**
     * Gibt alle eindeutigen Werte für ein bestimmtes Feld zurück
     */
    public List<String> getDistinctValuesForField(String fieldName) {
        switch (fieldName.toLowerCase()) {
            case "dokumenttyp":
                return repository.findAllDokumenttyp();
            case "prozess":
                return repository.findAllProzess();
            case "fachbereich":
                return repository.findAllFachbereich();
            case "aktenplan21c":
                return repository.findAllAktenplan21c();
            case "stichwort":
                return repository.findAllStichwort();
            case "specialpartnernr":
                return repository.findAllSpecialPartnerNr();
            case "fachbereichkz":
                return repository.findAllFachbereichKz();
            case "zustaendigkeit":
                return repository.findAllZustaendigkeit();
            default:
                // Für andere Felder (Integer, Boolean) alle eindeutigen Werte sammeln
                return getAllDistinctValuesForField(fieldName);
        }
    }

    /**
     * Generische Methode um alle eindeutigen Werte für ein Feld zu erhalten
     */
    private List<String> getAllDistinctValuesForField(String fieldName) {
        List<DocTypeDetails> allRecords = repository.findAll();
        Set<String> distinctValues = new HashSet<>();

        try {
            for (DocTypeDetails record : allRecords) {
                Field field = DocTypeDetails.class.getDeclaredField(fieldName);
                field.setAccessible(true);
                Object value = field.get(record);
                if (value != null) {
                    distinctValues.add(value.toString());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return new ArrayList<>(distinctValues);
    }

    /**
     * Fuzzy-Suche: Filtert Werte basierend auf der Eingabe
     */
    public List<String> searchValuesForField(String fieldName, String searchTerm) {
        List<String> allValues = getDistinctValuesForField(fieldName);

        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            return allValues;
        }

        String searchLower = searchTerm.toLowerCase();

        // Fuzzy-Matching: Zeige Werte die den Suchbegriff enthalten
        return allValues.stream()
                .filter(value -> value != null && value.toLowerCase().contains(searchLower))
                .sorted((a, b) -> {
                    // Sortiere nach Relevanz: Exakte Übereinstimmungen zuerst, dann Starts-With, dann Contains
                    boolean aExact = a.equalsIgnoreCase(searchTerm);
                    boolean bExact = b.equalsIgnoreCase(searchTerm);
                    if (aExact && !bExact) return -1;
                    if (!aExact && bExact) return 1;

                    boolean aStarts = a.toLowerCase().startsWith(searchLower);
                    boolean bStarts = b.toLowerCase().startsWith(searchLower);
                    if (aStarts && !bStarts) return -1;
                    if (!aStarts && bStarts) return 1;

                    return a.compareTo(b);
                })
                .collect(Collectors.toList());
    }

    /**
     * Wendet alle Änderungen auf die Datensätze an
     */
    @Transactional
    public void applyChanges(List<ChangeRecord> changes) throws Exception {
        Map<Integer, List<ChangeRecord>> changesByRow = changes.stream()
                .collect(Collectors.groupingBy(ChangeRecord::getRowId));

        for (Map.Entry<Integer, List<ChangeRecord>> entry : changesByRow.entrySet()) {
            Integer rowId = entry.getKey();
            List<ChangeRecord> rowChanges = entry.getValue();

            Optional<DocTypeDetails> optionalRecord = repository.findById(rowId);
            if (optionalRecord.isPresent()) {
                DocTypeDetails record = optionalRecord.get();

                for (ChangeRecord change : rowChanges) {
                    applyChangeToRecord(record, change);
                }

                repository.save(record);
            }
        }
    }

    /**
     * Wendet eine einzelne Änderung auf einen Datensatz an
     */
    private void applyChangeToRecord(DocTypeDetails record, ChangeRecord change) throws Exception {
        String fieldName = change.getFieldName();
        String newValue = change.getNewValue();

        Field field = DocTypeDetails.class.getDeclaredField(fieldName);
        field.setAccessible(true);

        Object convertedValue = convertValueToFieldType(newValue, field.getType());
        field.set(record, convertedValue);
    }

    /**
     * Konvertiert einen String-Wert in den entsprechenden Feldtyp
     */
    private Object convertValueToFieldType(String value, Class<?> fieldType) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }

        if (fieldType == String.class) {
            return value;
        } else if (fieldType == Integer.class) {
            return Integer.parseInt(value);
        } else if (fieldType == Boolean.class) {
            return Boolean.parseBoolean(value);
        } else if (fieldType == Double.class) {
            return Double.parseDouble(value);
        } else if (fieldType == Long.class) {
            return Long.parseLong(value);
        }

        return value;
    }
}
