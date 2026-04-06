package StudyMore.controllers;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import StudyMore.ApiClient;
import StudyMore.Main;
import StudyMore.models.SnowflakeIDGenerator;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class LoginRegisterController {

    @FXML private TextField loginUsernameField;
    @FXML private PasswordField loginPasswordField;
    @FXML private TextField registerUsernameField;
    @FXML private TextField registerEmailField;
    @FXML private PasswordField registerPasswordField;
    @FXML private Label loginErrorLabel;
    @FXML private Label registerErrorLabel;

    @FXML
    public void initialize() {
    }

    @FXML
    private void handleLogin(ActionEvent event) {
        String username = loginUsernameField.getText().trim();
        String password = loginPasswordField.getText();

        if (username.isEmpty() || password.isEmpty()) {
            showLoginError("Please fill in all fields.");
            return;
        }

        String hashedPassword = sha256(password);

        // local database first
        String sql = "SELECT id FROM users WHERE username = ? AND password_hash = ?";
        try (PreparedStatement pstmt = Main.mngr.getConnection().prepareStatement(sql)) {
            pstmt.setString(1, username);
            pstmt.setString(2, hashedPassword);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    // Found in local DB
                    long userId = rs.getLong("id");
                    Main.user = Main.mngr.getUser(userId);
                    navigateToMain();
                } else {
                    // Not found locally — try backend
                    String body = "{\"email\":\"" + username + "\","
                                + "\"password\":\"" + password + "\"}";
                    String response = ApiClient.postAuth("/auth/login", body);

                    if (response != null && response.contains("token")) {
                        System.out.println("Logged in via backend: " + response);
                        navigateToMain();
                    } else {
                        showLoginError("Invalid username or password.");
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            showLoginError("Login failed. Please try again.");
        }
    }

    @FXML
    private void handleRegister(ActionEvent event) {
        String username = registerUsernameField.getText();
        String email = registerEmailField.getText();
        String password = registerPasswordField.getText();

        if (username.isEmpty() || email.isEmpty() || password.isEmpty()) {
            showRegisterError("Please fill in all fields.");
            return;
        }

        final String sql = """
            INSERT INTO users(id, username, email, password_hash)
            VALUES (?, ?, ?, ?)
            """;

        try (PreparedStatement pstmt = Main.mngr.getConnection().prepareStatement(sql)) {
            long id = SnowflakeIDGenerator.generate();
            pstmt.setLong(1, id);
            pstmt.setString(2, username);
            pstmt.setString(3, email);
            pstmt.setString(4, sha256(password));
            int rows = pstmt.executeUpdate();

            if (rows != 1) {
                throw new IllegalStateException("Insert failed, no rows affected");
            }

            System.out.println("INSERTED USER");
            Main.mngr.initializeNewUserInventory(id);
            Main.mngr.insertAchievements(id);
            Main.user = Main.mngr.getUser(id);

            try {
                ApiClient.postAuth("/auth/users/heartbeat",
                    "{\"userId\":" + id + "}");
            } catch (Exception ignored) {}

            String syncBody = "{\"userId\":"        + id               + ","
                            + "\"username\":\""     + username         + "\","
                            + "\"email\":\""        + email            + "\","
                            + "\"passwordHash\":\"" + sha256(password) + "\"}";
            ApiClient.postAuth("/auth/users/sync", syncBody);

            navigateToMain();

        } catch (Exception e) {
            e.printStackTrace();
            showRegisterError("Registration failed. Username or email may already exist.");
        }
    }

    private void navigateToMain() throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("../fxml/Index.fxml"));
        Main.primarStageStatic.setTitle("StudyMore");
        Main.primarStageStatic.setScene(new Scene(root, 1200, 800));
        Main.primarStageStatic.show();
    }

    private void showLoginError(String message) {
        if (loginErrorLabel != null) {
            loginErrorLabel.setText(message);
            loginErrorLabel.setVisible(true);
        } else {
            System.out.println("Login error: " + message);
        }
    }

    private void showRegisterError(String message) {
        if (registerErrorLabel != null) {
            registerErrorLabel.setText(message);
            registerErrorLabel.setVisible(true);
        } else {
            System.out.println("Register error: " + message);
        }
    }

    public static String sha256(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(digest.length * 2);
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }
}