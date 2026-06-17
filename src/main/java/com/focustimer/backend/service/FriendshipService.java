package com.focustimer.backend.service;

import com.focustimer.backend.dto.FriendStatusDTO;
import com.focustimer.backend.dto.PendingRequestDTO;
import com.focustimer.backend.entity.Friendship;
import com.focustimer.backend.entity.Friendship.FriendshipStatus;
import com.focustimer.backend.entity.FocusStatus;
import com.focustimer.backend.entity.User;
import com.focustimer.backend.repository.FocusStatusRepository;
import com.focustimer.backend.repository.FriendshipRepository;
import com.focustimer.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class FriendshipService {

    private final UserRepository userRepository;
    private final FriendshipRepository friendshipRepository;
    private final FocusStatusRepository focusStatusRepository;
    private final SimpMessagingTemplate messagingTemplate;

    @Transactional
    public void sendRequest(String requesterUsername, Long addresseeId) {
        User requester = userRepository.findByUsername(requesterUsername).orElseThrow();
        User addressee = userRepository.findById(addresseeId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        if (requester.getId().equals(addresseeId))
            throw new IllegalArgumentException("Cannot friend yourself");
        friendshipRepository.findBetweenUsers(requester, addressee).ifPresent(f -> {
            throw new IllegalStateException("Friendship already exists");
        });
        friendshipRepository.save(Friendship.builder()
                .requester(requester)
                .addressee(addressee)
                .status(FriendshipStatus.PENDING)
                .build());
        // notify the addressee that they have a new request
        messagingTemplate.convertAndSend(
                "/topic/friends/" + addresseeId + "/requests",
                Map.of("from", requester.getUsername(), "requesterId", requester.getId()));
    }

    @Transactional
    public void respondToRequest(String addresseeUsername, Long friendshipId, boolean accept) {
        Friendship f = friendshipRepository.findById(friendshipId)
                .orElseThrow(() -> new IllegalArgumentException("Request not found"));
        if (!f.getAddressee().getUsername().equals(addresseeUsername))
            throw new SecurityException("Not your request");
        f.setStatus(accept ? FriendshipStatus.ACCEPTED : FriendshipStatus.DECLINED);
        friendshipRepository.save(f);
        if (accept) {
            // notify both users that a new friendship was accepted
            messagingTemplate.convertAndSend(
                    "/topic/friends/" + f.getRequester().getId() + "/requests",
                    Map.of("accepted", true, "by", f.getAddressee().getUsername()));
        }
    }

    @Transactional(readOnly = true)
    public List<PendingRequestDTO> getPendingRequests(String username) {
        User user = userRepository.findByUsername(username).orElseThrow();
        return friendshipRepository.findPendingRequestsForUser(user.getId()).stream()
                .map(f -> PendingRequestDTO.builder()
                        .friendshipId(f.getId())
                        .requesterId(f.getRequester().getId())
                        .requesterUsername(f.getRequester().getUsername())
                        .createdAt(f.getCreatedAt())
                        .build())
                .toList();
    }

    @Transactional(readOnly = true)
    public List<FriendStatusDTO> getFriendsWithStatus(String username) {
        User me = userRepository.findByUsername(username).orElseThrow();
        return friendshipRepository.findAcceptedFriendships(me.getId()).stream()
                .map(f -> {
                    User friend = f.getRequester().getId().equals(me.getId())
                            ? f.getAddressee() : f.getRequester();

                    FocusStatus status = focusStatusRepository.findByUser(friend).orElse(null);
                    boolean showLive = friend.isLiveStatusVisible()
                            && status != null
                            && status.getSessionState() != FocusStatus.SessionState.IDLE;

                    return FriendStatusDTO.builder()
                            .userId(friend.getId())
                            .username(friend.getUsername())
                            .xpTotal(friend.getXpTotal())
                            .sessionState(friend.isLiveStatusVisible() && status != null
                                    ? status.getSessionState().name().toLowerCase()
                                    : "hidden")
                            .remainingSeconds(showLive ? status.getRemainingSeconds() : 0)
                            .totalSeconds(showLive ? status.getTotalSeconds() : 0)
                            .build();
                }).toList();
    }
}
