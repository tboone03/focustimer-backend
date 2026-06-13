package com.focustimer.backend.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LeaderboardEntryDTO {
    private int rank;
    private long userId;
    private String username;
    private long xpTotal;
    private int level;
}
