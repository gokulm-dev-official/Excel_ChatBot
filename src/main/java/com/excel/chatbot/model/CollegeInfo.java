package com.excel.chatbot.model;

public class CollegeInfo {
    private String id;
    private String category;
    private String department;
    private String subcategory;
    private String question;
    private String answer;
    private String status;

    public CollegeInfo() {}

    public CollegeInfo(String id, String category, String department, String subcategory, String question, String answer, String status) {
        this.id = id;
        this.category = category;
        this.department = department;
        this.subcategory = subcategory;
        this.question = question;
        this.answer = answer;
        this.status = status;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }
    public String getSubcategory() { return subcategory; }
    public void setSubcategory(String subcategory) { this.subcategory = subcategory; }
    public String getQuestion() { return question; }
    public void setQuestion(String question) { this.question = question; }
    public String getAnswer() { return answer; }
    public void setAnswer(String answer) { this.answer = answer; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public boolean isActive() {
        return "Active".equalsIgnoreCase(status);
    }

    @Override
    public String toString() {
        return "CollegeInfo{category='" + category + "', department='" + department + "', subcategory='" + subcategory + "', question='" + question + "'}";
    }
}

