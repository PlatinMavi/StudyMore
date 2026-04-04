package com.codemaxxers.service;

import com.codemaxxers.model.Achievement;
import com.codemaxxers.model.User;
import com.codemaxxers.model.UserAchievement;
import com.codemaxxers.model.enums.AchievementType;
import com.codemaxxers.repository.AchievementRepository;
import com.codemaxxers.repository.UserAchievementRepository;
import com.codemaxxers.repository.UserRepository;
import com.codemaxxers.event.AchievementUnlockedEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.util.List;
import org.springframework.transaction.annotation.Transactional;


@Service
@Transactional
public class AchievementService {
 
    private final AchievementRepository achievementRepository;
    private final UserAchievementRepository userAchievementRepository;
    private final UserRepository userRepository;
    private final ApplicationEventPublisher eventPublisher;
 
    public AchievementService(AchievementRepository achievementRepository,
                              UserAchievementRepository userAchievementRepository,
                              UserRepository userRepository,
                              ApplicationEventPublisher eventPublisher) {
        this.achievementRepository = achievementRepository;
        this.userAchievementRepository = userAchievementRepository;
        this.userRepository = userRepository;
        this.eventPublisher = eventPublisher;
    }
    public void initAchievementsForUser(User user) {
        List<Achievement> all = achievementRepository.findAll();
        for (Achievement achievement : all) {
            if (!userAchievementRepository
                    .existsByUser_IdAndAchievement_AchievementId(user.getUserId(), achievement.getAchievementId())) {
                userAchievementRepository.save(new UserAchievement(user, achievement));
            }
        }
    }
    public void notifyStudyTimeAdded(Long userId, int minutesStudied) {
        updateProgress(userId, AchievementType.TIME_BASED, minutesStudied);
    }
 

    public void notifyStreakUpdated(Long userId, int currentStreak) {
        setProgress(userId, AchievementType.STREAK_BASED, currentStreak);
    }

    public void notifyFriendAdded(Long userId) {
        updateProgress(userId, AchievementType.SOCIAL, 1);
    }
 

    public void notifyTaskCompleted(Long userId) {
        updateProgress(userId, AchievementType.TASK_BASED, 1);
    }
 

    public void notifyCoinsEarned(Long userId, int coinsEarned) {
        updateProgress(userId, AchievementType.COIN_BASED, coinsEarned);
    }

    // queries 


    @Transactional(readOnly = true)
    public List<UserAchievement> getAllForUser(Long userId) {
        return userAchievementRepository.findByUser_Id(userId);
    }
 
    @Transactional(readOnly = true)
    public List<UserAchievement> getCompletedForUser(Long userId) {
        return userAchievementRepository.findByUser_IdAndCompletedTrue(userId);
    }
 
    @Transactional(readOnly = true)
    public List<UserAchievement> getInProgressForUser(Long userId) {
        return userAchievementRepository.findByUser_IdAndCompletedFalse(userId);
    }
 
   // admin helpers
    public Achievement createAchievement(Achievement achievement) {
        Achievement saved = achievementRepository.save(achievement);
        userRepository.findAll().forEach(u -> initAchievementsForUser(u));
        return saved;
    }

    private void updateProgress(Long userId, AchievementType type, int amount) {
        List<UserAchievement> targets =
                userAchievementRepository.findByUserIdAndType(userId, type);
        for (UserAchievement ua : targets) {
            boolean justUnlocked = ua.updateProgress(amount);
            userAchievementRepository.save(ua);
            if (justUnlocked) {
                eventPublisher.publishEvent(new AchievementUnlockedEvent(this, ua));
            }
        }
    }
 
    private void setProgress(Long userId, AchievementType type, int value) {
        List<UserAchievement> targets =
                userAchievementRepository.findByUserIdAndType(userId, type);
        for (UserAchievement ua : targets) {
            if (ua.isCompleted()) continue;
            ua.setCurrentProgress(value);
            boolean nowDone = ua.getAchievement().isCompleted(value);
            if (nowDone) ua.complete();
            userAchievementRepository.save(ua);
            if (nowDone) {
                eventPublisher.publishEvent(new AchievementUnlockedEvent(this, ua));
            }
        }
    }
}