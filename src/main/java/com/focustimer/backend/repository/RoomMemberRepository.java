package com.focustimer.backend.repository;

import com.focustimer.backend.entity.Room;
import com.focustimer.backend.entity.RoomMember;
import com.focustimer.backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RoomMemberRepository extends JpaRepository<RoomMember, Long> {
    Optional<RoomMember> findByRoomAndUser(Room room, User user);
    boolean existsByRoomAndUser(Room room, User user);
}
