package StudyMore;

import StudyMore.db.DatabaseManager;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.sql.*;

public class Main extends Application {
    public static DatabaseManager mngr;
    public static Stage primarStageStatic;

    @Override
    public void start(Stage primaryStage) throws Exception{
        primarStageStatic = primaryStage; // will use this to change the fxml for the user

        if(isUserLoggedIn()) {
            Parent root = FXMLLoader.load(getClass().getResource("fxml/Index.fxml"));
            primaryStage.setTitle("StudyMore");
            primaryStage.setScene(new Scene(root, 1200, 800));
            primaryStage.show();
        } else {
            Parent root = FXMLLoader.load(getClass().getResource("fxml/loginRegister.fxml"));
            primaryStage.setTitle("StudyMore");
            primaryStage.setScene(new Scene(root, 1200, 800));
            primaryStage.show();
        }
    }


    public static void main(String[] args) {
        mngr = new DatabaseManager();

        launch(args);
    }

    public static boolean isUserLoggedIn() {
        //Runs a querry checking if there are any users in the table 
        //(if there are more than 1 it will delete both and will send you to login page.)

        String querry = "SELECT * from users";

        try (Statement stmt = mngr.getConnection().createStatement();
            ResultSet rs = stmt.executeQuery(querry)) {

            int count = 0;
            while(rs.next()) {
                count++;
            }
            
            if(count != 1) {
                return false;
            }

            //TODO, save the user in here so other methods can acces without query.
            return true;
        } catch(Exception e) {} 

        return false;
    }
}
    