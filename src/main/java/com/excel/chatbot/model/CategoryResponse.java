package com.excel.chatbot.model;

import java.util.List;
import java.util.Map;

public class CategoryResponse {
    private List<String> categories;
    private Map<String, List<String>> subcategories;
    private Map<String, List<String>> departments;

    public CategoryResponse() {}

    public List<String> getCategories() { return categories; }
    public void setCategories(List<String> categories) { this.categories = categories; }

    public Map<String, List<String>> getSubcategories() { return subcategories; }
    public void setSubcategories(Map<String, List<String>> subcategories) { this.subcategories = subcategories; }

    public Map<String, List<String>> getDepartments() { return departments; }
    public void setDepartments(Map<String, List<String>> departments) { this.departments = departments; }
}

