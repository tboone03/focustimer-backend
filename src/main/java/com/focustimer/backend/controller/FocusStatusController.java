package com.focustimer.backend.controller;

import com.focustimer.backend.dto.TimerUpdateRequest;
import com.focustimer.backend.service.FocusStatusService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/focus")
@RequiredArgsConstructor
public class FocusStatusController {

    private final FocusStatusService focusStatusService;

    @PostMapping("/status")
    public ResponseEntity<Void> updateStatus(
            @AuthenticationPrincipal UserDetails principal,
            @Valid @RequestBody TimerUpdateRequest req) {
        focusStatusService.updateStatus(principal.getUsername(), req);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/privacy")
    public ResponseEntity<Void> updatePrivacy(
            @AuthenticationPrincipal UserDetails principal,
            @RequestParam boolean visible) {
        focusStatusService.updatePrivacySetting(principal.getUsername(), visible);
        return ResponseEntity.ok().build();
    }
}
