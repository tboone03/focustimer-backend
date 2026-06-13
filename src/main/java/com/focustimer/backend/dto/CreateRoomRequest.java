package com.focustimer.backend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Data
public class CreateRoomRequest {
    private String name;
    @JsonProperty("isPublic")
    private boolean isPublic = true;
}
