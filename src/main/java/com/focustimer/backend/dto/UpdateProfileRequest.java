package com.focustimer.backend.dto;

import lombok.Data;

@Data
public class UpdateProfileRequest {
    private String displayName;
    private String email;
    private Boolean liveStatusVisible;
}
