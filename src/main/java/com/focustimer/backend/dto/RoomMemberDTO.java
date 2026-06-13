package com.focustimer.backend.dto;

import lombok.*;

@Data
@Builder
public class RoomMemberDTO {
    private Long userId;
    private String username;
}
