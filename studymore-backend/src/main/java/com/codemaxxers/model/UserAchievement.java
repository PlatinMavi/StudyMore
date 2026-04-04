package com.codemaxxers.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_achievements",
       uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "achievement_id"}))
public class UserAchievement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
 
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
 
    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "achievement_id", nullable = false)
    private Achievement achievement;
    @Column(nullable = false)
    private int currentProgress = 0;
 
    @Column(nullable = false)
    private boolean completed = false;
    private LocalDateTime completedAt;

    public UserAchievement() {}
 
    public UserAchievement(User user, Achievement achievement) {
        this.user = user;
        this.achievement = achievement;
    }

    public boolean updateProgress(int amount) {
        if (completed) return false;
 
        currentProgress += amount;
 
        if (achievement.isCompleted(currentProgress)) {
            complete();
            return true;
        }
        return false;
    }
    public void complete() {
        if (completed) return;
        this.completed = true;
        this.completedAt = LocalDateTime.now();
        user.setCoinBalance(user.getCoinBalance() + achievement.getCoinReward());
    }
    public int getProgressPercentage() {
        return achievement.getProgressPercentage(currentProgress);
    }



    public Long getId()                                   { return id; }
    public User getUser()                                 { return user; }
    public void setUser(User user)                        { this.user = user; }
    public Achievement getAchievement()                   { return achievement; }
    public void setAchievement(Achievement achievement)   { this.achievement = achievement; }
    public int getCurrentProgress()                       { return currentProgress; }
    public void setCurrentProgress(int currentProgress)   { this.currentProgress = currentProgress; }
    public boolean isCompleted()                          { return completed; }
    public LocalDateTime getCompletedAt()                 { return completedAt; }
}