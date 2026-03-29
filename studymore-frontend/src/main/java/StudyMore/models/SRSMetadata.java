package StudyMore.models;

import java.time.LocalDate;
import java.util.List;
import java.util.ArrayList;

public class SRSMetadata {
    private int repetitionCount = 0;
    private double currentEaseFactor = 2.5;
    private int currentInterval = 0;
    private ReviewIntensity reviewIntensity;
    private List<SRSHistoryEntry> history;

    public SRSMetadata(ReviewIntensity intensity){
        this.reviewIntensity = intensity;
        this.history = new ArrayList<SRSHistoryEntry>();
    }

    public void updateHistory(SRSHistoryEntry newEntry){
        this.history.add(newEntry);
    }

    // GETTERS

    public int getRepetitionCount(){
        return this.repetitionCount;
    }

    public double getCurrentEaseFactor(){
        return this.currentEaseFactor;
    }

    public int getCurrentInterval(){
        return this.currentInterval;
    }

    public ReviewIntensity getIntensity(){
        return this.reviewIntensity;
    }
    
    // SETTERS

    public void setRepetitionCount(int repCount){
        this.repetitionCount = repCount;
    }

    public void setCurrentEaseFactor(double EF){
        this.currentEaseFactor = EF;
    }

    public void setCurrentInterval(int interval){
        this.currentInterval = interval;
    }


}
