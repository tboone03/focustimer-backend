package com.focustimer.backend.controller;

import com.focustimer.backend.dto.UpdateProfileRequest;
import com.focustimer.backend.dto.UserProfileResponse;
import com.focustimer.backend.entity.User;
import com.focustimer.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserRepository userRepository;

    @GetMapping("/me")
    public UserProfileResponse getProfile(@AuthenticationPrincipal UserDetails userDetails) {
        User user = userRepository.findByUsername(userDetails.getUsername()).orElseThrow();
        return map(user);
    }

    @PutMapping("/me")
    public UserProfileResponse updateProfile(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody UpdateProfileRequest req) {
        User user = userRepository.findByUsername(userDetails.getUsername()).orElseThrow();
        if (req.getDisplayName() != null && !req.getDisplayName().isBlank()) {
            user.setDisplayName(req.getDisplayName());
        }
        if (req.getEmail() != null && !req.getEmail().isBlank()) {
            user.setEmail(req.getEmail());
        }
        if (req.getLiveStatusVisible() != null) {
            user.setLiveStatusVisible(req.getLiveStatusVisible());
        }
        userRepository.save(user);
        return map(user);
    }

    private UserProfileResponse map(User user) {
        return new UserProfileResponse(
                user.getId(),
                user.getUsername(),
                user.getDisplayName() != null ? user.getDisplayName() : user.getUsername(),
                user.getEmail(),
                user.getXpTotal(),
                user.isLiveStatusVisible()
        );
    }
}
