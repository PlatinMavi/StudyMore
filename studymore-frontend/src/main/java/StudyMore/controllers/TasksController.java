/**
 * add a time check to make sure the tasks are valid
 */

package StudyMore.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import java.io.IOException;

public class TasksController {

    @FXML private StackPane pageRoot;
    @FXML private FlowPane normalTasksContainer;
    @FXML private FlowPane srsTasksContainer;

    // This flag is necessary since we are calling initialize more than once, both at start and after closing task overlay.
    private boolean isInitialized = false;

    public void initialize() {
        if (!isInitialized) {
            refreshTaskDisplay();
            isInitialized = true;
        }
    }

    @FXML
    private void handleCreateTask() {
        showOverlay("../fxml/CreateTaskOverlay.fxml");
    }

    private void showOverlay(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            loader.setController(this); 
            Parent overlay = loader.load();
            pageRoot.getChildren().add(overlay);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void saveTask() {
        Node overlay = pageRoot.getChildren().get(pageRoot.getChildren().size() - 1);

        TextField titleField = (TextField) overlay.lookup("#titleInput");
        TextArea contentArea = (TextArea) overlay.lookup("#contentInput");
        CheckBox srsBox = (CheckBox) overlay.lookup("#srsToggle");

        if (titleField == null) return;

        String title = titleField.getText();
        String content = (contentArea != null) ? contentArea.getText() : "";
        boolean isSrs = (srsBox != null) && srsBox.isSelected();

        if (title == null || title.isEmpty()) return;

        // TODO: update this part with the database and a task object
        addTaskToGrid(title, content, isSrs, "1 DAY");

        closeOverlay();
    }

    @FXML
    private void closeOverlay() {
        if (pageRoot.getChildren().size() > 1) {
            pageRoot.getChildren().remove(pageRoot.getChildren().size() - 1);
        }
    }

    private void refreshTaskDisplay() {
        normalTasksContainer.getChildren().clear();
        srsTasksContainer.getChildren().clear();
    }

    private void addTaskToGrid(String title, String content, boolean isSrs, String recallTime) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("../fxml/TaskCard.fxml"));
            VBox card = loader.load();

            Label titleLabel = (Label) card.lookup("#taskTitle");
            Label contentLabel = (Label) card.lookup("#taskContent");
            VBox srsBadge = (VBox) card.lookup("#srsBadgeContainer");
            Label recallLabel = (Label) card.lookup("#recallLabel");

            if (titleLabel != null) titleLabel.setText(title.toUpperCase());
            if (contentLabel != null) contentLabel.setText(content);

            if (isSrs) {
                if (srsBadge != null) {
                    srsBadge.setManaged(true);
                    srsBadge.setVisible(true);
                }
                if (recallLabel != null) recallLabel.setText(recallTime);
                srsTasksContainer.getChildren().add(card);
            } else {
                if (srsBadge != null) {
                    srsBadge.setManaged(false);
                    srsBadge.setVisible(false);
                }
                normalTasksContainer.getChildren().add(card);
            }
        } catch (IOException e) {
            System.err.println("Error loading TaskCard.fxml: " + e.getMessage());
        }
    }
}