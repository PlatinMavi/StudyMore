package com.codemaxxers.repository;

import com.codemaxxers.model.UserAchievement;
import com.codemaxxers.model.User;
import com.codemaxxers.model.Achievement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserAchievementRepository extends JpaRepository<UserAchievement, Long> {
    List<UserAchievement> findByUser(User user);
    Optional<UserAchievement> findByUserAndAchievement(User user, Achievement achievement);
}