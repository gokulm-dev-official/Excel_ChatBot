package com.excel.chatbot.service;

import com.excel.chatbot.model.FollowUpOption;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class FollowUpOptionsBuilder {

    private final GoogleSheetsService sheetsService;

    public FollowUpOptionsBuilder(GoogleSheetsService sheetsService) {
        this.sheetsService = sheetsService;
    }

    public List<FollowUpOption> buildOptions(String category, List<String> suggestions) {
        List<FollowUpOption> options = new ArrayList<>();

        if (suggestions != null && !suggestions.isEmpty()) {
            for (String s : suggestions) {
                options.add(new FollowUpOption(s, s, "suggestion"));
            }
            options.add(new FollowUpOption("Back to Menu", "menu", "action"));
            return options;
        }

        if (category == null || category.isBlank()) {
            for (String cat : List.of("Courses", "Admissions", "Departments", "Facilities", "Contact Information")) {
                options.add(new FollowUpOption(cat, cat, "category"));
            }
            return options;
        }

        switch (category) {
            case "Admissions" -> {
                // Top option for Admission Enquiry as required by Section 3
                options.add(new FollowUpOption("📝 Apply / Admission Enquiry", "apply", "apply"));
                List<String> subcats = sheetsService.getSubcategories("Admissions");
                for (String s : subcats) {
                    options.add(new FollowUpOption(s, s, "subcategory"));
                }
                options.add(new FollowUpOption("Back to Menu", "menu", "action"));
            }
            case "Courses" -> {
                List<String> subcats = sheetsService.getSubcategories("Courses");
                for (String s : subcats) {
                    options.add(new FollowUpOption(s, s, "subcategory"));
                }
                options.add(new FollowUpOption("Back to Menu", "menu", "action"));
            }
            case "Departments" -> {
                List<String> depts = sheetsService.getDepartments();
                for (String d : depts) {
                    options.add(new FollowUpOption(d, d, "department"));
                }
                options.add(new FollowUpOption("Back to Menu", "menu", "action"));
            }
            case "Facilities" -> {
                List<String> subcats = sheetsService.getSubcategories("Facilities");
                for (String s : subcats) {
                    options.add(new FollowUpOption(s, s, "subcategory"));
                }
                options.add(new FollowUpOption("Back to Menu", "menu", "action"));
            }
            case "Contact Information" -> {
                List<String> subcats = sheetsService.getSubcategories("Contact Information");
                for (String s : subcats) {
                    options.add(new FollowUpOption(s, s, "subcategory"));
                }
                options.add(new FollowUpOption("Back to Menu", "menu", "action"));
            }
            default -> {
                for (String cat : List.of("Courses", "Admissions", "Departments", "Facilities", "Contact Information")) {
                    options.add(new FollowUpOption(cat, cat, "category"));
                }
            }
        }

        return options;
    }
}

