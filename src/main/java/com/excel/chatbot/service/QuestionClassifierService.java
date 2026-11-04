package com.excel.chatbot.service;

import com.excel.chatbot.nlp.SynonymDictionary;
import com.excel.chatbot.nlp.TextNormalizer;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class QuestionClassifierService {

    private final TextNormalizer normalizer;
    private final SynonymDictionary synonymDict;

    private static final Map<String, Set<String>> BASE_CATEGORY_KEYWORDS = new LinkedHashMap<>();

    static {
        BASE_CATEGORY_KEYWORDS.put("Courses", Set.of(
                "course", "courses", "programme", "program", "programs", "degree", "degrees",
                "b.e", "b.tech", "be", "btech", "m.e", "m.tech", "me", "mtech", "mba", "mca",
                "duration", "syllabus", "curriculum", "semester", "branch", "branches",
                "specialization", "engineering", "undergraduate", "postgraduate", "diploma",
                "stream", "subject", "academics"
        ));
        BASE_CATEGORY_KEYWORDS.put("Admissions", Set.of(
                "admission", "admissions", "eligibility", "apply", "application", "enroll",
                "enrollment", "entrance", "cutoff", "cut-off", "seat", "seats", "intake",
                "merit", "counselling", "counseling", "documents", "fee", "fees", "tuition",
                "scholarship", "deadline", "procedure", "quota", "tnea", "cost", "charges"
        ));
        BASE_CATEGORY_KEYWORDS.put("Departments", Set.of(
                "department", "departments", "dept", "depts", "cse", "ece", "eee", "mech",
                "civil", "it", "computer science", "electronics", "electrical", "mechanical",
                "information technology", "artificial intelligence", "data science", "ai",
                "aids", "aiml", "faculty", "hod", "staff", "professor", "biomedical",
                "aeronautical", "food tech", "cyber security"
        ));
        BASE_CATEGORY_KEYWORDS.put("Facilities", Set.of(
                "facility", "facilities", "hostel", "hostels", "library", "lab", "labs",
                "laboratory", "laboratories", "transport", "transportation", "bus", "buses",
                "canteen", "cafeteria", "sports", "gym", "wifi", "internet", "parking",
                "auditorium", "seminar", "workshop", "campus", "ground", "accommodation",
                "room", "rooms", "mess", "food", "dining"
        ));
        BASE_CATEGORY_KEYWORDS.put("Contact Information", Set.of(
                "contact", "contacts", "phone", "telephone", "mobile", "number", "email",
                "mail", "address", "location", "where", "map", "direction", "directions",
                "reach", "visit", "office", "timing", "timings", "hours", "website", "web",
                "url", "link", "helpline", "enquiry", "inquiry", "call"
        ));
    }

    public QuestionClassifierService(TextNormalizer normalizer, SynonymDictionary synonymDict) {
        this.normalizer = normalizer;
        this.synonymDict = synonymDict;
    }

    public ClassificationResult classify(String question, List<String> dynamicVocabulary) {
        if (question == null || question.isBlank()) {
            return new ClassificationResult(null, 0.0);
        }

        List<String> tokens = normalizer.tokenizeAndNormalize(question);
        if (tokens.isEmpty()) {
            return new ClassificationResult(null, 0.0);
        }

        Map<String, Double> scores = new LinkedHashMap<>();
        for (String cat : BASE_CATEGORY_KEYWORDS.keySet()) {
            scores.put(cat, 0.0);
        }

        for (String token : tokens) {
            String canon = synonymDict.getCanonical(token);

            for (Map.Entry<String, Set<String>> entry : BASE_CATEGORY_KEYWORDS.entrySet()) {
                String cat = entry.getKey();
                Set<String> keywords = entry.getValue();

                if (keywords.contains(token)) {
                    scores.put(cat, scores.get(cat) + 2.0);
                } else if (canon != null && keywords.contains(canon)) {
                    scores.put(cat, scores.get(cat) + 2.5);
                } else {
                    // Fuzzy match
                    for (String kw : keywords) {
                        if (normalizer.fuzzyMatch(token, kw)) {
                            scores.put(cat, scores.get(cat) + 1.2);
                            break;
                        }
                    }
                }
            }

            // Match against live sheet vocabulary if present
            if (dynamicVocabulary != null) {
                for (String vocab : dynamicVocabulary) {
                    if (vocab != null && vocab.toLowerCase().contains(token)) {
                        for (String cat : BASE_CATEGORY_KEYWORDS.keySet()) {
                            if (vocab.equalsIgnoreCase(cat)) {
                                scores.put(cat, scores.get(cat) + 3.0);
                            }
                        }
                    }
                }
            }
        }

        double maxScore = 0.0;
        String bestCategory = null;
        double totalScore = 0.0;

        for (Map.Entry<String, Double> e : scores.entrySet()) {
            totalScore += e.getValue();
            if (e.getValue() > maxScore) {
                maxScore = e.getValue();
                bestCategory = e.getKey();
            }
        }

        if (maxScore == 0.0) {
            return new ClassificationResult(null, 0.0);
        }

        double confidence = Math.min(1.0, maxScore / (totalScore > 0 ? (totalScore * 0.7 + 1.0) : 1.0));
        return new ClassificationResult(bestCategory, confidence);
    }

    public String classifyQuestion(String question) {
        ClassificationResult res = classify(question, null);
        return res.confidence() >= 0.3 ? res.category() : null;
    }

    public record ClassificationResult(String category, double confidence) {}
}
