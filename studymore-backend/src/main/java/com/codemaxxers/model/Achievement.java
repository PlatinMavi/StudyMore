package com.codemaxxers.model;
import com.codemaxxers.model.enums.AchievementType;
import jakarta.persistence.*;

@Entity
@Table(name = "achievements")
public class Achievement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long achievementId;

    private String title;

    private String description;

    @Enumerated(EnumType.STRING)
    private AchievementType type;

    private int targetValue;
    private int reward;

    private String iconPath;

    public Achievement() {}
 
    public Achievement(AchievementType type, String title, String description,
                       int targetValue, int reward, String iconPath) {
        this.type = type;
        this.title = title;
        this.description = description;
        this.targetValue = targetValue;
        this.reward = reward;
        this.iconPath = iconPath;
    }

    public Long getAchievementId()                        { return achievementId; }
    public AchievementType getType()                      { return type; }
    public void setType(AchievementType type)             { this.type = type; }
    public String getDescription()                        { return description; }
    public void setDescription(String description)        { this.description = description; }
    public int getTargetValue()                           { return targetValue; }
    public void setTargetValue(int targetValue)           { this.targetValue = targetValue; }
    public int getCoinReward()                            { return reward; }
    public void setCoinReward(int reward)             { this.reward = reward; }
    public String getIconPath()                           { return iconPath; }
    public void setIconPath(String iconPath)              { this.iconPath = iconPath; }
    public String getTitle()                              { return title; }
    public void setTitle(String title)                    { this.title = title; }
    
    public boolean isCompleted(int currentProgress) {
        return currentProgress >= targetValue;
    }
    public int getProgressPercentage(int currentProgress) {
        if (targetValue == 0) return 100;
        return Math.min(100, (int) ((currentProgress / (double) targetValue) * 100));
    }
}