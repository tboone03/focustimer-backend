package com.focustimer.backend.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class FriendStatusDTO {
    private long userId;
    private String username;
    private String sessionState; // "idle" | "active" | "paused" | "hidden"
    private int remainingSeconds;
    private int totalSeconds;
    private long xpTotal;
}
