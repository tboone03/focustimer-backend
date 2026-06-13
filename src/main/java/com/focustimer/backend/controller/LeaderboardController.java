package com.focustimer.backend.controller;

import com.focustimer.backend.dto.LeaderboardEntryDTO;
import com.focustimer.backend.service.LeaderboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/leaderboard")
@RequiredArgsConstructor
public class LeaderboardController {

    private final LeaderboardService leaderboardService;

    @GetMapping("/global")
    public ResponseEntity<List<LeaderboardEntryDTO>> getGlobal() {
        return ResponseEntity.ok(leaderboardService.getGlobalLeaderboard());
    }

    @GetMapping("/friends")
    public ResponseEntity<List<LeaderboardEntryDTO>> getFriends(
            @AuthenticationPrincipal UserDetails principal) {
        return ResponseEntity.ok(leaderboardService.getFriendsLeaderboard(principal.getUsername()));
    }
}
