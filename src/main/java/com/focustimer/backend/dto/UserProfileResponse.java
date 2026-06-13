package com.focustimer.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UserProfileResponse {
    private Long id;
    private String username;
    private String displayName;
    private String email;
    private long xpTotal;
    private boolean liveStatusVisible;
}
