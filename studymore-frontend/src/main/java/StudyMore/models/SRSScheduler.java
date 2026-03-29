package StudyMore.models;

import java.time.LocalDateTime;

public class SRSScheduler {

    /**
     * records the data history before modification
     * resets the task if quality criteria isnt met
     * updates the task
    */
    public void processReview(Task task, int qualityScore) {
        if (!task.isSrsEnabled() || task.getSrsData() == null) {
            return;
        }

        SRSMetadata metadata = task.getSrsData();

        // 1. Record current state into history before modifying it
        recordHistory(metadata, qualityScore);

        /** 
         * TODO: Figure out a way to check if the task is not completed at the specified day,
         * if the interval is bigger than a certain size, then add tolerated delays
         * also maybe we could make it so that the punishment isnt too harsh and add a 
         * cooldown mechanism
         * */ 

        // 2. Handle the "Reset" logic if the user failed (score < 3)
        if (qualityScore < 3) {
            resetTask(metadata);
        } else {
            // 3. Update the core values for a successful review
            updateSuccessfulTask(metadata, qualityScore);
        }
    }

    // records the last metadata before modifying it and stores it
    private void recordHistory(SRSMetadata data, int qualityScore) {
        SRSHistoryEntry newEntry = new SRSHistoryEntry(data.getCurrentEaseFactor(), data.getCurrentInterval(), qualityScore);
        data.updateHistory(newEntry);
    }

    // resets the SRS of the task to day 1 upon failing
    private void resetTask(SRSMetadata data) {
        data.setRepetitionCount(0);
        data.setCurrentInterval(1);
        // TODO: (Optional) Decide if I wanna penalize the Ease Factor here
    }

    private void updateSuccessfulTask(SRSMetadata data, int qualityScore) {
        // 1. Increment repetition count
        
        // 2. Calculate and set the new Ease Factor (EF)
        double newEF = calculateNewEaseFactor(data.getCurrentEaseFactor(), qualityScore);
        data.setCurrentEaseFactor(newEF);

        // 3. Calculate and set the next Interval (I)
        int nextInterval = calculateNextInterval(data, newEF);
        data.setCurrentInterval(nextInterval);

        // TODO: Update the actual calendar date for the next review
        // could use this: LocalDateTime.now().plusDays(nextInterval)
    }

    // calculates the new ease factor value
    private double calculateNewEaseFactor(double oldEF, int q) {        
        double newEF = oldEF + (0.1 - (5 - q) * (0.08 + (5 - q) * 0.02));
        // capping it at 1.3
        return Math.max(1.3, newEF);
    }

    private int calculateNextInterval(SRSMetadata data, double easeFactor) {
        int n = data.getRepetitionCount();
        if (n == 0){
            return 1;
        } else if (n == 2){
            return data.getIntensity().getSecondInterval();
        } else if (n > 2){
            return (int) Math.round(data.getCurrentInterval() * easeFactor);
        } 

        return -1;
    }
}