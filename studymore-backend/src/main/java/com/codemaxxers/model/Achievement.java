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
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    private String description;

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Enumerated(EnumType.STRING)
    private AchievementType type;

    private int targetValue;
    private int reward;
    public int getReward() {
        return reward;
    }

    public void setReward(int reward) {
        this.reward = reward;
    }

    private String iconPath;

    public String getIconPath() {
        return iconPath;
    }

    public void setIconPath(String iconPath) {
        this.iconPath = iconPath;
    }

    public boolean checkCompletion(User user) {
        return getProgress(user) >= this.targetValue;
    }

    public void awardReward(User user) {
        //TODO
    }

    public int getProgress(User user) {
        return 1; //TODO
    }
}