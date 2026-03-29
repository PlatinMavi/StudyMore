package StudyMore.models;

public enum ReviewIntensity {
    INTENSE(3),
    STANDARD(6),
    RELAXED(10);

    private int secondInterval;

    private ReviewIntensity(int secondInterval){
        this.secondInterval = secondInterval;
    }

    public int getSecondInterval(){
        return this.secondInterval;
    }
}
