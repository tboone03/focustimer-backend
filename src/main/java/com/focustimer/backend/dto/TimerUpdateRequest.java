package com.focustimer.backend.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class TimerUpdateRequest {

    @NotBlank
    private String sessionState; // "idle" | "active" | "paused"

    private int remainingSeconds;
    private int totalSeconds;
    private long xpTotal;
}
