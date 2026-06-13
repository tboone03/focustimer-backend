package com.focustimer.backend.service;

import com.focustimer.backend.dto.LeaderboardEntryDTO;
import com.focustimer.backend.entity.User;
import com.focustimer.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Service
@RequiredArgsConstructor
public class LeaderboardService {

    private static final int XP_PER_LEVEL = 500;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public List<LeaderboardEntryDTO> getGlobalLeaderboard() {
        AtomicInteger rank = new AtomicInteger(1);
        return userRepository.findAllOrderByXpDesc().stream()
                .map(u -> toEntry(u, rank.getAndIncrement()))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<LeaderboardEntryDTO> getFriendsLeaderboard(String username) {
        User user = userRepository.findByUsername(username).orElseThrow();
        AtomicInteger rank = new AtomicInteger(1);
        return userRepository.findFriendsOrderByXpDesc(user.getId()).stream()
                .map(u -> toEntry(u, rank.getAndIncrement()))
                .toList();
    }

    private LeaderboardEntryDTO toEntry(User u, int rank) {
        return LeaderboardEntryDTO.builder()
                .rank(rank)
                .userId(u.getId())
                .username(u.getUsername())
                .xpTotal(u.getXpTotal())
                .level((int) (u.getXpTotal() / XP_PER_LEVEL) + 1)
                .build();
    }
}
