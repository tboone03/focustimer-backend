package com.focustimer.backend.repository;

import com.focustimer.backend.entity.Friendship;
import com.focustimer.backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface FriendshipRepository extends JpaRepository<Friendship, Long> {

    @Query("""
            SELECT f FROM Friendship f
            WHERE (f.requester.id = :userId OR f.addressee.id = :userId)
              AND f.status = 'ACCEPTED'
            """)
    List<Friendship> findAcceptedFriendships(@Param("userId") Long userId);

    @Query("SELECT f FROM Friendship f WHERE f.addressee.id = :userId AND f.status = 'PENDING'")
    List<Friendship> findPendingRequestsForUser(@Param("userId") Long userId);

    @Query("""
            SELECT f FROM Friendship f
            WHERE (f.requester = :u1 AND f.addressee = :u2)
               OR (f.requester = :u2 AND f.addressee = :u1)
            """)
    Optional<Friendship> findBetweenUsers(@Param("u1") User u1, @Param("u2") User u2);
}
