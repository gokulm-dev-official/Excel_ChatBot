package com.excel.chatbot.service;

import com.excel.chatbot.model.ChatResponse;
import com.excel.chatbot.model.CollegeInfo;
import com.excel.chatbot.model.ContactInfo;
import com.excel.chatbot.nlp.ContactExtractor;
import com.excel.chatbot.nlp.IntentDetector;
import com.excel.chatbot.nlp.TextNormalizer;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ChatService {

    private final IntentDetector intentDetector;
    private final QuestionClassifierService classifier;
    private final KnowledgeSearchService knowledgeSearch;
    private final ResponseComposer composer;
    private final FollowUpOptionsBuilder optionsBuilder;
    private final ContactExtractor contactExtractor;
    private final GoogleSheetsService sheetsService;
    private final TextNormalizer normalizer;

    public ChatService(IntentDetector intentDetector,
                       QuestionClassifierService classifier,
                       KnowledgeSearchService knowledgeSearch,
                       ResponseComposer composer,
                       FollowUpOptionsBuilder optionsBuilder,
                       ContactExtractor contactExtractor,
                       GoogleSheetsService sheetsService,
                       TextNormalizer normalizer) {
        this.intentDetector = intentDetector;
        this.classifier = classifier;
        this.knowledgeSearch = knowledgeSearch;
        this.composer = composer;
        this.optionsBuilder = optionsBuilder;
        this.contactExtractor = contactExtractor;
        this.sheetsService = sheetsService;
        this.normalizer = normalizer;
    }

    public ChatResponse processMessage(String rawMessage, String clientCategory, String context) {
        ChatResponse resp = new ChatResponse();
        resp.setSource("college-records");

        if (rawMessage == null || rawMessage.trim().isEmpty()) {
            resp.setAnswer(composer.composeFallback());
            resp.setFollowUpOptions(optionsBuilder.buildOptions(null, null));
            resp.setIntent("out-of-scope");
            return resp;
        }

        String message = rawMessage.trim();
        String intent = intentDetector.detectIntent(message);
        resp.setIntent(intent);

        // 1. Check for Apply / Admission Enquiry intent
        if ("apply-for-admission".equals(intent) || "apply".equalsIgnoreCase(message)) {
            resp.setAnswer("I'm opening our step-by-step Admission Enquiry Assistant for you. Let's get started!");
            resp.setCategory("Admissions");
            resp.setStartAdmissionForm(true);
            resp.setVerified(true);
            resp.setConfidence(1.0);
            return resp;
        }

        // 2. Greeting intent
        if ("greeting".equals(intent)) {
            resp.setAnswer(composer.composeGreeting());
            resp.setConfidence(1.0);
            resp.setVerified(false);
            resp.setFollowUpOptions(optionsBuilder.buildOptions(null, null));
            return resp;
        }

        // 3. Thanks intent
        if ("thanks".equals(intent)) {
            resp.setAnswer(composer.composeThanks());
            resp.setConfidence(1.0);
            resp.setVerified(false);
            resp.setFollowUpOptions(optionsBuilder.buildOptions(null, null));
            return resp;
        }

        // 4. Goodbye intent
        if ("goodbye".equals(intent)) {
            resp.setAnswer(composer.composeGoodbye());
            resp.setConfidence(1.0);
            resp.setVerified(false);
            return resp;
        }

        // 5. Help / Menu intent
        if ("help-menu".equals(intent)) {
            resp.setAnswer(composer.composeHelpMenu());
            resp.setConfidence(1.0);
            resp.setVerified(false);
            resp.setFollowUpOptions(optionsBuilder.buildOptions(null, null));
            return resp;
        }

        // 6. Resolve category
        String detectedCategory = clientCategory;
        QuestionClassifierService.ClassificationResult classResult = classifier.classify(message, sheetsService.getDepartments());
        if ((detectedCategory == null || detectedCategory.isBlank()) && classResult.confidence() >= 0.30) {
            detectedCategory = classResult.category();
        }
        resp.setCategory(detectedCategory);

        // Check resilience: if sheets service has no data
        if (!sheetsService.isAvailable()) {
            resp.setAnswer("Official college records are temporarily updating. Please try again in a few moments or contact the college office directly at +91 98427 13789.");
            resp.setFollowUpOptions(optionsBuilder.buildOptions(null, null));
            resp.setVerified(false);
            return resp;
        }

        // 7. Search knowledge records with deterministic scoring
        KnowledgeSearchService.SearchResult searchResult = knowledgeSearch.search(message, detectedCategory);
        resp.setConfidence(searchResult.confidence());

        if (!searchResult.records().isEmpty()) {
            // Found high-confidence answer(s)
            List<CollegeInfo> records = searchResult.records();
            resp.setVerified(true);
            resp.setAnswer(composer.composeAnswer(records, detectedCategory));

            List<String> recordIds = new ArrayList<>();
            for (CollegeInfo r : records) {
                if (r.getId() != null) recordIds.add(r.getId());
            }
            resp.setRecordIds(recordIds);

            // Extract structured contacts if any
            List<ContactInfo> contacts = contactExtractor.extractContacts(resp.getAnswer());
            resp.setContacts(contacts);

            // Build options based on category
            resp.setFollowUpOptions(optionsBuilder.buildOptions(detectedCategory, null));
            return resp;
        }

        if (!searchResult.suggestions().isEmpty()) {
            // Ambiguous query: return suggestions
            resp.setAnswer(composer.composeSuggestions(searchResult.suggestions()));
            resp.setSuggestions(searchResult.suggestions());
            resp.setFollowUpOptions(optionsBuilder.buildOptions(detectedCategory, searchResult.suggestions()));
            resp.setVerified(false);
            return resp;
        }

        // Out of scope / no match
        if (detectedCategory != null && !detectedCategory.isBlank()) {
            resp.setAnswer(composer.composeFallback());
            resp.setFollowUpOptions(optionsBuilder.buildOptions(detectedCategory, null));
        } else {
            resp.setAnswer(composer.composeOutOfScope());
            resp.setFollowUpOptions(optionsBuilder.buildOptions(null, null));
        }
        resp.setVerified(false);

        return resp;
    }
}
