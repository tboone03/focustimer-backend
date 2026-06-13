package com.focustimer.backend.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "focus_status")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class FocusStatus {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", unique = true, nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "session_state", nullable = false, length = 20)
    @Builder.Default
    private SessionState sessionState = SessionState.IDLE;

    @Column(name = "remaining_seconds")
    @Builder.Default
    private int remainingSeconds = 0;

    @Column(name = "total_seconds")
    @Builder.Default
    private int totalSeconds = 0;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public enum SessionState {
        IDLE, ACTIVE, PAUSED
    }
}
