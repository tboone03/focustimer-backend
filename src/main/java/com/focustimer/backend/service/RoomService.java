package com.focustimer.backend.service;

import com.focustimer.backend.dto.CreateRoomRequest;
import com.focustimer.backend.dto.RoomDTO;
import com.focustimer.backend.dto.RoomMemberDTO;
import com.focustimer.backend.entity.Room;
import com.focustimer.backend.entity.RoomMember;
import com.focustimer.backend.entity.User;
import com.focustimer.backend.repository.RoomMemberRepository;
import com.focustimer.backend.repository.RoomRepository;
import com.focustimer.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RoomService {

    private final RoomRepository roomRepository;
    private final RoomMemberRepository roomMemberRepository;
    private final UserRepository userRepository;

    private static final String CODE_CHARS = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
    private static final SecureRandom RANDOM = new SecureRandom();

    private String generateCode() {
        StringBuilder sb = new StringBuilder(6);
        for (int i = 0; i < 6; i++) {
            sb.append(CODE_CHARS.charAt(RANDOM.nextInt(CODE_CHARS.length())));
        }
        String code = sb.toString();
        return roomRepository.findByCode(code).isPresent() ? generateCode() : code;
    }

    @Transactional
    public RoomDTO createRoom(String username, CreateRoomRequest req) {
        if (req.getName() == null || req.getName().isBlank()) {
            throw new IllegalArgumentException("Room name cannot be blank");
        }
        User user = userRepository.findByUsername(username).orElseThrow();
        Room room = Room.builder()
                .name(req.getName().trim())
                .code(generateCode())
                .host(user)
                .isPublic(req.isPublic())
                .build();
        roomRepository.save(room);

        RoomMember member = RoomMember.builder()
                .room(room)
                .user(user)
                .build();
        roomMemberRepository.save(member);
        room.getMembers().add(member);

        return toDTO(room);
    }

    @Transactional
    public RoomDTO joinByCode(String username, String code) {
        User user = userRepository.findByUsername(username).orElseThrow();
        Room room = roomRepository.findByCode(code.toUpperCase().trim())
                .orElseThrow(() -> new IllegalArgumentException("Room not found"));
        if (!room.isActive()) throw new IllegalStateException("Room is no longer active");

        if (!roomMemberRepository.existsByRoomAndUser(room, user)) {
            RoomMember member = RoomMember.builder()
                    .room(room)
                    .user(user)
                    .build();
            roomMemberRepository.save(member);
            room.getMembers().add(member);
        }
        return toDTO(room);
    }

    @Transactional
    public void leaveRoom(String username, Long roomId) {
        User user = userRepository.findByUsername(username).orElseThrow();
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("Room not found"));

        roomMemberRepository.findByRoomAndUser(room, user).ifPresent(m -> {
            roomMemberRepository.delete(m);
            room.getMembers().remove(m);
        });

        if (room.getHost().getId().equals(user.getId()) && room.getMembers().isEmpty()) {
            room.setActive(false);
            roomRepository.save(room);
        }
    }

    @Transactional(readOnly = true)
    public List<RoomDTO> getVisibleRooms(String username) {
        User user = userRepository.findByUsername(username).orElseThrow();
        return roomRepository.findVisibleRooms(user).stream()
                .map(this::toDTO)
                .toList();
    }

    private RoomDTO toDTO(Room room) {
        List<RoomMemberDTO> memberDTOs = room.getMembers().stream()
                .map(m -> RoomMemberDTO.builder()
                        .userId(m.getUser().getId())
                        .username(m.getUser().getUsername())
                        .build())
                .toList();
        return RoomDTO.builder()
                .id(room.getId())
                .name(room.getName())
                .code(room.getCode())
                .hostId(room.getHost().getId())
                .hostUsername(room.getHost().getUsername())
                .isPublic(room.isPublic())
                .memberCount(memberDTOs.size())
                .members(memberDTOs)
                .createdAt(room.getCreatedAt())
                .build();
    }
}
