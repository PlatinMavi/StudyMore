package StudyMore.controllers;

import StudyMore.Main;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class FriendsController {
 
    @FXML private TextField searchField;
    @FXML private VBox searchResultsContainer;
    @FXML private VBox pendingRequestsContainer;
    @FXML private VBox friendsListContainer;
    @FXML private Label statusLabel;
    @FXML private Label friendCountLabel;
 
    @FXML
    public void initialize() {
        refresh();
    }
    @FXML
    private void onSearch() {
        String keyword = searchField.getText().trim();
        if (keyword.isEmpty()) return;
        searchResultsContainer.getChildren().clear();
 
        String query = """
                SELECT id, username, email FROM users
                WHERE LOWER(username) LIKE LOWER(?)
                AND id != ?
                """;
 
        try (PreparedStatement stmt = Main.mngr.getConnection().prepareStatement(query)) {
            stmt.setString(1, "%" + keyword + "%");
            stmt.setLong(2, Main.user.getUserId());
 
            try (ResultSet rs = stmt.executeQuery()) {
                boolean found = false;
                while (rs.next()) {
                    found = true;
                    long foundId   = rs.getLong("id");
                    String foundName = rs.getString("username");
                    searchResultsContainer.getChildren().add(buildSearchResultRow(foundId, foundName));
                }
                if (!found) {
                    searchResultsContainer.getChildren().add(emptyLabel("No users found."));
                }
            }
        } catch (SQLException e) {
            setStatus("Search error: " + e.getMessage());
        }
    }
    public void refresh() {
        loadPendingRequests();
        loadFriendsList();
    }
 
    private void loadPendingRequests() {
        pendingRequestsContainer.getChildren().clear();
 
        String query = """
                SELECT fr.id AS req_id, u.id AS sender_id, u.username AS sender_name
                FROM friend_requests fr
                JOIN users u ON fr.sender_id = u.id
                WHERE fr.receiver_id = ? AND fr.status = 'PENDING'
                ORDER BY fr.sent_at DESC
                """;
 
        try (PreparedStatement stmt = Main.mngr.getConnection().prepareStatement(query)) {
            stmt.setLong(1, Main.user.getUserId());
 
            try (ResultSet rs = stmt.executeQuery()) {
                boolean found = false;
                while (rs.next()) {
                    found = true;
                    long reqId      = rs.getLong("req_id");
                    String senderName = rs.getString("sender_name");
                    pendingRequestsContainer.getChildren().add(buildRequestRow(reqId, senderName));
                }
                if (!found) {
                    pendingRequestsContainer.getChildren().add(emptyLabel("No pending requests."));
                }
            }
        } catch (SQLException e) {
            setStatus("Error loading requests: " + e.getMessage());
        }
    }
 
    private void loadFriendsList() {
        friendsListContainer.getChildren().clear();
 
        String query = """
                SELECT u.id, u.username, us.rank, us.coin_balance
                FROM friends f
                JOIN users u ON f.friend_id = u.id
                LEFT JOIN user_stats us ON u.id = us.user_id
                WHERE f.user_id = ?
                ORDER BY u.username ASC
                """;
 
        try (PreparedStatement stmt = Main.mngr.getConnection().prepareStatement(query)) {
            stmt.setLong(1, Main.user.getUserId());
 
            List<HBox> rows = new ArrayList<>();
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    String username = rs.getString("username");
                    String rank     = rs.getString("rank") != null ? rs.getString("rank") : "BRONZE";
                    int coins       = rs.getInt("coin_balance");
                    rows.add(buildFriendRow(username, rank, coins));
                }
            }
 
            friendCountLabel.setText("Friends (" + rows.size() + ")");
            if (rows.isEmpty()) {
                friendsListContainer.getChildren().add(emptyLabel("No friends yet. Search for users above!"));
            } else {
                friendsListContainer.getChildren().addAll(rows);
            }
        } catch (SQLException e) {
            setStatus("Error loading friends: " + e.getMessage());
        }
    }
    private void sendRequest(long receiverId) {
        String checkQuery = """
                SELECT id FROM friend_requests
                WHERE (sender_id = ? AND receiver_id = ?)
                   OR (sender_id = ? AND receiver_id = ?)
                """;
        try (PreparedStatement check = Main.mngr.getConnection().prepareStatement(checkQuery)) {
            check.setLong(1, Main.user.getUserId());
            check.setLong(2, receiverId);
            check.setLong(3, receiverId);
            check.setLong(4, Main.user.getUserId());
            try (ResultSet rs = check.executeQuery()) {
                if (rs.next()) {
                    setStatus("Request already exists.");
                    return;
                }
            }
        } catch (SQLException e) {
            setStatus("Error: " + e.getMessage());
            return;
        }
 
        String insert = """
                INSERT INTO friend_requests (sender_id, receiver_id, status, sent_at)
                VALUES (?, ?, 'PENDING', CURRENT_TIMESTAMP)
                """;
        try (PreparedStatement stmt = Main.mngr.getConnection().prepareStatement(insert)) {
            stmt.setLong(1, Main.user.getUserId());
            stmt.setLong(2, receiverId);
            stmt.executeUpdate();
            setStatus("Friend request sent!");
        } catch (SQLException e) {
            setStatus("Error sending request: " + e.getMessage());
        }
    }
 
    private void acceptRequest(long requestId) {
        String getReq = "SELECT sender_id FROM friend_requests WHERE id = ?";
        try (PreparedStatement stmt = Main.mngr.getConnection().prepareStatement(getReq)) {
            stmt.setLong(1, requestId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (!rs.next()) return;
                long senderId = rs.getLong("sender_id");
                long myId     = Main.user.getUserId();
 
                try (PreparedStatement upd = Main.mngr.getConnection().prepareStatement(
                        "UPDATE friend_requests SET status = 'ACCEPTED' WHERE id = ?")) {
                    upd.setLong(1, requestId);
                    upd.executeUpdate();
                }
                String insertFriend = """
                        INSERT INTO friends (user_id, friend_id)
                        VALUES (?, ?) ON CONFLICT DO NOTHING
                        """;
                try (PreparedStatement ins = Main.mngr.getConnection().prepareStatement(insertFriend)) {
                    ins.setLong(1, myId);     ins.setLong(2, senderId); ins.executeUpdate();
                    ins.setLong(1, senderId); ins.setLong(2, myId);     ins.executeUpdate();
                }
 
                setStatus("Friend added!");
                refresh();
            }
        } catch (SQLException e) {
            setStatus("Error accepting: " + e.getMessage());
        }
    }
 
    private void denyRequest(long requestId) {
        try (PreparedStatement stmt = Main.mngr.getConnection().prepareStatement(
                "UPDATE friend_requests SET status = 'DENIED' WHERE id = ?")) {
            stmt.setLong(1, requestId);
            stmt.executeUpdate();
            setStatus("Request denied.");
            refresh();
        } catch (SQLException e) {
            setStatus("Error denying: " + e.getMessage());
        }
    }
    private HBox buildSearchResultRow(long userId, String username) {
        Label nameLbl = new Label(username);
        nameLbl.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 13px;");
        HBox.setHgrow(nameLbl, Priority.ALWAYS);
 
        Button addBtn = yellowButton("+ Add Friend");
        addBtn.setOnAction(e -> sendRequest(userId));
 
        HBox row = styledRow();
        row.getChildren().addAll(avatar(username), nameLbl, addBtn);
        return row;
    }
 
    private HBox buildRequestRow(long requestId, String senderName) {
        Label nameLbl = new Label("From: " + senderName);
        nameLbl.setStyle("-fx-text-fill: white; -fx-font-size: 13px;");
        HBox.setHgrow(nameLbl, Priority.ALWAYS);
 
        Button acceptBtn = yellowButton("✓ Accept");
        Button denyBtn   = ghostButton("✗ Deny");
        acceptBtn.setOnAction(e -> acceptRequest(requestId));
        denyBtn.setOnAction(e   -> denyRequest(requestId));
 
        HBox row = styledRow();
        row.getChildren().addAll(avatar(senderName), nameLbl, acceptBtn, denyBtn);
        return row;
    }
 
    private HBox buildFriendRow(String username, String rank, int coins) {
        Label nameLbl  = new Label(username);
        nameLbl.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 13px;");
 
        Label rankLbl  = new Label(rank);
        rankLbl.setStyle("-fx-text-fill: #888; -fx-font-size: 11px;");
 
        VBox info = new VBox(2, nameLbl, rankLbl);
        HBox.setHgrow(info, Priority.ALWAYS);
 
        Label coinsLbl = new Label("🪙 " + coins);
        coinsLbl.setStyle("-fx-text-fill: #fbbf24; -fx-font-weight: bold; -fx-font-size: 12px;");
 
        HBox row = styledRow();
        row.getChildren().addAll(avatar(username), info, coinsLbl);
        return row;
    }
 
    // style helpers
    private HBox styledRow() {
        HBox row = new HBox(10);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(10, 14, 10, 14));
        row.setStyle("-fx-background-color: #111111; -fx-border-color: #262626; " +
                     "-fx-border-width: 1; -fx-background-radius: 4; -fx-border-radius: 4;");
        return row;
    }
 
    private Label avatar(String name) {
        Label av = new Label(name.isEmpty() ? "?" : String.valueOf(name.charAt(0)).toUpperCase());
        av.setPrefSize(32, 32);
        av.setMinSize(32, 32);
        av.setAlignment(Pos.CENTER);
        av.setStyle("-fx-background-color: #fbbf24; -fx-text-fill: #0a0a0a; " +
                    "-fx-font-weight: bold; -fx-background-radius: 16;");
        return av;
    }
 
    private Button yellowButton(String text) {
        Button btn = new Button(text);
        btn.setStyle("-fx-background-color: transparent; -fx-text-fill: #fbbf24; " +
                     "-fx-border-color: #fbbf24; -fx-border-width: 1; -fx-border-radius: 4; " +
                     "-fx-background-radius: 4; -fx-font-weight: bold; -fx-padding: 5 12; -fx-cursor: hand;");
        return btn;
    }
 
    private Button ghostButton(String text) {
        Button btn = new Button(text);
        btn.setStyle("-fx-background-color: transparent; -fx-text-fill: #888; " +
                     "-fx-border-color: #444; -fx-border-width: 1; -fx-border-radius: 4; " +
                     "-fx-background-radius: 4; -fx-padding: 5 12; -fx-cursor: hand;");
        return btn;
    }
 
    private Label emptyLabel(String text) {
        Label l = new Label(text);
        l.setStyle("-fx-text-fill: #555; -fx-font-size: 12px;");
        l.setPadding(new Insets(10, 0, 0, 4));
        return l;
    }
 
    private void setStatus(String msg) {
        if (statusLabel != null) statusLabel.setText(msg);
    }
}
