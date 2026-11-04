package com.excel.chatbot.nlp;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;

@Component
public class IntentDetector {

    private final TextNormalizer normalizer;

    private static final Set<String> GREETINGS = Set.of(
            "hi", "hello", "hey", "good morning", "good afternoon", "good evening",
            "greetings", "hii", "hiii", "namaste", "vanakkam", "howdy", "hola"
    );

    private static final Set<String> THANKS = Set.of(
            "thank", "thanks", "thank you", "thankyou", "thx", "appreciated", "grateful", "nandri"
    );

    private static final Set<String> GOODBYES = Set.of(
            "bye", "goodbye", "see you", "cya", "take care", "have a good day", "exit", "quit"
    );

    private static final Set<String> HELP_MENU = Set.of(
            "menu", "main menu", "help", "options", "start over", "home", "restart", "categories"
    );

    private static final Set<String> APPLY_PATTERNS = Set.of(
            "apply", "admission enquiry", "admission form", "application form", "apply online",
            "how to apply", "want to join", "join excel", "admissions enquiry", "register",
            "registration", "enquire admission", "check eligibility", "apply for admission",
            "want to enroll", "seat booking", "admission registration"
    );

    public IntentDetector(TextNormalizer normalizer) {
        this.normalizer = normalizer;
    }

    public String detectIntent(String rawMessage) {
        if (rawMessage == null || rawMessage.isBlank()) return "out-of-scope";

        String clean = normalizer.clean(rawMessage);

        // Check Greetings
        for (String g : GREETINGS) {
            if (clean.equals(g) || clean.startsWith(g + " ") || clean.endsWith(" " + g)) {
                return "greeting";
            }
        }

        // Check Thanks
        for (String t : THANKS) {
            if (clean.contains(t)) {
                return "thanks";
            }
        }

        // Check Goodbyes
        for (String gb : GOODBYES) {
            if (clean.equals(gb) || clean.startsWith(gb + " ") || clean.endsWith(" " + gb)) {
                return "goodbye";
            }
        }

        // Check Help/Menu
        for (String m : HELP_MENU) {
            if (clean.equals(m) || clean.startsWith(m + " ")) {
                return "help-menu";
            }
        }

        // Check Apply for admission
        for (String p : APPLY_PATTERNS) {
            if (clean.contains(p)) {
                return "apply-for-admission";
            }
        }

        return "category-question";
    }
}

