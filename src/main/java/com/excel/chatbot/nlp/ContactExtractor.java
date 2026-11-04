package com.excel.chatbot.nlp;

import com.excel.chatbot.model.ContactInfo;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class ContactExtractor {

    private static final Pattern PHONE_PATTERN = Pattern.compile("(?:\\+91[\\s-]?)?[6-9]\\d{4}[\\s-]?\\d{5}");
    private static final Pattern EMAIL_PATTERN = Pattern.compile("[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}");
    private static final Pattern URL_PATTERN = Pattern.compile("https?://[A-Za-z0-9.-]+(?:/[A-Za-z0-9._%+-]*)*");

    public List<ContactInfo> extractContacts(String text) {
        List<ContactInfo> list = new ArrayList<>();
        if (text == null || text.isBlank()) return list;

        // Phones
        Matcher pm = PHONE_PATTERN.matcher(text);
        while (pm.find()) {
            String val = pm.group().trim();
            list.add(new ContactInfo("phone", "Call College", val));
        }

        // Emails
        Matcher em = EMAIL_PATTERN.matcher(text);
        while (em.find()) {
            String val = em.group().trim();
            list.add(new ContactInfo("email", "Email Admission", val));
        }

        // URLs
        Matcher um = URL_PATTERN.matcher(text);
        while (um.find()) {
            String val = um.group().trim();
            list.add(new ContactInfo("website", "Official Portal", val));
        }

        return list;
    }
}

