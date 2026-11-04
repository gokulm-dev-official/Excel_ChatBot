package com.excel.chatbot.service;

import com.excel.chatbot.model.CollegeInfo;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ResponseComposer {

    public static final String EXACT_FALLBACK =
            "Sorry, I couldn't find verified information about this in our college records. Please contact the college office for assistance.";

    public String composeGreeting() {
        return "Hello! Welcome to Excel Engineering College (Autonomous). I'm your college assistant. How can I help you today? Ask me about courses, admissions, departments, facilities, or contact information.";
    }

    public String composeThanks() {
        return "You're very welcome! If you have any more questions about Excel Engineering College, feel free to ask. I'm here to help!";
    }

    public String composeGoodbye() {
        return "Thank you for visiting Excel Engineering College! Have a wonderful day ahead. Feel free to return anytime you need assistance.";
    }

    public String composeHelpMenu() {
        return "I can assist you with official information from our autonomous college records. Please choose a category below or type your question directly:";
    }

    public String composeOutOfScope() {
        return "I can only assist with verified information related to Excel Engineering College across 5 areas: Courses, Admissions, Departments, Facilities, and Contact Information. Please choose one of the topics below or type your question.";
    }

    public String composeFallback() {
        return EXACT_FALLBACK;
    }

    public String composeSuggestions(List<String> suggestions) {
        if (suggestions == null || suggestions.isEmpty()) {
            return EXACT_FALLBACK;
        }
        return "I found a few related topics in our college records. Did you mean one of these?";
    }

    public String composeAnswer(List<CollegeInfo> records, String category) {
        if (records == null || records.isEmpty()) {
            return EXACT_FALLBACK;
        }

        if (records.size() == 1) {
            CollegeInfo r = records.get(0);
            return r.getAnswer();
        }

        // Multiple matching records - tidy bulleted list
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < records.size(); i++) {
            CollegeInfo r = records.get(i);
            if (i > 0) sb.append("\n\n");
            if (r.getSubcategory() != null && !r.getSubcategory().isBlank()) {
                sb.append("• **").append(r.getSubcategory()).append("**: ");
            } else if (r.getDepartment() != null && !r.getDepartment().isBlank()) {
                sb.append("• **").append(r.getDepartment()).append("**: ");
            } else {
                sb.append("• ");
            }
            sb.append(r.getAnswer());
        }
        return sb.toString();
    }
}

