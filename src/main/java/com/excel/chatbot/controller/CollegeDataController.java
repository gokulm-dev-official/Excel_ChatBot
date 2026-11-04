package com.excel.chatbot.controller;

import com.excel.chatbot.model.CategoryResponse;
import com.excel.chatbot.model.CollegeInfo;
import com.excel.chatbot.service.GoogleSheetsService;
import com.excel.chatbot.service.KnowledgeSearchService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/college")
public class CollegeDataController {

    private final GoogleSheetsService sheetsService;
    private final KnowledgeSearchService knowledgeSearch;

    public CollegeDataController(GoogleSheetsService sheetsService, KnowledgeSearchService knowledgeSearch) {
        this.sheetsService = sheetsService;
        this.knowledgeSearch = knowledgeSearch;
    }

    @GetMapping("/categories")
    public ResponseEntity<CategoryResponse> getCategories() {
        CategoryResponse response = new CategoryResponse();
        response.setCategories(sheetsService.getCategories());

        Map<String, List<String>> subcategories = new HashMap<>();
        for (String cat : response.getCategories()) {
            subcategories.put(cat, sheetsService.getSubcategories(cat));
        }
        response.setSubcategories(subcategories);

        Map<String, List<String>> departments = new HashMap<>();
        departments.put("all", sheetsService.getDepartments());
        response.setDepartments(departments);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/data")
    public ResponseEntity<List<CollegeInfo>> getAllData() {
        return ResponseEntity.ok(sheetsService.getAllData());
    }

    @GetMapping("/data/{category}")
    public ResponseEntity<List<CollegeInfo>> getByCategory(@PathVariable String category) {
        return ResponseEntity.ok(sheetsService.getByCategory(category));
    }

    @GetMapping("/departments")
    public ResponseEntity<List<String>> getDepartments() {
        return ResponseEntity.ok(sheetsService.getDepartments());
    }

    @GetMapping("/courses")
    public ResponseEntity<List<String>> getCourses() {
        return ResponseEntity.ok(sheetsService.getCourses());
    }

    @PostMapping("/refresh")
    public ResponseEntity<Map<String, Object>> refreshData(@RequestHeader(value = "X-Admin-Key", required = false) String adminKey) {
        // Enforce basic admin authorization
        if (adminKey == null || !adminKey.equals(System.getenv().getOrDefault("ADMIN_KEY", "excel-admin-secret-2026"))) {
            Map<String, Object> forbidden = new HashMap<>();
            forbidden.put("error", "Unauthorized. Administrative key required.");
            return ResponseEntity.status(403).body(forbidden);
        }

        sheetsService.refreshData();
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("recordCount", sheetsService.getRecordCount());
        result.put("lastFetchTime", sheetsService.getLastFetchTime());
        return ResponseEntity.ok(result);
    }
}

