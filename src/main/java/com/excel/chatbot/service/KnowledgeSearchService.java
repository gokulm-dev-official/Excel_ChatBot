package com.excel.chatbot.service;

import com.excel.chatbot.model.CollegeInfo;
import com.excel.chatbot.nlp.SynonymDictionary;
import com.excel.chatbot.nlp.TextNormalizer;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class KnowledgeSearchService {

    private final GoogleSheetsService sheetsService;
    private final TextNormalizer normalizer;
    private final SynonymDictionary synonymDict;

    @Value("${MATCH_ANSWER_THRESHOLD:0.55}")
    private double answerThreshold = 0.55;

    @Value("${MATCH_SUGGEST_THRESHOLD:0.30}")
    private double suggestThreshold = 0.30;

    public KnowledgeSearchService(GoogleSheetsService sheetsService,
                                  TextNormalizer normalizer,
                                  SynonymDictionary synonymDict) {
        this.sheetsService = sheetsService;
        this.normalizer = normalizer;
        this.synonymDict = synonymDict;
    }

    public SearchResult search(String query, String categoryFilter) {
        if (!sheetsService.isAvailable() || query == null || query.isBlank()) {
            return new SearchResult(Collections.emptyList(), Collections.emptyList(), 0.0);
        }

        List<CollegeInfo> allRecords = sheetsService.getAllData();
        List<String> queryTokens = normalizer.tokenizeAndNormalize(query);
        Set<String> queryTrigrams = makeTrigrams(normalizer.clean(query));

        List<ScoredRecord> scored = new ArrayList<>();

        for (CollegeInfo record : allRecords) {
            double score = computeRecordScore(record, query, queryTokens, queryTrigrams, categoryFilter);
            if (score > 0.0) {
                scored.add(new ScoredRecord(record, score));
            }
        }

        scored.sort((a, b) -> Double.compare(b.score, a.score));

        if (scored.isEmpty()) {
            return new SearchResult(Collections.emptyList(), Collections.emptyList(), 0.0);
        }

        double topScore = scored.get(0).score;

        if (topScore >= answerThreshold) {
            // Return top 1-3 matches with high score
            List<CollegeInfo> answers = scored.stream()
                    .filter(s -> s.score >= Math.max(answerThreshold, topScore - 0.20))
                    .limit(3)
                    .map(s -> s.record)
                    .collect(Collectors.toList());
            return new SearchResult(answers, Collections.emptyList(), topScore);
        } else if (topScore >= suggestThreshold) {
            // Suggest ambiguous topics
            List<String> suggestions = scored.stream()
                    .limit(3)
                    .map(s -> s.record.getSubcategory() != null ? s.record.getSubcategory() : s.record.getQuestion())
                    .distinct()
                    .collect(Collectors.toList());
            return new SearchResult(Collections.emptyList(), suggestions, topScore);
        }

        return new SearchResult(Collections.emptyList(), Collections.emptyList(), topScore);
    }

    private double computeRecordScore(CollegeInfo record,
                                      String rawQuery,
                                      List<String> queryTokens,
                                      Set<String> queryTrigrams,
                                      String categoryFilter) {

        double subcatScore = 0.0;
        double questionScore = 0.0;
        double entityScore = 0.0;
        double categoryScore = 0.0;

        String rSub = record.getSubcategory() != null ? record.getSubcategory().toLowerCase() : "";
        String rQ = record.getQuestion() != null ? record.getQuestion().toLowerCase() : "";
        String rDept = record.getDepartment() != null ? record.getDepartment().toLowerCase() : "";
        String rCat = record.getCategory() != null ? record.getCategory().toLowerCase() : "";
        String rAns = record.getAnswer() != null ? record.getAnswer().toLowerCase() : "";

        // 1. Subcategory / Keyword hit (Weight: 35%)
        for (String qt : queryTokens) {
            String canon = synonymDict.getCanonical(qt);
            if (rSub.contains(qt) || (canon != null && rSub.contains(canon))) {
                subcatScore = 1.0;
                break;
            } else if (normalizer.fuzzyMatch(qt, rSub)) {
                subcatScore = Math.max(subcatScore, 0.7);
            }
        }

        // 2. Question-text similarity: token Jaccard + Trigram (Weight: 30%)
        List<String> qTokens = normalizer.tokenizeAndNormalize(rQ);
        double jaccard = computeJaccard(queryTokens, qTokens);
        Set<String> qTrigrams = makeTrigrams(normalizer.clean(rQ));
        double trigramSim = computeTrigramSimilarity(queryTrigrams, qTrigrams);
        questionScore = (jaccard * 0.5) + (trigramSim * 0.5);

        // Also if raw question is in answer
        if (rAns.contains(rawQuery.toLowerCase().trim())) {
            questionScore = Math.max(questionScore, 0.85);
        }

        // 3. Entity match (Department, Course, Facility) (Weight: 20%)
        for (String qt : queryTokens) {
            if (rDept.contains(qt)) {
                entityScore = 1.0;
                break;
            }
        }

        // 4. Category match (Weight: 15%)
        if (categoryFilter != null && !categoryFilter.isBlank()) {
            if (rCat.equalsIgnoreCase(categoryFilter.trim())) {
                categoryScore = 1.0;
            }
        } else {
            for (String qt : queryTokens) {
                if (rCat.contains(qt)) {
                    categoryScore = 0.8;
                    break;
                }
            }
        }

        double total = (subcatScore * 0.35) +
                       (questionScore * 0.30) +
                       (entityScore * 0.20) +
                       (categoryScore * 0.15);

        return Math.min(1.0, total);
    }

    private double computeJaccard(List<String> a, List<String> b) {
        if (a.isEmpty() || b.isEmpty()) return 0.0;
        Set<String> union = new HashSet<>(a);
        union.addAll(b);
        Set<String> intersect = new HashSet<>(a);
        intersect.retainAll(new HashSet<>(b));
        return (double) intersect.size() / union.size();
    }

    private Set<String> makeTrigrams(String s) {
        Set<String> tg = new HashSet<>();
        if (s == null || s.length() < 3) return tg;
        for (int i = 0; i <= s.length() - 3; i++) {
            tg.add(s.substring(i, i + 3));
        }
        return tg;
    }

    private double computeTrigramSimilarity(Set<String> a, Set<String> b) {
        if (a.isEmpty() || b.isEmpty()) return 0.0;
        Set<String> intersect = new HashSet<>(a);
        intersect.retainAll(b);
        return (2.0 * intersect.size()) / (a.size() + b.size());
    }

    public record SearchResult(List<CollegeInfo> records, List<String> suggestions, double confidence) {}
    private record ScoredRecord(CollegeInfo record, double score) {}
}
