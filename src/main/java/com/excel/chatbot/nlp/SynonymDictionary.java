package com.excel.chatbot.nlp;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.util.*;

@Component
public class SynonymDictionary {

    private static final Logger log = LoggerFactory.getLogger(SynonymDictionary.class);
    private final Map<String, Set<String>> canonicalToSynonyms = new HashMap<>();
    private final Map<String, String> synonymToCanonical = new HashMap<>();

    @PostConstruct
    public void init() {
        try {
            ClassPathResource res = new ClassPathResource("nlp/synonyms.json");
            if (res.exists()) {
                try (InputStream is = res.getInputStream()) {
                    ObjectMapper mapper = new ObjectMapper();
                    Map<String, List<String>> map = mapper.readValue(is, new TypeReference<>() {});
                    for (Map.Entry<String, List<String>> e : map.entrySet()) {
                        String canonical = e.getKey().toLowerCase().trim();
                        canonicalToSynonyms.putIfAbsent(canonical, new HashSet<>());
                        canonicalToSynonyms.get(canonical).add(canonical);
                        synonymToCanonical.put(canonical, canonical);

                        for (String syn : e.getValue()) {
                            String s = syn.toLowerCase().trim();
                            canonicalToSynonyms.get(canonical).add(s);
                            synonymToCanonical.put(s, canonical);
                        }
                    }
                }
                log.info("Loaded SynonymDictionary with {} canonical terms", canonicalToSynonyms.size());
            }
        } catch (Exception e) {
            log.warn("Could not load nlp/synonyms.json: {}", e.getMessage());
        }
    }

    public String getCanonical(String term) {
        if (term == null) return null;
        return synonymToCanonical.get(term.toLowerCase().trim());
    }

    public Set<String> getSynonyms(String canonical) {
        return canonicalToSynonyms.getOrDefault(canonical.toLowerCase().trim(), Collections.emptySet());
    }

    public boolean matches(String userWord, String canonical) {
        if (userWord == null || canonical == null) return false;
        String u = userWord.toLowerCase().trim();
        String c = canonical.toLowerCase().trim();
        if (u.equals(c)) return true;
        Set<String> syns = canonicalToSynonyms.get(c);
        return syns != null && syns.contains(u);
    }
}

