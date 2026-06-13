package com.focustimer.backend.controller;

import com.focustimer.backend.dto.FriendStatusDTO;
import com.focustimer.backend.dto.PendingRequestDTO;
import com.focustimer.backend.service.FriendshipService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/friends")
@RequiredArgsConstructor
public class FriendController {

    private final FriendshipService friendshipService;

    @PostMapping("/request/{addresseeId}")
    public ResponseEntity<Void> sendRequest(
            @AuthenticationPrincipal UserDetails principal,
            @PathVariable Long addresseeId) {
        friendshipService.sendRequest(principal.getUsername(), addresseeId);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/request/{friendshipId}")
    public ResponseEntity<Void> respondToRequest(
            @AuthenticationPrincipal UserDetails principal,
            @PathVariable Long friendshipId,
            @RequestParam boolean accept) {
        friendshipService.respondToRequest(principal.getUsername(), friendshipId, accept);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/requests/pending")
    public ResponseEntity<List<PendingRequestDTO>> getPendingRequests(
            @AuthenticationPrincipal UserDetails principal) {
        return ResponseEntity.ok(friendshipService.getPendingRequests(principal.getUsername()));
    }

    @GetMapping("/status")
    public ResponseEntity<List<FriendStatusDTO>> getFriendsStatus(
            @AuthenticationPrincipal UserDetails principal) {
        return ResponseEntity.ok(friendshipService.getFriendsWithStatus(principal.getUsername()));
    }
}
