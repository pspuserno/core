package com.example.doctypeeditor.controller;

import com.example.doctypeeditor.dto.ChangeBatch;
import com.example.doctypeeditor.dto.ChangeRecord;
import com.example.doctypeeditor.entity.DocTypeDetails;
import com.example.doctypeeditor.service.DocTypeDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Controller
public class DocTypeDetailsController {

    @Autowired
    private DocTypeDetailsService service;

    /**
     * Hauptseite mit der Tabelle
     */
    @GetMapping("/")
    public String index(Model model) {
        List<DocTypeDetails> records = service.findAll();
        model.addAttribute("records", records);
        return "index";
    }

    /**
     * API: Alle Datensätze abrufen
     */
    @GetMapping("/api/records")
    @ResponseBody
    public List<DocTypeDetails> getAllRecords() {
        return service.findAll();
    }

    /**
     * API: Eindeutige Werte für ein Feld abrufen (mit optionaler Suche)
     */
    @GetMapping("/api/field-values/{fieldName}")
    @ResponseBody
    public List<String> getFieldValues(
            @PathVariable String fieldName,
            @RequestParam(required = false) String search) {

        if (search != null && !search.isEmpty()) {
            return service.searchValuesForField(fieldName, search);
        }

        return service.getDistinctValuesForField(fieldName);
    }

    /**
     * Bestätigungsseite anzeigen
     */
    @GetMapping("/confirm")
    public String confirmPage() {
        return "confirm";
    }

    /**
     * API: Änderungen anwenden
     */
    @PostMapping("/api/apply-changes")
    @ResponseBody
    public ResponseEntity<?> applyChanges(@RequestBody ChangeBatch changeBatch) {
        try {
            service.applyChanges(changeBatch.getChanges());
            return ResponseEntity.ok(Map.of("success", true, "message", "Änderungen erfolgreich übernommen"));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", "Fehler beim Übernehmen der Änderungen: " + e.getMessage()));
        }
    }
}
