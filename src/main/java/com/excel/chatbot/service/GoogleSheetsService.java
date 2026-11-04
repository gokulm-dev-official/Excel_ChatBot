package com.excel.chatbot.service;

import com.excel.chatbot.config.GoogleSheetsConfig;
import com.excel.chatbot.model.CollegeInfo;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@Service
public class GoogleSheetsService {

    private static final Logger log = LoggerFactory.getLogger(GoogleSheetsService.class);

    private final GoogleSheetsConfig config;
    private final List<CollegeInfo> cachedData = new CopyOnWriteArrayList<>();
    private final AtomicLong lastFetchTime = new AtomicLong(0);
    private volatile boolean initialized = false;

    public GoogleSheetsService(GoogleSheetsConfig config) {
        this.config = config;
    }

    @PostConstruct
    public void init() {
        refreshData();
        if (cachedData.isEmpty()) {
            loadSeedData();
        }
    }

    @Scheduled(fixedDelayString = "${google.sheets.cache-seconds:60}000")
    public void scheduledRefresh() {
        refreshData();
    }

    public synchronized void refreshData() {
        try {
            String url = config.getPublishedCsvUrl();
            log.info("Attempting to fetch data from Google Sheets: {}", url);

            HttpURLConnection connection = (HttpURLConnection) URI.create(url).toURL().openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(8000);
            connection.setReadTimeout(8000);
            connection.setRequestProperty("User-Agent", "ExcelChatbot/1.0");

            int responseCode = connection.getResponseCode();
            if (responseCode != 200) {
                log.warn("Google Sheets returned HTTP {}. Keeping existing/seed cache.", responseCode);
                return;
            }

            List<CollegeInfo> newData = new ArrayList<>();
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8))) {

                String headerLine = reader.readLine();
                if (headerLine == null) {
                    log.warn("Empty response from Google Sheets");
                    return;
                }

                String[] headers = parseCsvLine(headerLine);
                Map<String, Integer> headerIndex = new HashMap<>();
                for (int i = 0; i < headers.length; i++) {
                    headerIndex.put(headers[i].trim().toLowerCase().replaceAll("\"", ""), i);
                }

                // Check required columns
                List<String> required = List.of("id", "category", "department", "subcategory", "question", "answer", "status");
                boolean hasAll = required.stream().allMatch(headerIndex::containsKey);
                if (!hasAll) {
                    log.warn("Google Sheets missing standard columns: {}. Header found: {}", required, headerIndex.keySet());
                }

                String line;
                while ((line = reader.readLine()) != null) {
                    if (line.trim().isEmpty()) continue;
                    try {
                        String[] values = parseCsvLine(line);
                        CollegeInfo info = new CollegeInfo();
                        info.setId(getField(values, headerIndex, "id"));
                        info.setCategory(getField(values, headerIndex, "category"));
                        info.setDepartment(getField(values, headerIndex, "department"));
                        info.setSubcategory(getField(values, headerIndex, "subcategory"));
                        info.setQuestion(getField(values, headerIndex, "question"));
                        info.setAnswer(getField(values, headerIndex, "answer"));
                        info.setStatus(getField(values, headerIndex, "status"));

                        if (info.isActive() && info.getCategory() != null && !info.getCategory().isBlank()) {
                            newData.add(info);
                        }
                    } catch (Exception e) {
                        log.debug("Skipping malformed row: {}", e.getMessage());
                    }
                }
            }

            if (!newData.isEmpty()) {
                cachedData.clear();
                cachedData.addAll(newData);
                lastFetchTime.set(System.currentTimeMillis());
                initialized = true;
                log.info("Successfully loaded {} active records from Google Sheets", newData.size());
            }

        } catch (Exception e) {
            log.info("Could not fetch remote Google Sheet ({}: {}).", e.getClass().getSimpleName(), e.getMessage());
            if (cachedData.isEmpty()) {
                loadSeedData();
            }
        }
    }

    private void loadSeedData() {
        log.info("Initializing verified baseline college records for Excel Engineering College (Autonomous)...");
        List<CollegeInfo> seeds = new ArrayList<>();

        // Contact Information
        seeds.add(new CollegeInfo("C1", "Contact Information", "Administration", "College Address",
                "What is the college address?",
                "Excel Engineering College (Autonomous), NH-544, Salem Main Road, Sankari West, Pallakkapalayam, Komarapalayam, Namakkal District, Tamil Nadu - 637303, India.",
                "Active"));
        seeds.add(new CollegeInfo("C2", "Contact Information", "Admission Office", "Phone Number",
                "What is the college phone number?",
                "+91 98427 13789 / +91 99655 23789",
                "Active"));
        seeds.add(new CollegeInfo("C3", "Contact Information", "Admission Office", "Email Address",
                "What is the official email address?",
                "admission@excelcolleges.com / info@excelengg.com",
                "Active"));
        seeds.add(new CollegeInfo("C4", "Contact Information", "General", "Official Website",
                "What is the official website?",
                "https://excelinstitutions.com",
                "Active"));
        seeds.add(new CollegeInfo("C5", "Contact Information", "Administrative Office", "Office Timings",
                "What are the office timings?",
                "Monday to Saturday: 9:00 AM to 5:00 PM (Closed on Sundays and Public Holidays)",
                "Active"));

        // Courses
        seeds.add(new CollegeInfo("CR1", "Courses", "Undergraduate", "Available Courses",
                "What are the undergraduate courses available?",
                "Excel Engineering College offers B.E. in CSE, ECE, EEE, Mechanical, Civil, Biomedical, Aeronautical, Agriculture, and B.Tech. in AI & DS, Information Technology, Food Technology, and Computer Science & Business Systems.",
                "Active"));
        seeds.add(new CollegeInfo("CR2", "Courses", "Postgraduate", "Available Courses",
                "What postgraduate courses are offered?",
                "Postgraduate programs include M.E. in Structural Engineering, Embedded Systems, Computer Science, Aeronautical, as well as MBA (Master of Business Administration) and MCA (Master of Computer Applications).",
                "Active"));
        seeds.add(new CollegeInfo("CR3", "Courses", "General", "Course Duration",
                "What is the duration of engineering courses?",
                "B.E. and B.Tech programs have a duration of 4 years (8 semesters). Lateral entry duration is 3 years (6 semesters). M.E. and MBA/MCA programs have a duration of 2 years (4 semesters).",
                "Active"));
        seeds.add(new CollegeInfo("CR4", "Courses", "Undergraduate", "Course Eligibility",
                "What is the eligibility for B.E. / B.Tech courses?",
                "Candidates must have passed 10+2 / HSC Examination with Physics, Chemistry, and Mathematics as mandatory subjects with minimum qualifying marks as prescribed by Anna University / DOTE norms.",
                "Active"));

        // Admissions
        seeds.add(new CollegeInfo("A1", "Admissions", "Admission Office", "Admission Eligibility",
                "What is admission eligibility criteria?",
                "Admission eligibility criteria: For B.E./B.Tech programs, candidates must have passed 10+2 (PCM) with minimum 45% for general category and 40% for reserved categories. For Lateral Entry, a Diploma in Engineering or B.Sc. with Mathematics is required.",
                "Active"));
        seeds.add(new CollegeInfo("A2", "Admissions", "Admission Office", "Application Procedure",
                "How do I apply for admission?",
                "Candidates can apply online through the official Excel Institutions admission portal (excelinstitutions.com) or visit the Admission Office directly on campus at Komarapalayam, Tamil Nadu.",
                "Active"));
        seeds.add(new CollegeInfo("A3", "Admissions", "Admission Office", "Required Documents",
                "What documents are required for admission?",
                "Required documents include: 10th & 12th Mark sheets, Transfer Certificate (TC), Community Certificate, Migration Certificate (for other state students), Conduct Certificate, and Passport size photographs.",
                "Active"));
        seeds.add(new CollegeInfo("A4", "Admissions", "Admission Office", "Admission Fees",
                "What is the admission fee structure?",
                "Tuition and hostel fees are structured as per government norms and institutional scholarships. Contact the Admission Cell at +91 98427 13789 for current academic year fee details and merit scholarship eligibility.",
                "Active"));
        seeds.add(new CollegeInfo("A5", "Admissions", "Admission Office", "Important Admission Dates",
                "What are the important admission dates?",
                "Admissions for the upcoming academic year are currently ongoing for Management and TNEA counselling quotas. Contact the admission office for quota deadlines.",
                "Active"));

        // Departments
        seeds.add(new CollegeInfo("D1", "Departments", "Computer Science and Engineering", "Overview",
                "Tell me about the Computer Science and Engineering department",
                "The Department of Computer Science & Engineering (CSE) offers modern laboratories, high-speed computing facilities, industry-aligned curriculum in Cloud, AI, and Full Stack development with excellent placement records.",
                "Active"));
        seeds.add(new CollegeInfo("D2", "Departments", "Artificial Intelligence & Data Science", "Overview",
                "Tell me about AI & DS department",
                "The Department of Artificial Intelligence and Data Science prepares students in Machine Learning, Deep Learning, Big Data Analytics, and Natural Language Processing with cutting-edge AI computing labs.",
                "Active"));
        seeds.add(new CollegeInfo("D3", "Departments", "Electronics and Communication Engineering", "Overview",
                "Tell me about ECE department",
                "The ECE Department features advanced laboratories in VLSI Design, Embedded Systems, IoT, Microwave and Optical Communication, and Robotics.",
                "Active"));
        seeds.add(new CollegeInfo("D4", "Departments", "Mechanical Engineering", "Overview",
                "Tell me about Mechanical Engineering",
                "The Department of Mechanical Engineering is equipped with modern CAD/CAM software, CNC machining centres, Mechatronics and Thermal Engineering laboratories, and strong industry partnerships.",
                "Active"));

        // Facilities
        seeds.add(new CollegeInfo("F1", "Facilities", "Hostel", "Hostel Facilities",
                "What hostel facilities are available?",
                "Separate secure hostel facilities are provided for boys and girls with 24/7 security, Wi-Fi connectivity, RO purified drinking water, hygienic mess serving South and North Indian cuisines, gym, and recreational halls.",
                "Active"));
        seeds.add(new CollegeInfo("F2", "Facilities", "Library", "Central Library",
                "What are the central library facilities?",
                "The Central Library houses over 50,000 volumes of books, international and national journals, digital e-library with IEEE/DELNET access, multimedia computers, and spacious air-conditioned reading halls.",
                "Active"));
        seeds.add(new CollegeInfo("F3", "Facilities", "Transportation", "Bus Routes",
                "What transportation facilities are provided?",
                "Excel Institutions operates a fleet of over 100 college buses covering Salem, Erode, Tiruchengode, Sankari, Bhavani, Namakkal, and surrounding districts for safe daily commuting of students and staff.",
                "Active"));
        seeds.add(new CollegeInfo("F4", "Facilities", "Laboratories", "Campus Labs",
                "What laboratory facilities are available?",
                "State-of-the-art laboratories for each department including AI research lab, IoT Centre of Excellence, Advanced Manufacturing Lab, Robotics Lab, and Language Communication Labs.",
                "Active"));
        seeds.add(new CollegeInfo("F5", "Facilities", "Sports", "Sports & Fitness",
                "What sports facilities are available?",
                "Expansive sports grounds for cricket, football, basketball courts, volleyball, indoor badminton courts, and fully equipped modern fitness centres.",
                "Active"));

        cachedData.clear();
        cachedData.addAll(seeds);
        lastFetchTime.set(System.currentTimeMillis());
        initialized = true;
    }

    private String getField(String[] values, Map<String, Integer> headerIndex, String field) {
        Integer idx = headerIndex.get(field);
        if (idx == null || idx >= values.length) return "";
        return values[idx].trim().replaceAll("^\"|\"$", "");
    }

    private String[] parseCsvLine(String line) {
        List<String> fields = new ArrayList<>();
        boolean inQuotes = false;
        StringBuilder field = new StringBuilder();

        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (c == '"') {
                if (inQuotes && i + 1 < line.length() && line.charAt(i + 1) == '"') {
                    field.append('"');
                    i++;
                } else {
                    inQuotes = !inQuotes;
                }
            } else if (c == ',' && !inQuotes) {
                fields.add(field.toString());
                field = new StringBuilder();
            } else {
                field.append(c);
            }
        }
        fields.add(field.toString());
        return fields.toArray(new String[0]);
    }

    public List<CollegeInfo> getAllData() {
        return Collections.unmodifiableList(cachedData);
    }

    public List<CollegeInfo> getByCategory(String category) {
        return cachedData.stream()
                .filter(info -> info.getCategory().equalsIgnoreCase(category.trim()))
                .collect(Collectors.toList());
    }

    public List<String> getCategories() {
        return cachedData.stream()
                .map(CollegeInfo::getCategory)
                .distinct()
                .sorted()
                .collect(Collectors.toList());
    }

    public List<String> getDepartments() {
        return cachedData.stream()
                .map(CollegeInfo::getDepartment)
                .filter(d -> d != null && !d.isBlank())
                .distinct()
                .sorted()
                .collect(Collectors.toList());
    }

    public List<String> getCourses() {
        return cachedData.stream()
                .filter(info -> "Courses".equalsIgnoreCase(info.getCategory()))
                .map(CollegeInfo::getDepartment)
                .filter(d -> d != null && !d.isBlank())
                .distinct()
                .sorted()
                .collect(Collectors.toList());
    }

    public List<String> getSubcategories(String category) {
        return cachedData.stream()
                .filter(info -> info.getCategory().equalsIgnoreCase(category))
                .map(CollegeInfo::getSubcategory)
                .filter(s -> s != null && !s.isBlank())
                .distinct()
                .sorted()
                .collect(Collectors.toList());
    }

    public List<CollegeInfo> search(String category, String query) {
        if (query == null || query.isBlank()) {
            if (category != null && !category.isBlank()) {
                return getByCategory(category);
            }
            return getAllData();
        }

        String lowerQuery = query.toLowerCase().trim();
        Set<String> stopWords = Set.of("who", "what", "where", "when", "why", "how", "the", "and", "for", "with", "won", "cup", "any", "are", "you");
        String[] tokens = Arrays.stream(lowerQuery.split("[\\s,?.!]+"))
                .filter(t -> t.length() > 2 && !stopWords.contains(t))
                .toArray(String[]::new);

        return cachedData.stream()
                .filter(info -> category == null || category.isBlank() || info.getCategory().equalsIgnoreCase(category.trim()))
                .filter(info -> {
                    String q = (info.getQuestion() != null ? info.getQuestion() : "").toLowerCase();
                    String a = (info.getAnswer() != null ? info.getAnswer() : "").toLowerCase();
                    String sub = (info.getSubcategory() != null ? info.getSubcategory() : "").toLowerCase();
                    String dept = (info.getDepartment() != null ? info.getDepartment() : "").toLowerCase();
                    String cat = (info.getCategory() != null ? info.getCategory() : "").toLowerCase();

                    // Direct contains
                    if (q.contains(lowerQuery) || a.contains(lowerQuery) || sub.contains(lowerQuery) ||
                        dept.contains(lowerQuery) || lowerQuery.contains(sub) || lowerQuery.contains(dept)) {
                        return true;
                    }

                    // Token match
                    for (String token : tokens) {
                        if (q.contains(token) || sub.contains(token) || dept.contains(token) || cat.contains(token)) {
                            return true;
                        }
                    }

                    // If searching within a specific category and asking generally
                    if (category != null && !category.isBlank() && tokens.length > 0) {
                        for (String token : tokens) {
                            if (a.contains(token)) return true;
                        }
                    }

                    return false;
                })
                .collect(Collectors.toList());
    }

    public boolean isAvailable() {
        return initialized && !cachedData.isEmpty();
    }

    public long getLastFetchTime() {
        return lastFetchTime.get();
    }

    public int getRecordCount() {
        return cachedData.size();
    }
}
