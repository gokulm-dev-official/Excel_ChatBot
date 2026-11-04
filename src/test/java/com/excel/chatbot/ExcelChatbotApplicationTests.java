package com.excel.chatbot;

import com.excel.chatbot.model.ChatRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class ExcelChatbotApplicationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("Context loads and categories are retrieved")
    void testGetCategories() throws Exception {
        mockMvc.perform(get("/api/college/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.categories", hasItems("Courses", "Admissions", "Departments", "Facilities", "Contact Information")));
    }

    @Test
    @DisplayName("Departments endpoint returns non-empty list")
    void testGetDepartments() throws Exception {
        mockMvc.perform(get("/api/college/departments"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", not(empty())));
    }

    @Test
    @DisplayName("Chat handles polite greeting")
    void testChatGreeting() throws Exception {
        ChatRequest req = new ChatRequest("Hello", null, null);
        mockMvc.perform(post("/api/chat/message")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.answer", containsStringIgnoringCase("Welcome to Excel Engineering College")));
    }

    @Test
    @DisplayName("Chat answers courses inquiry with verified information")
    void testChatCourses() throws Exception {
        ChatRequest req = new ChatRequest("What engineering courses are available?", "Courses", null);
        mockMvc.perform(post("/api/chat/message")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.verified", is(true)))
                .andExpect(jsonPath("$.answer", notNullValue()));
    }

    @Test
    @DisplayName("Chat answers admission eligibility inquiry with verified information")
    void testChatAdmissions() throws Exception {
        ChatRequest req = new ChatRequest("What is admission eligibility?", "Admissions", null);
        mockMvc.perform(post("/api/chat/message")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.verified", is(true)))
                .andExpect(jsonPath("$.answer", containsStringIgnoringCase("eligibility")));
    }

    @Test
    @DisplayName("Chat answers hostel inquiry with verified information")
    void testChatFacilities() throws Exception {
        ChatRequest req = new ChatRequest("What hostel facilities are available?", "Facilities", null);
        mockMvc.perform(post("/api/chat/message")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.verified", is(true)))
                .andExpect(jsonPath("$.answer", containsStringIgnoringCase("hostel")));
    }

    @Test
    @DisplayName("Chat answers official contact inquiry with verified information")
    void testChatContact() throws Exception {
        ChatRequest req = new ChatRequest("What is the college phone number?", "Contact Information", null);
        mockMvc.perform(post("/api/chat/message")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.verified", is(true)))
                .andExpect(jsonPath("$.answer", containsString("98427")));
    }

    @Test
    @DisplayName("Chat handles unrelated query politely")
    void testChatUnrelated() throws Exception {
        ChatRequest req = new ChatRequest("What is the weather on Mars?", null, null);
        mockMvc.perform(post("/api/chat/message")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.verified", is(false)))
                .andExpect(jsonPath("$.answer", containsString("Excel Engineering College")));
    }
}

