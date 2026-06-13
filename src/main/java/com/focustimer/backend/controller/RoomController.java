package com.focustimer.backend.controller;

import com.focustimer.backend.dto.CreateRoomRequest;
import com.focustimer.backend.dto.RoomDTO;
import com.focustimer.backend.service.RoomService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/rooms")
@RequiredArgsConstructor
public class RoomController {

    private final RoomService roomService;

    @GetMapping
    public ResponseEntity<List<RoomDTO>> getRooms(
            @AuthenticationPrincipal UserDetails principal) {
        return ResponseEntity.ok(roomService.getVisibleRooms(principal.getUsername()));
    }

    @PostMapping
    public ResponseEntity<RoomDTO> createRoom(
            @AuthenticationPrincipal UserDetails principal,
            @RequestBody CreateRoomRequest request) {
        return ResponseEntity.ok(roomService.createRoom(principal.getUsername(), request));
    }

    @PostMapping("/join/{code}")
    public ResponseEntity<RoomDTO> joinRoom(
            @AuthenticationPrincipal UserDetails principal,
            @PathVariable String code) {
        return ResponseEntity.ok(roomService.joinByCode(principal.getUsername(), code));
    }

    @DeleteMapping("/{id}/leave")
    public ResponseEntity<Void> leaveRoom(
            @AuthenticationPrincipal UserDetails principal,
            @PathVariable Long id) {
        roomService.leaveRoom(principal.getUsername(), id);
        return ResponseEntity.ok().build();
    }
}
