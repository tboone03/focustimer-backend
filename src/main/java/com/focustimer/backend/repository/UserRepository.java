package com.focustimer.backend.repository;

import com.focustimer.backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    @Query("SELECT u FROM User u ORDER BY u.xpTotal DESC")
    List<User> findAllOrderByXpDesc();

    @Query("""
            SELECT u FROM User u
            WHERE u.id IN (
                SELECT CASE WHEN f.requester.id = :userId
                            THEN f.addressee.id
                            ELSE f.requester.id END
                FROM Friendship f
                WHERE (f.requester.id = :userId OR f.addressee.id = :userId)
                  AND f.status = 'ACCEPTED'
            )
            ORDER BY u.xpTotal DESC
            """)
    List<User> findFriendsOrderByXpDesc(@Param("userId") Long userId);
}
