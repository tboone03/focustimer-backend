package com.focustimer.backend.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "rooms")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Room {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(unique = true, nullable = false, length = 8)
    private String code;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "host_id", nullable = false)
    private User host;

    @Column(name = "is_public")
    @Builder.Default
    private boolean isPublic = true;

    @Column(name = "is_active")
    @Builder.Default
    private boolean isActive = true;

    @OneToMany(mappedBy = "room", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    @Builder.Default
    private List<RoomMember> members = new ArrayList<>();

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
