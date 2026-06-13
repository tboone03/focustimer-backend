package com.focustimer.backend.repository;

import com.focustimer.backend.entity.Room;
import com.focustimer.backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface RoomRepository extends JpaRepository<Room, Long> {

    Optional<Room> findByCode(String code);

    @Query("""
            SELECT r FROM Room r
            WHERE r.isActive = true
              AND (r.isPublic = true
                   OR EXISTS (SELECT m FROM RoomMember m WHERE m.room = r AND m.user = :user))
            ORDER BY r.createdAt DESC
            """)
    List<Room> findVisibleRooms(@Param("user") User user);
}
