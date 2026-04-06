// TODO FOR EXPANSION: Implement a leach threshold mechanism to handle the tasks 
// that are failed couple of times.

package StudyMore.models;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class SRSScheduler {

    /**
     * records the data history before modification
     * resets the task if quality criteria isnt met
     * updates the task
    */
    public static void processReview(Task task, int qualityScore) {
        if (!task.isSrsEnabled() || task.getSrsData() == null) {
            return;
        }

        SRSMetadata metadata = task.getSrsData();

        // Record current state into history before modifying it
        recordHistory(metadata, qualityScore);

        double newEF = calculateNewEaseFactor(metadata.getCurrentEaseFactor(), qualityScore);
        metadata.setCurrentEaseFactor(newEF);

        // RESET LOGIC (if the user failed - score < 3)
        if (qualityScore < 3) {
            resetTask(metadata);
            task.setNextRecallDate(LocalDate.now().plusDays(metadata.getCurrentInterval()));
        } else {
            // Update the core values for a successful review
            updateSuccessfulTask(metadata, qualityScore, task);
        }
    }

    // records the last metadata before modifying it and stores it
    private static void recordHistory(SRSMetadata data, int qualityScore) {
        SRSHistoryEntry newEntry = new SRSHistoryEntry(data.getCurrentEaseFactor(), data.getCurrentInterval(), qualityScore);
        data.updateHistory(newEntry);
    }

    // resets the SRS of the task to day 1 upon failing
    private static void resetTask(SRSMetadata data) {
        data.setRepetitionCount(0);
        data.setCurrentInterval(1);
    }

    private static void updateSuccessfulTask(SRSMetadata data, int qualityScore, Task task) {
        // 1. Increment repetition count
        data.setRepetitionCount(data.getRepetitionCount() + 1);

        // 2. Calculate and set the next Interval (I)
        int nextInterval = calculateNextInterval(data, task.getSrsData().getCurrentEaseFactor());
        data.setCurrentInterval(nextInterval);

        // 3. Update the actual calendar date for the next review
        LocalDate nextDate = LocalDate.now().plusDays(nextInterval);
        task.setNextRecallDate(nextDate);
    }

    // Calculate the new ease factor value
    private static double calculateNewEaseFactor(double oldEF, int q) {        
        double newEF = oldEF + (0.1 - (5 - q) * (0.08 + (5 - q) * 0.02));
        // Capping it at 1.3
        return Math.max(1.3, newEF);
    }

    private static int calculateNextInterval(SRSMetadata data, double easeFactor) {
        int reps = data.getRepetitionCount();
        if (reps == 1){
            return 1;
        } else if (reps == 2){
            return data.getIntensity().getSecondInterval();
        } else if (reps > 2){
            return (int) Math.round(data.getCurrentInterval() * easeFactor);
        } 

        return -1;
    }
}