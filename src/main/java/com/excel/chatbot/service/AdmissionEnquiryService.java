package com.excel.chatbot.service;

import com.excel.chatbot.model.AdmissionEnquiry;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class AdmissionEnquiryService {

    private static final Logger logger = LoggerFactory.getLogger(AdmissionEnquiryService.class);

    @Value("${google.apps-script.url:}")
    private String appsScriptUrl;

    @Value("${google.apps-script.token:}")
    private String appsScriptToken;

    @Value("${google.enquiry.sheet-id:1lYbmg-7LYewpJhxTVqyYZSToFS7XjUyLDdwIQkJn5zA}")
    private String enquirySheetId;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private volatile HttpClient httpClient;

    private HttpClient getHttpClient() {
        if (httpClient == null) {
            synchronized (this) {
                if (httpClient == null) {
                    httpClient = HttpClient.newBuilder()
                            .version(HttpClient.Version.HTTP_1_1)
                            .followRedirects(HttpClient.Redirect.ALWAYS)
                            .connectTimeout(Duration.ofSeconds(10))
                            .build();
                }
            }
        }
        return httpClient;
    }

    // Rate limiting: IP -> list of submission epoch milliseconds
    private final Map<String, List<Long>> rateLimitMap = new ConcurrentHashMap<>();
    // Duplicate detection: SHA-256 hash -> submission epoch millis
    private final Map<String, Long> duplicateSubmissionMap = new ConcurrentHashMap<>();

    private static final int MAX_SUBMISSIONS_PER_HOUR = 10;
    private static final long ONE_HOUR_MS = 60 * 60 * 1000L;
    private static final long ONE_DAY_MS = 24 * 60 * 60 * 1000L;

    public Map<String, Object> processEnquiry(AdmissionEnquiry enquiry, String clientIp) {
        Map<String, Object> response = new HashMap<>();

        // 1. Honeypot check (bot prevention)
        if (enquiry.getHoneypot() != null && !enquiry.getHoneypot().trim().isEmpty()) {
            logger.warn("Honeypot triggered from IP: {}", clientIp);
            response.put("success", false);
            response.put("error", "Invalid submission.");
            return response;
        }

        // 2. Rate limiting check
        if (!checkRateLimit(clientIp)) {
            logger.warn("Rate limit exceeded for IP: {}", clientIp);
            response.put("success", false);
            response.put("error", "Too many submissions from this connection. Please try again after an hour.");
            return response;
        }

        // 3. Validation - Name and Mobile are the core required fields
        if (enquiry.getFullName() == null || enquiry.getFullName().trim().length() < 2) {
            response.put("success", false);
            response.put("error", "Please provide your full name (at least 2 letters).");
            return response;
        }
        if (enquiry.getMobile() == null || !enquiry.getMobile().trim().matches("^[6-9]\\d{9}$")) {
            response.put("success", false);
            response.put("error", "Please provide a valid 10-digit Indian mobile number.");
            return response;
        }

        // Support half-filled / partial form enquiries with high detail level
        boolean isPartial = false;
        if (enquiry.getProgramme1() == null || enquiry.getProgramme1().trim().isEmpty()) {
            enquiry.setProgramme1("General Admission Enquiry / Counselling Pending");
            isPartial = true;
        }
        if (enquiry.getDistrict() == null || enquiry.getDistrict().trim().isEmpty()) {
            enquiry.setDistrict("Not specified");
        }
        if (enquiry.getBoard() == null || enquiry.getBoard().trim().isEmpty()) {
            enquiry.setBoard("Not specified");
        }
        if (enquiry.getStream() == null || enquiry.getStream().trim().isEmpty()) {
            enquiry.setStream("Not specified");
        }
        if (enquiry.getHostelRequired() == null || enquiry.getHostelRequired().trim().isEmpty()) {
            enquiry.setHostelRequired("Not specified");
        }
        if (enquiry.getTransportRequired() == null || enquiry.getTransportRequired().trim().isEmpty()) {
            enquiry.setTransportRequired("Not specified");
        }
        if (enquiry.getContactTime() == null || enquiry.getContactTime().trim().isEmpty()) {
            enquiry.setContactTime("Anytime");
        }
        if (enquiry.getAdmissionType() == null || enquiry.getAdmissionType().trim().isEmpty()) {
            enquiry.setAdmissionType("Regular");
        }

        // 4. Duplicate submission check
        String dupKey = generateDuplicateKey(enquiry.getMobile(), enquiry.getProgramme1());
        Long lastSubmitted = duplicateSubmissionMap.get(dupKey);
        long now = System.currentTimeMillis();
        if (lastSubmitted != null && (now - lastSubmitted) < ONE_DAY_MS) {
            logger.info("Duplicate submission detected for mobile hash key");
            response.put("success", true);
            response.put("message", "We already have an active admission enquiry for this mobile number. Our admissions office will get in touch with you shortly!");
            response.put("duplicate", true);
            return response;
        }

        // 5. Server-side cutoff calculation
        if (enquiry.getMaths() != null && enquiry.getPhysics() != null && enquiry.getChemistry() != null) {
            double cutoff = enquiry.getMaths() + (enquiry.getPhysics() / 2.0) + (enquiry.getChemistry() / 2.0);
            cutoff = Math.round(cutoff * 100.0) / 100.0;
            enquiry.setCalculatedCutoff(cutoff);
        }

        // 6. Assign IDs & Timestamps
        String enquiryId = "EXCEL-" + (now % 10000000);
        enquiry.setEnquiryId(enquiryId);
        enquiry.setSubmissionTime(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));

        // 7. Prevent formula injection on all string inputs
        sanitizeEnquiry(enquiry);

        // 8. Forward to Google Apps Script / Sheet
        boolean saved = forwardToAppsScript(enquiry);

        duplicateSubmissionMap.put(dupKey, now);
        logger.info("Admission enquiry processed successfully. EnquiryID: {}, Partial: {}, SavedToCloud: {}",
                enquiryId, isPartial, saved);

        response.put("success", true);
        response.put("enquiryId", enquiryId);
        response.put("isPartial", isPartial);
        response.put("calculatedCutoff", enquiry.getCalculatedCutoff());
        response.put("message", "Your admission enquiry has been submitted successfully! Our admissions counsellor will contact you at your preferred time.");
        return response;
    }

    private boolean checkRateLimit(String clientIp) {
        long now = System.currentTimeMillis();
        List<Long> timestamps = rateLimitMap.computeIfAbsent(clientIp, k -> new ArrayList<>());
        synchronized (timestamps) {
            timestamps.removeIf(t -> now - t > ONE_HOUR_MS);
            if (timestamps.size() >= MAX_SUBMISSIONS_PER_HOUR) {
                return false;
            }
            timestamps.add(now);
            return true;
        }
    }

    private String generateDuplicateKey(String mobile, String programme1) {
        try {
            String raw = mobile.trim() + "|" + (programme1 != null ? programme1.trim().toLowerCase() : "") + "|" + LocalDate.now().toString();
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(raw.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder();
            for (byte b : hash) {
                hex.append(String.format("%02x", b));
            }
            return hex.toString();
        } catch (Exception e) {
            return mobile.trim() + "_" + LocalDate.now().toString();
        }
    }

    private void sanitizeEnquiry(AdmissionEnquiry e) {
        e.setFullName(sanitizeString(e.getFullName()));
        e.setDistrict(sanitizeString(e.getDistrict()));
        e.setBoard(sanitizeString(e.getBoard()));
        e.setStream(sanitizeString(e.getStream()));
        e.setProgramme1(sanitizeString(e.getProgramme1()));
        e.setProgramme2(sanitizeString(e.getProgramme2()));
        e.setAdmissionType(sanitizeString(e.getAdmissionType()));
        e.setMessage(sanitizeString(e.getMessage()));
    }

    private String sanitizeString(String input) {
        if (input == null) return null;
        String trimmed = input.trim();
        if (trimmed.startsWith("=") || trimmed.startsWith("+") || trimmed.startsWith("-") || trimmed.startsWith("@")) {
            return "'" + trimmed;
        }
        return trimmed;
    }

    private boolean forwardToAppsScript(AdmissionEnquiry enquiry) {
        if (appsScriptUrl == null || appsScriptUrl.trim().isEmpty()) {
            logger.info("Google Apps Script URL not configured. Enquiry stored in local memory only.");
            return false;
        }

        try {
            // Build explicit record map matching the Apps Script HEADERS exactly (PascalCase)
            Map<String, Object> record = new LinkedHashMap<>();
            record.put("EnquiryID", enquiry.getEnquiryId());
            record.put("SubmittedAt", enquiry.getSubmissionTime());
            record.put("FullName", enquiry.getFullName());
            record.put("Mobile", enquiry.getMobile());
            record.put("Email", enquiry.getEmail() != null ? enquiry.getEmail() : "");
            record.put("District", enquiry.getDistrict());
            record.put("Board", enquiry.getBoard());
            record.put("Stream", enquiry.getStream());
            record.put("TwelfthPercentage", enquiry.getTwelfthPercentage() != null ? enquiry.getTwelfthPercentage() : "");
            record.put("Maths", enquiry.getMaths() != null ? enquiry.getMaths() : "");
            record.put("Physics", enquiry.getPhysics() != null ? enquiry.getPhysics() : "");
            record.put("Chemistry", enquiry.getChemistry() != null ? enquiry.getChemistry() : "");
            record.put("Cutoff", enquiry.getCalculatedCutoff() != null ? enquiry.getCalculatedCutoff() : "");
            record.put("CutoffSource", enquiry.getCalculatedCutoff() != null ? "Calculated" : "N/A");
            record.put("Programme1", enquiry.getProgramme1());
            record.put("Programme2", enquiry.getProgramme2() != null ? enquiry.getProgramme2() : "");
            record.put("AdmissionType", enquiry.getAdmissionType());
            record.put("HostelRequired", enquiry.getHostelRequired());
            record.put("TransportRequired", enquiry.getTransportRequired());
            record.put("PreferredContactTime", enquiry.getContactTime());
            record.put("Message", enquiry.getMessage() != null ? enquiry.getMessage() : "");
            record.put("ConsentGiven", enquiry.getConsentGiven() != null ? enquiry.getConsentGiven() : false);
            record.put("Source", "AI Chatbot");
            record.put("Status", "New");
            record.put("DuplicateKey", "");

            Map<String, Object> payload = new HashMap<>();
            payload.put("action", "submitEnquiry");
            payload.put("token", appsScriptToken);
            payload.put("sheetId", enquirySheetId);
            payload.put("record", record);

            String jsonPayload = objectMapper.writeValueAsString(payload);
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(appsScriptUrl))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonPayload, StandardCharsets.UTF_8))
                    .timeout(Duration.ofSeconds(15))
                    .build();

            HttpResponse<String> response = getHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() >= 200 && response.statusCode() < 400) {
                logger.info("Enquiry forwarded to Google Apps Script successfully. Response status: {}", response.statusCode());
                return true;
            } else {
                logger.warn("Apps Script responded with status: {}, body: {}", response.statusCode(), response.body());
                return false;
            }
        } catch (Exception e) {
            logger.warn("Failed to forward enquiry to Google Apps Script: {}", e.getMessage());
            return false;
        }
    }
}
