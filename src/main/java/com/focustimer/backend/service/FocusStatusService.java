package com.focustimer.backend.service;

import com.focustimer.backend.dto.FriendStatusDTO;
import com.focustimer.backend.dto.TimerUpdateRequest;
import com.focustimer.backend.entity.FocusStatus;
import com.focustimer.backend.entity.Friendship;
import com.focustimer.backend.entity.User;
import com.focustimer.backend.repository.FocusStatusRepository;
import com.focustimer.backend.repository.FriendshipRepository;
import com.focustimer.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class FocusStatusService {

    private final UserRepository userRepository;
    private final FocusStatusRepository focusStatusRepository;
    private final FriendshipRepository friendshipRepository;
    private final SimpMessagingTemplate messagingTemplate;

    @Transactional
    public void updateStatus(String username, TimerUpdateRequest req) {
        User user = userRepository.findByUsername(username).orElseThrow();
        user.setXpTotal(req.getXpTotal());
        userRepository.save(user);

        FocusStatus status = focusStatusRepository.findByUser(user)
                .orElse(FocusStatus.builder().user(user).build());

        status.setSessionState(switch (req.getSessionState().toLowerCase()) {
            case "active" -> FocusStatus.SessionState.ACTIVE;
            case "paused" -> FocusStatus.SessionState.PAUSED;
            default       -> FocusStatus.SessionState.IDLE;
        });
        status.setRemainingSeconds(req.getRemainingSeconds());
        status.setTotalSeconds(req.getTotalSeconds());
        focusStatusRepository.save(status);

        if (user.isLiveStatusVisible()) {
            broadcastToFriends(user, status);
        }
    }

    @Transactional
    public void updatePrivacySetting(String username, boolean visible) {
        User user = userRepository.findByUsername(username).orElseThrow();
        user.setLiveStatusVisible(visible);
        userRepository.save(user);
    }

    private void broadcastToFriends(User user, FocusStatus status) {
        FriendStatusDTO dto = FriendStatusDTO.builder()
                .userId(user.getId())
                .username(user.getUsername())
                .xpTotal(user.getXpTotal())
                .sessionState(status.getSessionState().name().toLowerCase())
                .remainingSeconds(status.getRemainingSeconds())
                .totalSeconds(status.getTotalSeconds())
                .build();

        for (Friendship f : friendshipRepository.findAcceptedFriendships(user.getId())) {
            Long friendId = f.getRequester().getId().equals(user.getId())
                    ? f.getAddressee().getId()
                    : f.getRequester().getId();
            messagingTemplate.convertAndSend("/topic/friends/" + friendId, dto);
        }
    }
}
