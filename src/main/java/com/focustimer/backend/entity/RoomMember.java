package com.focustimer.backend.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "room_members",
       uniqueConstraints = @UniqueConstraint(columnNames = {"room_id", "user_id"}))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class RoomMember {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id", nullable = false)
    private Room room;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "joined_at", updatable = false)
    private LocalDateTime joinedAt;

    @PrePersist
    protected void onJoin() {
        joinedAt = LocalDateTime.now();
    }
}
