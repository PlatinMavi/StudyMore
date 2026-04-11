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

    @FXML
    private TextField loginUsernameField;
    @FXML
    private PasswordField loginPasswordField;
    @FXML
    private TextField registerUsernameField;
    @FXML
    private TextField registerEmailField;
    @FXML
    private PasswordField registerPasswordField;
    @FXML
    private Label loginErrorLabel;
    @FXML
    private Label registerErrorLabel;

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

        String loginBody = "{\"username\":\"" + username + "\","
                + "\"passwordHash\":\"" + sha256(password) + "\"}";

        try {
            String loginResponse = ApiClient.postAuth("/auth/login", loginBody);

            if (loginResponse != null && loginResponse.contains("\"userId\"")) {
                org.json.JSONObject userJson = new org.json.JSONObject(loginResponse);
                long serverUserId = userJson.getLong("userId");
                String email = userJson.optString("email", "");

                // Ensure base user exists locally
                try (java.sql.PreparedStatement check = Main.mngr.getConnection().prepareStatement(
                        "SELECT id FROM users WHERE id = ?")) {
                    check.setLong(1, serverUserId);
                    try (java.sql.ResultSet rs = check.executeQuery()) {
                        if (!rs.next()) {
                            try (java.sql.PreparedStatement ins = Main.mngr.getConnection().prepareStatement(
                                    "INSERT INTO users(id, username, email, password_hash) VALUES(?,?,?,?)")) {
                                ins.setLong(1, serverUserId);
                                ins.setString(2, username);
                                ins.setString(3, email);
                                ins.setString(4, sha256(password));
                                ins.executeUpdate();
                            }
                            // Initialize default fallbacks in case sync fails
                            Main.mngr.initializeNewUserInventory(serverUserId);
                            Main.mngr.insertAchievements(serverUserId);
                            Main.mngr.saveSettings(serverUserId, new StudyMore.models.Settings());
                        }
                    }
                }

                // --- PULL SYNC DATA FROM SERVER ---
                try {
                    System.out.println("Pulling sync data for user " + serverUserId + "...");

                    String syncResponse = ApiClient.get("/sync/pull/" + serverUserId);

                    if (syncResponse != null && !syncResponse.isEmpty() && !syncResponse.contains("\"error\"")) {
                        org.json.JSONObject syncPayload = new org.json.JSONObject(syncResponse);
                        Main.mngr.restoreFromSyncPayload(syncPayload);
                    } else {
                        System.out.println("No sync data found or error pulling data.");
                    }
                } catch (Exception e) {
                    System.err.println("Could not pull sync data during login: " + e.getMessage());
                }
                // ----------------------------------

                Main.user = Main.mngr.getUser(serverUserId);

                try {
                    ApiClient.postAuth("/auth/users/heartbeat",
                            "{\"userId\":" + serverUserId + "}");
                } catch (Exception ignored) {
                }

                Main.settings = Main.mngr.getSettings(serverUserId);

                Main.startSyncLoop();
                navigateToMain();

            } else {
                showLoginError("Invalid username or password.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            showLoginError("Login failed. Check your connection.");
        }
    }

    @FXML
    private void handleRegister(ActionEvent event) {
        String username = registerUsernameField.getText().trim();
        String email = registerEmailField.getText().trim();
        String password = registerPasswordField.getText();

        if (username.isEmpty() || email.isEmpty() || password.isEmpty()) {
            showRegisterError("Please fill in all fields.");
            return;
        }

        // Generate the Snowflake ID locally first
        long generatedId = SnowflakeIDGenerator.generate();

        // Send the registration request to the backend including the new ID
        String requestBody = "{\"userId\":\"" + generatedId + "\","
                + "\"username\":\"" + username + "\","
                + "\"email\":\"" + email + "\","
                + "\"password\":\"" + password + "\"}";

        try {
            String response = ApiClient.postAuth("/auth/register", requestBody);

            if (response == null || response.contains("\"error\"") || response.contains("taken")
                    || response.contains("already in use")) {
                showRegisterError("Registration failed. Username or email may already exist.");
                return;
            }

            // The backend successfully registered the user. Now save locally.
            final String sql = """
                    INSERT INTO users(id, username, email, password_hash)
                    VALUES (?, ?, ?, ?)
                    """;

            try (java.sql.PreparedStatement pstmt = Main.mngr.getConnection().prepareStatement(sql)) {
                pstmt.setLong(1, generatedId);
                pstmt.setString(2, username);
                pstmt.setString(3, email);
                pstmt.setString(4, sha256(password));
                int rows = pstmt.executeUpdate();

                if (rows != 1) {
                    throw new IllegalStateException("Insert failed locally, no rows affected");
                }

                System.out.println("User successfully registered on server and saved locally.");

                // Initialize local states
                Main.mngr.initializeNewUserInventory(generatedId);
                Main.mngr.insertAchievements(generatedId);
                Main.mngr.saveSettings(generatedId, new StudyMore.models.Settings());
                Main.settings = Main.mngr.getSettings(generatedId);
                Main.user = Main.mngr.getUser(generatedId);

                try {
                    ApiClient.postAuth("/auth/users/heartbeat", "{\"userId\":" + generatedId + "}");
                } catch (Exception ignored) {
                }

                navigateToMain();

            } catch (Exception e) {
                e.printStackTrace();
                showRegisterError("Local database error during registration.");
            }

        } catch (Exception e) {
            e.printStackTrace();
            showRegisterError("Could not connect to the server.");
        }
    }

    private void navigateToMain() throws Exception {
        Main.startSyncLoop();
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