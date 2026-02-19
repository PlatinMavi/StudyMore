package StudyMore.controllers;
import javafx.animation.*;
import javafx.scene.control.*;
import javafx.util.Duration;
import javafx.fxml.FXML;

public class StudyController {
    @FXML
    private Label timerLabel;

    @FXML
    private Button timerControlButton;

    @FXML
    private Button longBreakButton;

    @FXML
    private Button shortBreakButton;

    private int secondsElapsed = 0;
    private int breakTime = 0;
    private Timeline timeline;
    private Timeline breakTimeline;
    private boolean isWorking = false;

    public void initialize() {
        KeyFrame keyFrame = new KeyFrame(Duration.seconds(1), event -> {
            secondsElapsed++;
            updateTimer(secondsElapsed);
        });

        timeline = new Timeline(keyFrame);
        timeline.setCycleCount(Timeline.INDEFINITE);
        startTimer();
    }

    @FXML
    private void timerController() {
        if (isWorking) {
            stopTimer();
        } else {
            startTimer();
        }
    }

    @FXML
    private void longBreak() {
        giveBreak(1200); // 20 min
    }

    @FXML
    private void shortBreak() {
        giveBreak(600); // 10 min
    }

    private void giveBreak(int time) {
        stopTimer();
        
        if (breakTimeline != null) {
            breakTimeline.stop();
        }
        
        breakTime = time;
        
        KeyFrame breakKeyFrame = new KeyFrame(Duration.seconds(1), event -> {
            breakTime--;

            if (breakTime <= 0) {
                breakTimeline.stop();
                breakTime = 0;
                startTimer(); 
            } else {
                updateTimer(breakTime);
            }
        });
        
        breakTimeline = new Timeline(breakKeyFrame);
        breakTimeline.setCycleCount(Timeline.INDEFINITE);
        breakTimeline.play();
    }


    private void startTimer(){
        if(breakTime != 0) {
            breakTime = 0;
            breakTimeline.stop();
        }

        timeline.play();
        isWorking = true;
        timerControlButton.setText("Stop");
    }

    private void stopTimer(){
        timeline.stop();
        isWorking = false;
        timerControlButton.setText("Start");
    }

    private void updateTimer(int secondsTotal) {  
        int hours = secondsTotal / 3600;
        int minutes = (secondsTotal % 3600) / 60;
        int seconds = secondsTotal % 60;
        
        // String timeString = String.format("%02d:%02d:%02d", hours, minutes, seconds);
        
        if(hours > 0) {
            timerLabel.setText(String.format("%02d:%02d", hours, minutes));
        } else {
            timerLabel.setText(String.format("%02d:%02d", minutes, seconds));
        }
    }
}
