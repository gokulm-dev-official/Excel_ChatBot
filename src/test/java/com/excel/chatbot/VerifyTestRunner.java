package com.excel.chatbot;

import com.excel.chatbot.config.GoogleSheetsConfig;
import com.excel.chatbot.model.AdmissionEnquiry;
import com.excel.chatbot.model.ChatResponse;
import com.excel.chatbot.nlp.ContactExtractor;
import com.excel.chatbot.nlp.IntentDetector;
import com.excel.chatbot.nlp.SynonymDictionary;
import com.excel.chatbot.nlp.TextNormalizer;
import com.excel.chatbot.service.*;

import java.util.List;

public class VerifyTestRunner {

    public static void main(String[] args) {
        System.out.println("=================================================");
        System.out.println("  EXCEL ENGINEERING COLLEGE CHATBOT VERIFICATION ");
        System.out.println("=================================================\n");

        int passed = 0;
        int total = 0;

        // Initialize Services
        GoogleSheetsConfig sheetsConfig = new GoogleSheetsConfig();
        GoogleSheetsService sheetsService = new GoogleSheetsService(sheetsConfig);
        sheetsService.init();

        TextNormalizer normalizer = new TextNormalizer();
        SynonymDictionary synonymDict = new SynonymDictionary();
        synonymDict.init();

        IntentDetector intentDetector = new IntentDetector(normalizer);
        QuestionClassifierService classifier = new QuestionClassifierService(normalizer, synonymDict);
        KnowledgeSearchService knowledgeSearch = new KnowledgeSearchService(sheetsService, normalizer, synonymDict);
        ResponseComposer responseComposer = new ResponseComposer();
        FollowUpOptionsBuilder followUpBuilder = new FollowUpOptionsBuilder(sheetsService);
        ContactExtractor contactExtractor = new ContactExtractor();

        ChatService chatService = new ChatService(
                intentDetector,
                classifier,
                knowledgeSearch,
                responseComposer,
                followUpBuilder,
                contactExtractor,
                sheetsService,
                normalizer
        );

        // Test 1: Categories
        total++;
        List<String> categories = sheetsService.getCategories();
        System.out.println("[TEST 1] Google Sheets Categories: " + categories);
        if (categories.contains("Courses") && categories.contains("Admissions") &&
            categories.contains("Departments") && categories.contains("Facilities") &&
            categories.contains("Contact Information")) {
            System.out.println("  -> PASSED: All 5 mandatory categories present.");
            passed++;
        } else {
            System.out.println("  -> FAILED: Missing categories.");
        }

        // Test 2: Greeting
        total++;
        ChatResponse rGreeting = chatService.processMessage("Hello, good morning!", null, null);
        System.out.println("\n[TEST 2] Greeting: " + rGreeting.getAnswer());
        if (rGreeting.getAnswer().contains("Welcome to Excel Engineering College")) {
            System.out.println("  -> PASSED: Polite greeting handled.");
            passed++;
        } else {
            System.out.println("  -> FAILED");
        }

        // Test 3: Thank you
        total++;
        ChatResponse rThanks = chatService.processMessage("Thank you so much", null, null);
        System.out.println("\n[TEST 3] Thank you: " + rThanks.getAnswer());
        if (rThanks.getAnswer().toLowerCase().contains("welcome")) {
            System.out.println("  -> PASSED: Thank you handled.");
            passed++;
        } else {
            System.out.println("  -> FAILED");
        }

        // Test 4: Courses inquiry
        total++;
        ChatResponse rCourses = chatService.processMessage("What engineering courses are offered?", "Courses", null);
        System.out.println("\n[TEST 4] Courses Query: " + rCourses.getAnswer());
        if (rCourses.isVerified() && rCourses.getAnswer().contains("B.E.")) {
            System.out.println("  -> PASSED: Verified courses returned.");
            passed++;
        } else {
            System.out.println("  -> FAILED");
        }

        // Test 5: Admissions eligibility
        total++;
        ChatResponse rAdm = chatService.processMessage("What is admission eligibility?", "Admissions", null);
        System.out.println("\n[TEST 5] Admissions Query: " + rAdm.getAnswer());
        if (rAdm.isVerified() && rAdm.getAnswer().toLowerCase().contains("eligibility")) {
            System.out.println("  -> PASSED: Verified admission eligibility returned.");
            passed++;
        } else {
            System.out.println("  -> FAILED");
        }

        // Test 6: Departments inquiry
        total++;
        ChatResponse rDept = chatService.processMessage("Tell me about Computer Science and Engineering", "Departments", null);
        System.out.println("\n[TEST 6] Departments Query: " + rDept.getAnswer());
        if (rDept.isVerified() && rDept.getAnswer().toLowerCase().contains("computer science")) {
            System.out.println("  -> PASSED: Verified department information returned.");
            passed++;
        } else {
            System.out.println("  -> FAILED");
        }

        // Test 7: Facilities inquiry (Hostel)
        total++;
        ChatResponse rFac = chatService.processMessage("What hostel facilities are available?", "Facilities", null);
        System.out.println("\n[TEST 7] Facilities Query: " + rFac.getAnswer());
        if (rFac.isVerified() && rFac.getAnswer().toLowerCase().contains("hostel")) {
            System.out.println("  -> PASSED: Verified hostel facilities returned.");
            passed++;
        } else {
            System.out.println("  -> FAILED");
        }

        // Test 8: Contact Information inquiry
        total++;
        ChatResponse rContact = chatService.processMessage("What is the official college phone number?", "Contact Information", null);
        System.out.println("\n[TEST 8] Contact Query: " + rContact.getAnswer());
        if (rContact.isVerified() && rContact.getAnswer().contains("98427")) {
            System.out.println("  -> PASSED: Verified official phone number returned.");
            passed++;
        } else {
            System.out.println("  -> FAILED");
        }

        // Test 9: Manual question entry with auto-classification
        total++;
        ChatResponse rAuto = chatService.processMessage("How can I apply for engineering admission?", null, null);
        System.out.println("\n[TEST 9] Auto-classified Query: Category=" + rAuto.getCategory() + " | Answer=" + rAuto.getAnswer());
        if ("Admissions".equalsIgnoreCase(rAuto.getCategory()) && rAuto.isVerified()) {
            System.out.println("  -> PASSED: Auto-classification and retrieval successful.");
            passed++;
        } else {
            System.out.println("  -> FAILED");
        }

        // Test 10: Unrelated query
        total++;
        ChatResponse rUnrelated = chatService.processMessage("Who won the 1994 World Cup?", null, null);
        System.out.println("\n[TEST 10] Unrelated Query: " + rUnrelated.getAnswer());
        if (!rUnrelated.isVerified() && rUnrelated.getAnswer().contains("Excel Engineering College")) {
            System.out.println("  -> PASSED: Politely redirected to the 5 supported categories.");
            passed++;
        } else {
            System.out.println("  -> FAILED");
        }

        // Test 11: Half-filled Admission Enquiry (Partial Submission)
        total++;
        AdmissionEnquiryService enquiryService = new AdmissionEnquiryService();
        AdmissionEnquiry partial = new AdmissionEnquiry();
        partial.setFullName("Kavitha R");
        partial.setMobile("9842112233");
        // No programme1, no marks, no quota - only half filled
        java.util.Map<String, Object> partialRes = enquiryService.processEnquiry(partial, "127.0.0.10");
        System.out.println("\n[TEST 11] Half-filled Enquiry: Success=" + partialRes.get("success") + " | ID=" + partialRes.get("enquiryId") + " | isPartial=" + partialRes.get("isPartial"));
        if (Boolean.TRUE.equals(partialRes.get("success")) && Boolean.TRUE.equals(partialRes.get("isPartial"))) {
            System.out.println("  -> PASSED: Half-filled form accepted and defaulted with high detail level.");
            passed++;
        } else {
            System.out.println("  -> FAILED");
        }

        // Test 12: Complete Admission Enquiry with Cutoff Calculation
        total++;
        AdmissionEnquiry full = new AdmissionEnquiry();
        full.setFullName("Arun Kumar");
        full.setMobile("9786554433");
        full.setDistrict("Salem");
        full.setMaths(90.0);
        full.setPhysics(80.0);
        full.setChemistry(85.0);
        full.setProgramme1("B.E. Computer Science & Engineering");
        java.util.Map<String, Object> fullRes = enquiryService.processEnquiry(full, "127.0.0.11");
        Double cutoff = (Double) fullRes.get("calculatedCutoff");
        System.out.println("\n[TEST 12] Full Enquiry: Cutoff=" + cutoff + " | ID=" + fullRes.get("enquiryId"));
        // 90 + 80/2 + 85/2 = 90 + 40 + 42.5 = 172.5
        if (Boolean.TRUE.equals(fullRes.get("success")) && cutoff != null && Math.abs(cutoff - 172.5) < 0.01) {
            System.out.println("  -> PASSED: Cutoff formula correctly evaluated (172.5 / 200).");
            passed++;
        } else {
            System.out.println("  -> FAILED");
        }

        System.out.println("\n=================================================");
        System.out.printf("  TOTAL PASSED: %d / %d TESTS\n", passed, total);
        System.out.println("=================================================");

        if (passed == total) {
            System.exit(0);
        } else {
            System.exit(1);
        }
    }
}
