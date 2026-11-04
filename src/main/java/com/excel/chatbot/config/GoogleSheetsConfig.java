package com.excel.chatbot.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GoogleSheetsConfig {

    @Value("${google.sheets.id:1lYbmg-7LYewpJhxTVqyYZSToFS7XjUyLDdwIQkJn5zA}")
    private String spreadsheetId;

    @Value("${google.sheets.tab:Sheet1}")
    private String tabName;

    @Value("${google.sheets.read-url:}")
    private String readUrl;

    @Value("${google.sheets.cache-seconds:60}")
    private int cacheSeconds;

    @Value("${google.apps-script.url:}")
    private String appsScriptUrl;

    @Value("${google.apps-script.token:}")
    private String appsScriptToken;

    public String getSpreadsheetId() { return spreadsheetId; }
    public String getTabName() { return tabName; }
    public String getReadUrl() { return readUrl; }
    public int getCacheSeconds() { return cacheSeconds; }
    public String getAppsScriptUrl() { return appsScriptUrl; }
    public String getAppsScriptToken() { return appsScriptToken; }

    public String getPublishedCsvUrl() {
        if (readUrl != null && !readUrl.isBlank()) {
            return readUrl;
        }
        return "https://docs.google.com/spreadsheets/d/" + spreadsheetId + "/gviz/tq?tqx=out:csv&sheet=" + tabName;
    }
}

