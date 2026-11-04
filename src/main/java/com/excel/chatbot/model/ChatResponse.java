package com.excel.chatbot.model;

import java.util.ArrayList;
import java.util.List;

public class ChatResponse {
    private String answer;
    private String category;
    private String intent;
    private boolean verified;
    private double confidence;
    private String source = "college-records";
    private List<String> recordIds = new ArrayList<>();
    private List<FollowUpOption> followUpOptions = new ArrayList<>();
    private List<ContactInfo> contacts = new ArrayList<>();
    private List<String> suggestions = new ArrayList<>();
    private boolean startAdmissionForm;

    public ChatResponse() {}

    public String getAnswer() { return answer; }
    public void setAnswer(String answer) { this.answer = answer; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getIntent() { return intent; }
    public void setIntent(String intent) { this.intent = intent; }

    public boolean isVerified() { return verified; }
    public void setVerified(boolean verified) { this.verified = verified; }

    public double getConfidence() { return confidence; }
    public void setConfidence(double confidence) { this.confidence = confidence; }

    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }

    public List<String> getRecordIds() { return recordIds; }
    public void setRecordIds(List<String> recordIds) { this.recordIds = recordIds; }

    public List<FollowUpOption> getFollowUpOptions() { return followUpOptions; }
    public void setFollowUpOptions(List<FollowUpOption> followUpOptions) { this.followUpOptions = followUpOptions; }

    public List<ContactInfo> getContacts() { return contacts; }
    public void setContacts(List<ContactInfo> contacts) { this.contacts = contacts; }

    public List<String> getSuggestions() { return suggestions; }
    public void setSuggestions(List<String> suggestions) { this.suggestions = suggestions; }

    public boolean isStartAdmissionForm() { return startAdmissionForm; }
    public void setStartAdmissionForm(boolean startAdmissionForm) { this.startAdmissionForm = startAdmissionForm; }
}
