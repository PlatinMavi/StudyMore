package StudyMore.models;

import java.time.LocalDateTime;

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
}
