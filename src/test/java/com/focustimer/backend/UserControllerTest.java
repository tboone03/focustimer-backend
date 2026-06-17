package com.focustimer.backend;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.focustimer.backend.dto.RegisterRequest;
import com.focustimer.backend.dto.UpdateProfileRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class UserControllerTest {

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper mapper;

    private String tokenA;
    private String tokenB;

    @BeforeEach
    void setup() throws Exception {
        tokenA = register("userA", "a@test.com", "pass1234");
        tokenB = register("userB", "b@test.com", "pass1234");
    }

    private String register(String username, String email, String password) throws Exception {
        RegisterRequest req = new RegisterRequest();
        req.setUsername(username); req.setEmail(email); req.setPassword(password);
        MvcResult result = mvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andReturn();
        return mapper.readTree(result.getResponse().getContentAsString()).get("token").asText();
    }

    @Test
    void getProfile_returns_current_user() throws Exception {
        mvc.perform(get("/api/users/me").header("Authorization", "Bearer " + tokenA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("userA"))
                .andExpect(jsonPath("$.email").value("a@test.com"));
    }

    @Test
    void getProfile_without_token_returns_403() throws Exception {
        mvc.perform(get("/api/users/me"))
                .andExpect(status().isForbidden());
    }

    @Test
    void updateProfile_displayName_succeeds() throws Exception {
        UpdateProfileRequest req = new UpdateProfileRequest();
        req.setDisplayName("Alice");
        mvc.perform(put("/api/users/me")
                .header("Authorization", "Bearer " + tokenA)
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.displayName").value("Alice"));
    }

    @Test
    void updateProfile_email_to_same_value_succeeds() throws Exception {
        UpdateProfileRequest req = new UpdateProfileRequest();
        req.setEmail("a@test.com");
        mvc.perform(put("/api/users/me")
                .header("Authorization", "Bearer " + tokenA)
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("a@test.com"));
    }

    @Test
    void updateProfile_email_taken_by_other_returns_400() throws Exception {
        UpdateProfileRequest req = new UpdateProfileRequest();
        req.setEmail("b@test.com"); // taken by userB
        mvc.perform(put("/api/users/me")
                .header("Authorization", "Bearer " + tokenA)
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Email is already in use"));
    }

    @Test
    void updateProfile_new_email_succeeds() throws Exception {
        UpdateProfileRequest req = new UpdateProfileRequest();
        req.setEmail("new@test.com");
        mvc.perform(put("/api/users/me")
                .header("Authorization", "Bearer " + tokenA)
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("new@test.com"));
    }

    @Test
    void updateProfile_liveStatus_toggle() throws Exception {
        UpdateProfileRequest req = new UpdateProfileRequest();
        req.setLiveStatusVisible(false);
        mvc.perform(put("/api/users/me")
                .header("Authorization", "Bearer " + tokenA)
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.liveStatusVisible").value(false));
    }
}
