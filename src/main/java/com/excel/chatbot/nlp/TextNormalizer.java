package com.excel.chatbot.nlp;

import org.springframework.stereotype.Component;

import java.util.*;
import java.util.regex.Pattern;

@Component
public class TextNormalizer {

    private static final Pattern EMOJI_AND_PUNCT = Pattern.compile("[\\p{Punct}\\p{So}\\p{Cn}]");
    private static final Pattern MULTI_SPACE = Pattern.compile("\\s+");

    private static final Set<String> STOP_WORDS = new HashSet<>(Arrays.asList(
            "a", "an", "the", "and", "or", "but", "if", "then", "else", "when", "at",
            "from", "by", "for", "with", "about", "against", "between", "into", "through",
            "during", "before", "after", "above", "below", "to", "of", "up", "down", "in",
            "out", "on", "off", "over", "under", "again", "further", "once",
            "here", "there", "where", "why", "how", "all", "any", "both", "each",
            "few", "more", "most", "other", "some", "such", "no", "nor", "not", "only",
            "own", "same", "so", "than", "too", "very", "s", "t", "can", "will", "just",
            "don", "should", "now", "d", "ll", "m", "o", "re", "ve", "y", "ain", "aren",
            "couldn", "didn", "doesn", "hadn", "hasn", "haven", "isn", "ma", "mightn",
            "mustn", "needn", "shan", "shouldn", "wasn", "weren", "won", "wouldn", "is",
            "are", "was", "were", "be", "been", "being", "have", "has", "had", "having",
            "do", "does", "did", "doing", "i", "me", "my", "myself", "we", "our", "ours",
            "ourselves", "you", "your", "yours", "yourself", "yourselves", "he", "him",
            "his", "himself", "she", "her", "hers", "herself", "it", "its", "itself",
            "they", "them", "their", "theirs", "themselves", "what", "which", "who", "whom"
    ));

    public String clean(String text) {
        if (text == null) return "";
        String s = text.toLowerCase().trim();
        s = EMOJI_AND_PUNCT.matcher(s).replaceAll(" ");
        s = MULTI_SPACE.matcher(s).replaceAll(" ").trim();
        return s;
    }

    public List<String> tokenizeAndNormalize(String text) {
        String cleaned = clean(text);
        if (cleaned.isBlank()) return Collections.emptyList();

        String[] raw = cleaned.split("\\s+");
        List<String> tokens = new ArrayList<>();
        for (String w : raw) {
            if (w.length() < 2 || STOP_WORDS.contains(w)) continue;
            tokens.add(stem(w));
        }
        return tokens;
    }

    public String stem(String word) {
        if (word == null || word.length() < 4) return word;
        String w = word.toLowerCase();
        if (w.endsWith("ies") && w.length() > 4) return w.substring(0, w.length() - 3) + "y";
        if (w.endsWith("ing") && w.length() > 5) return w.substring(0, w.length() - 3);
        if (w.endsWith("ed") && w.length() > 4) return w.substring(0, w.length() - 2);
        if (w.endsWith("es") && w.length() > 4) return w.substring(0, w.length() - 2);
        if (w.endsWith("s") && !w.endsWith("ss") && w.length() > 3) return w.substring(0, w.length() - 1);
        return w;
    }

    public int levenshtein(String s1, String s2) {
        if (s1 == null || s2 == null) return Integer.MAX_VALUE;
        int[][] dp = new int[s1.length() + 1][s2.length() + 1];
        for (int i = 0; i <= s1.length(); i++) dp[i][0] = i;
        for (int j = 0; j <= s2.length(); j++) dp[0][j] = j;

        for (int i = 1; i <= s1.length(); i++) {
            for (int j = 1; j <= s2.length(); j++) {
                int cost = (s1.charAt(i - 1) == s2.charAt(j - 1)) ? 0 : 1;
                dp[i][j] = Math.min(
                        Math.min(dp[i - 1][j] + 1, dp[i][j - 1] + 1),
                        dp[i - 1][j - 1] + cost
                );
            }
        }
        return dp[s1.length()][s2.length()];
    }

    public boolean isFuzzyMatch(String word1, String word2) {
        if (word1 == null || word2 == null) return false;
        if (word1.equalsIgnoreCase(word2)) return true;
        if (word1.length() >= 5 && word2.length() >= 5) {
            return levenshtein(word1.toLowerCase(), word2.toLowerCase()) <= 2;
        }
        return false;
    }

    public boolean fuzzyMatch(String word1, String word2) {
        return isFuzzyMatch(word1, word2);
    }
}
