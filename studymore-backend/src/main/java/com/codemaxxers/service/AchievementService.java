package com.codemaxxers.service;

import com.codemaxxers.model.Achievement;
import com.codemaxxers.model.User;
import com.codemaxxers.model.UserAchievement;
import com.codemaxxers.repository.AchievementRepository;
import com.codemaxxers.repository.UserAchievementRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AchievementService {

    private final AchievementRepository achievementRepository;
    private final UserAchievementRepository userAchievementRepository;
    public AchievementService(AchievementRepository achievementRepository, 
                              UserAchievementRepository userAchievementRepository) {
        this.achievementRepository = achievementRepository;
        this.userAchievementRepository = userAchievementRepository;
    }
    public void evaluateUserAchievements(User user) {
        List<Achievement> allAchievements = achievementRepository.findAll();

        for (Achievement achievement : allAchievements) {
            
            UserAchievement userAchievement = userAchievementRepository
                .findByUserAndAchievement(user, achievement)
                .orElseGet(() -> createNewTrackingRecord(user, achievement));

            if (userAchievement.isCompleted()) {
                continue;
            }

            int currentProgress = achievement.getProgress(user);
            userAchievement.updateProgress(currentProgress);

            if (achievement.checkCompletion(user)) {
                userAchievement.complete(); 
                achievement.awardReward(user); 
                
            }
            userAchievementRepository.save(userAchievement);
        }
    }

    private UserAchievement createNewTrackingRecord(User user, Achievement achievement) {
        UserAchievement newRecord = new UserAchievement();
        newRecord.setUser(user);
        newRecord.setAchievement(achievement);
        return newRecord;
    }
}