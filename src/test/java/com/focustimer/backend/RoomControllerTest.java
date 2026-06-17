package com.focustimer.backend;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.focustimer.backend.dto.CreateRoomRequest;
import com.focustimer.backend.dto.RegisterRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class RoomControllerTest {

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper mapper;

    private String tokenHost;
    private String tokenGuest;

    @BeforeEach
    void setup() throws Exception {
        tokenHost  = register("host",  "host@test.com",  "pass1234");
        tokenGuest = register("guest", "guest@test.com", "pass1234");
    }

    private String register(String username, String email, String password) throws Exception {
        RegisterRequest req = new RegisterRequest();
        req.setUsername(username); req.setEmail(email); req.setPassword(password);
        MvcResult r = mvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(req)))
                .andExpect(status().isOk()).andReturn();
        return mapper.readTree(r.getResponse().getContentAsString()).get("token").asText();
    }

    private JsonNode createRoom(String token, String name, boolean isPublic) throws Exception {
        CreateRoomRequest req = new CreateRoomRequest();
        req.setName(name); req.setPublic(isPublic);
        MvcResult r = mvc.perform(post("/api/rooms")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(req)))
                .andExpect(status().isOk()).andReturn();
        return mapper.readTree(r.getResponse().getContentAsString());
    }

    @Test
    void create_room_returns_code_and_host() throws Exception {
        JsonNode room = createRoom(tokenHost, "Study Hall", true);
        assert room.get("code").asText().length() == 6;
        assert room.get("hostUsername").asText().equals("host");
        assert room.get("name").asText().equals("Study Hall");
    }

    @Test
    void create_room_blank_name_returns_400() throws Exception {
        CreateRoomRequest req = new CreateRoomRequest();
        req.setName("   "); req.setPublic(true);
        mvc.perform(post("/api/rooms")
                .header("Authorization", "Bearer " + tokenHost)
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void list_rooms_includes_public_room() throws Exception {
        createRoom(tokenHost, "Open Room", true);
        mvc.perform(get("/api/rooms").header("Authorization", "Bearer " + tokenGuest))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].name", hasItem("Open Room")));
    }

    @Test
    void join_by_code_adds_member() throws Exception {
        JsonNode room = createRoom(tokenHost, "Code Room", true);
        String code = room.get("code").asText();

        mvc.perform(post("/api/rooms/join/" + code)
                .header("Authorization", "Bearer " + tokenGuest))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.memberCount").value(2));
    }

    @Test
    void join_invalid_code_returns_400() throws Exception {
        mvc.perform(post("/api/rooms/join/ZZZZZZ")
                .header("Authorization", "Bearer " + tokenGuest))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Room not found"));
    }

    @Test
    void join_twice_is_idempotent() throws Exception {
        JsonNode room = createRoom(tokenHost, "Idempotent Room", true);
        String code = room.get("code").asText();
        mvc.perform(post("/api/rooms/join/" + code)
                .header("Authorization", "Bearer " + tokenGuest))
                .andExpect(status().isOk());
        mvc.perform(post("/api/rooms/join/" + code)
                .header("Authorization", "Bearer " + tokenGuest))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.memberCount").value(2));
    }

    @Test
    void leave_room_removes_member() throws Exception {
        JsonNode room = createRoom(tokenHost, "Leave Room", true);
        String code = room.get("code").asText();
        long roomId = room.get("id").asLong();

        mvc.perform(post("/api/rooms/join/" + code)
                .header("Authorization", "Bearer " + tokenGuest))
                .andExpect(status().isOk());

        mvc.perform(delete("/api/rooms/" + roomId + "/leave")
                .header("Authorization", "Bearer " + tokenGuest))
                .andExpect(status().isOk());

        mvc.perform(get("/api/rooms").header("Authorization", "Bearer " + tokenGuest))
                .andExpect(jsonPath("$[?(@.id == " + roomId + ")].memberCount", hasItem(1)));
    }

    @Test
    void host_leaving_last_deactivates_room() throws Exception {
        JsonNode room = createRoom(tokenHost, "Solo Room", true);
        long roomId = room.get("id").asLong();

        mvc.perform(delete("/api/rooms/" + roomId + "/leave")
                .header("Authorization", "Bearer " + tokenHost))
                .andExpect(status().isOk());

        mvc.perform(get("/api/rooms").header("Authorization", "Bearer " + tokenHost))
                .andExpect(jsonPath("$[*].id", not(hasItem((int) roomId))));
    }

    @Test
    void unauthenticated_request_returns_403() throws Exception {
        mvc.perform(get("/api/rooms"))
                .andExpect(status().isForbidden());
    }
}
