package StudyMore.models;

import java.time.LocalDateTime;

import StudyMore.Main;

public class StudySession {

    private final long sessionID;
    private final User user;
    private final LocalDateTime startTime;
    private LocalDateTime endTime;
    private Multiplier multiplier;

    private int duration;
    private int coinsEarned;
    private SessionState state;

    private int breakDuration;
    private int breakTimeRemaining;

    public StudySession(User user) {
        this.user = user;
        this.multiplier = new Multiplier();
        this.duration = 0;
        this.coinsEarned = 0;
        this.state = SessionState.IDLE;

        //StudySession existing = getTodaysSession(user.getUserId());
        StudySession existing = null;

        if (existing != null) {
            this.sessionID = existing.sessionID;
            this.startTime = existing.startTime;
            this.endTime = existing.endTime;
            this.duration = existing.duration;
            this.coinsEarned = existing.coinsEarned;
            this.multiplier = existing.multiplier;
            System.out.println("LOG: Resumed session ID: " + this.sessionID);
        } else {
            this.sessionID = SnowflakeIDGenerator.generate();
            this.startTime = LocalDateTime.now();
            System.out.println("LOG: Created new session ID: " + this.sessionID);
        }
    }

    public void start() {
        state = SessionState.STUDYING;
    }

    public void stop() {
        state = SessionState.IDLE;
    }

    public void end() {
        endTime = LocalDateTime.now();
    }

    public void incrementDuration() {
        duration++; // handles internal duration 
        multiplier.increment(); // handles multiplier
    }

    public int getDuration() {
        return duration;
    }

    public void startBreak(int breakSeconds) {
        state = SessionState.ON_BREAK;
        breakDuration = breakSeconds;
        breakTimeRemaining = breakSeconds;
    }

    public void tickBreak() {
        if (state == SessionState.ON_BREAK) {
            breakTimeRemaining--; // handle internal duration
            multiplier.applyCooldown(); // handle cooldown
        }
    }

    public boolean isBreakOver() {
        return state == SessionState.ON_BREAK && breakTimeRemaining <= 0;
    }

    public void resetBreak() {
        breakDuration = 0;
        breakTimeRemaining = 0;
    }

    public boolean isOnBreak() {
        return state == SessionState.ON_BREAK;
    }

    public int getBreakTimeRemaining() {
        return breakTimeRemaining;
    }

    public SessionState getState() {
        return state;
    }

    public Multiplier getMultiplier() {
        return multiplier;
    }

    public void calculateCoins() {
        coinsEarned = (int)((duration / 60) * multiplier.getValue());
    }

    public void updateSession() {
        //TODO
    }

    public StudySession getTodaysSession(long id) {
        //TODO 
        return null;
    }
}