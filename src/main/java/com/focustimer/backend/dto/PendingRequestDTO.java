package com.focustimer.backend.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class PendingRequestDTO {
    private long friendshipId;
    private long requesterId;
    private String requesterUsername;
    private LocalDateTime createdAt;
}
