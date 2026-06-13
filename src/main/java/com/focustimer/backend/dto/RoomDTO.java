package com.focustimer.backend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class RoomDTO {
    private Long id;
    private String name;
    private String code;
    private Long hostId;
    private String hostUsername;
    @JsonProperty("isPublic")
    private boolean isPublic;
    private int memberCount;
    private List<RoomMemberDTO> members;
    private LocalDateTime createdAt;
}
