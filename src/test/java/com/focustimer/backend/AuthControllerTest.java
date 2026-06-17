package com.focustimer.backend;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.focustimer.backend.dto.AuthRequest;
import com.focustimer.backend.dto.RegisterRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class AuthControllerTest {

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper mapper;

    private RegisterRequest reg(String user, String email, String pass) {
        RegisterRequest r = new RegisterRequest();
        r.setUsername(user); r.setEmail(email); r.setPassword(pass);
        return r;
    }

    @Test
    void register_success_returns_token() throws Exception {
        mvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(reg("alice", "alice@test.com", "secret123"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.username").value("alice"));
    }

    @Test
    void register_duplicate_username_returns_400() throws Exception {
        String body = mapper.writeValueAsString(reg("bob", "bob@test.com", "secret123"));
        mvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isOk());
        mvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(reg("bob", "bob2@test.com", "secret123"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Username already taken"));
    }

    @Test
    void register_duplicate_email_returns_400() throws Exception {
        mvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(reg("carol", "shared@test.com", "secret123"))))
                .andExpect(status().isOk());
        mvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(reg("dave", "shared@test.com", "secret123"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Email already registered"));
    }

    @Test
    void register_short_password_returns_400() throws Exception {
        mvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(reg("eve", "eve@test.com", "abc"))))
                .andExpect(status().isBadRequest());
    }

    @Test
    void register_invalid_email_returns_400() throws Exception {
        mvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(reg("frank", "not-an-email", "secret123"))))
                .andExpect(status().isBadRequest());
    }

    @Test
    void login_success_returns_token() throws Exception {
        mvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(reg("grace", "grace@test.com", "pass1234"))))
                .andExpect(status().isOk());

        AuthRequest login = new AuthRequest();
        login.setUsername("grace"); login.setPassword("pass1234");
        mvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(login)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.username").value("grace"));
    }

    @Test
    void login_wrong_password_returns_401() throws Exception {
        mvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(reg("henry", "henry@test.com", "correct"))))
                .andExpect(status().isOk());

        AuthRequest login = new AuthRequest();
        login.setUsername("henry"); login.setPassword("wrong");
        mvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(login)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void login_unknown_user_returns_401() throws Exception {
        AuthRequest login = new AuthRequest();
        login.setUsername("nobody"); login.setPassword("pass");
        mvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(login)))
                .andExpect(status().isUnauthorized());
    }
}
