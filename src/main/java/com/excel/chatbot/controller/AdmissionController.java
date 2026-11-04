package com.excel.chatbot.controller;

import com.excel.chatbot.model.AdmissionEnquiry;
import com.excel.chatbot.service.AdmissionEnquiryService;
import com.excel.chatbot.service.GoogleSheetsService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admissions")
public class AdmissionController {

    private final AdmissionEnquiryService enquiryService;
    private final GoogleSheetsService sheetsService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public AdmissionController(AdmissionEnquiryService enquiryService, GoogleSheetsService sheetsService) {
        this.enquiryService = enquiryService;
        this.sheetsService = sheetsService;
    }

    @GetMapping("/form-config")
    public ResponseEntity<List<Map<String, Object>>> getFormConfig() {
        try {
            ClassPathResource resource = new ClassPathResource("admission-form.json");
            try (InputStream is = resource.getInputStream()) {
                List<Map<String, Object>> steps = objectMapper.readValue(is, new TypeReference<List<Map<String, Object>>>() {});
                
                // Dynamically populate available courses from Google Sheets if available
                List<String> dynamicCourses = sheetsService.getCourses();
                if (dynamicCourses != null && !dynamicCourses.isEmpty()) {
                    for (Map<String, Object> step : steps) {
                        if ("courses".equals(step.get("dynamic"))) {
                            step.put("options", dynamicCourses);
                        }
                    }
                }
                return ResponseEntity.ok(steps);
            }
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/enquiry")
    public ResponseEntity<Map<String, Object>> submitEnquiry(@RequestBody AdmissionEnquiry enquiry, HttpServletRequest request) {
        String clientIp = request.getHeader("X-Forwarded-For");
        if (clientIp == null || clientIp.isEmpty()) {
            clientIp = request.getRemoteAddr();
        } else {
            clientIp = clientIp.split(",")[0].trim();
        }

        Map<String, Object> result = enquiryService.processEnquiry(enquiry, clientIp);
        if (Boolean.TRUE.equals(result.get("success"))) {
            return ResponseEntity.ok(result);
        } else {
            return ResponseEntity.badRequest().body(result);
        }
    }
}

