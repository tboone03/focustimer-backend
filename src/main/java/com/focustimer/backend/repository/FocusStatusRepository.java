package com.focustimer.backend.repository;

import com.focustimer.backend.entity.FocusStatus;
import com.focustimer.backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface FocusStatusRepository extends JpaRepository<FocusStatus, Long> {
    Optional<FocusStatus> findByUser(User user);
}
