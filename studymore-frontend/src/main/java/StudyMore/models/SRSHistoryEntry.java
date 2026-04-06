package StudyMore.models;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class SRSHistoryEntry {
    private double easeFactorAtTime;
    private int intervalAtTime;
    private int qualityScore;
    private LocalDateTime timeStamp;

    public SRSHistoryEntry(double EF, int interval, int qualityScore){
        this.easeFactorAtTime = EF;
        this.intervalAtTime = interval;
        this.qualityScore = qualityScore;
        this.timeStamp = LocalDateTime.now();
    }

    public double getEaseFactorAtTime(){
        return this.easeFactorAtTime;
    }

    public int getIntervalAtTime(){
        return this.intervalAtTime;
    }

    public int getQualityScore(){
        return this.qualityScore;
    }
}
