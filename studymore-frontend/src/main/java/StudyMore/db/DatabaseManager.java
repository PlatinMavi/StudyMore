package StudyMore.db;

import java.sql.*;

public class DatabaseManager {
    private static final String DB_URL = "jdbc:sqlite:studymore_database.db";
    private Connection connection;

    public DatabaseManager() {
        initilizeDB();
    }

    private void initilizeDB() {
        try {
            connection = DriverManager.getConnection(DB_URL);
            createTables();
            System.out.println("Database connected successfully!");
        } catch (SQLException e) {
            System.err.println("Failed to connect to database: " + e.getMessage());
        }
    }

    private void createTables() {
        String createTasksTable = """
            CREATE TABLE IF NOT EXISTS tasks (
                id INTEGER PRIMARY KEY,
                user_id INTEGER NOT NULL,
                title TEXT NOT NULL,
                content TEXT,
                srs_enabled INTEGER NOT NULL DEFAULT 0,
                next_recall_date TEXT,
                is_complete INTEGER NOT NULL DEFAULT 0,
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                FOREIGN KEY (user_id) REFERENCES users(id)
            );
        """;

        String createMultipliersTable = """
            CREATE TABLE IF NOT EXISTS multipliers (
                id INTEGER PRIMARY KEY,
                user_id INTEGER NOT NULL UNIQUE,
                current_value REAL NOT NULL DEFAULT 1.0,
                max_value REAL NOT NULL DEFAULT 5.0,
                increment_interval INTEGER NOT NULL DEFAULT 0,
                cooldown_interval INTEGER NOT NULL DEFAULT 0,
                last_active_time TIMESTAMP,
                FOREIGN KEY (user_id) REFERENCES users(id)
            );
        """;

        String createCosmeticsTable = """
            CREATE TABLE IF NOT EXISTS cosmetics (
                id INTEGER PRIMARY KEY,
                name TEXT NOT NULL,
                type TEXT NOT NULL,
                price INTEGER NOT NULL DEFAULT 0,
                image_path TEXT,
                description TEXT
            );
        """;

        String createInventoryTable = """
            CREATE TABLE IF NOT EXISTS inventory (
                id INTEGER PRIMARY KEY,
                user_id INTEGER NOT NULL UNIQUE,
                FOREIGN KEY (user_id) REFERENCES users(id)
            );
        """;

        String createInventoryItemsTable = """
            CREATE TABLE IF NOT EXISTS inventory_items (
                inventory_id INTEGER NOT NULL,
                cosmetic_id INTEGER NOT NULL,
                is_equipped INTEGER NOT NULL DEFAULT 0,
                PRIMARY KEY (inventory_id, cosmetic_id),
                FOREIGN KEY (inventory_id) REFERENCES inventory(id),
                FOREIGN KEY (cosmetic_id) REFERENCES cosmetics(id)
            );
        """;

        String createMascotCatsTable = """
            CREATE TABLE IF NOT EXISTS mascot_cats (
                id INTEGER PRIMARY KEY,
                user_id INTEGER NOT NULL UNIQUE,
                equipped_skin_id INTEGER,
                equipped_house_id INTEGER,
                equipped_hat_id INTEGER,
                FOREIGN KEY (user_id) REFERENCES users(id),
                FOREIGN KEY (equipped_skin_id) REFERENCES cosmetics(id),
                FOREIGN KEY (equipped_house_id) REFERENCES cosmetics(id),
                FOREIGN KEY (equipped_hat_id) REFERENCES cosmetics(id)
            );
        """;

        String createSettingsTable = """
            CREATE TABLE IF NOT EXISTS settings (
                id INTEGER PRIMARY KEY,
                user_id INTEGER NOT NULL UNIQUE,
                dark_mode INTEGER NOT NULL DEFAULT 0,
                lock_in_mode INTEGER NOT NULL DEFAULT 0,
                show_mascot INTEGER NOT NULL DEFAULT 1,
                study_time INTEGER NOT NULL DEFAULT 25,
                short_break INTEGER NOT NULL DEFAULT 5,
                long_break INTEGER NOT NULL DEFAULT 15,
                long_break_after INTEGER NOT NULL DEFAULT 4,
                start_sound INTEGER NOT NULL DEFAULT 1,
                break_alert INTEGER NOT NULL DEFAULT 1,
                popups INTEGER NOT NULL DEFAULT 1,
                FOREIGN KEY (user_id) REFERENCES users(id)
            );
        """;

        String createAchievementsTable = """
            CREATE TABLE IF NOT EXISTS achievements (
                id INTEGER PRIMARY KEY,
                title TEXT NOT NULL,
                description TEXT,
                type TEXT NOT NULL,
                target_value INTEGER NOT NULL DEFAULT 0,
                reward INTEGER NOT NULL DEFAULT 0,
                icon_path TEXT
            );
        """;

        String createUserAchievementsTable = """
            CREATE TABLE IF NOT EXISTS user_achievements (
                id INTEGER PRIMARY KEY,
                user_id INTEGER NOT NULL,
                achievement_id INTEGER NOT NULL,
                progress INTEGER NOT NULL DEFAULT 0,
                is_completed INTEGER NOT NULL DEFAULT 0,
                completed_at TIMESTAMP,
                UNIQUE (user_id, achievement_id),
                FOREIGN KEY (user_id) REFERENCES users(id),
                FOREIGN KEY (achievement_id) REFERENCES achievements(id)
            );
        """;

        String createStudyGroupsTable = """
            CREATE TABLE IF NOT EXISTS study_groups (
                id INTEGER PRIMARY KEY,
                title TEXT NOT NULL,
                host_id INTEGER NOT NULL,
                study_goal INTEGER NOT NULL DEFAULT 0,
                max_members INTEGER NOT NULL DEFAULT 10,
                is_active INTEGER NOT NULL DEFAULT 1,
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                FOREIGN KEY (host_id) REFERENCES users(id)
            );
        """;

        String createStudyGroupMembersTable = """
            CREATE TABLE IF NOT EXISTS study_group_members (
                group_id INTEGER NOT NULL,
                user_id INTEGER NOT NULL,
                PRIMARY KEY (group_id, user_id),
                FOREIGN KEY (group_id) REFERENCES study_groups(id),
                FOREIGN KEY (user_id) REFERENCES users(id)
            );
        """;

        String createFriendRequestsTable = """
            CREATE TABLE IF NOT EXISTS friend_requests (
                id INTEGER PRIMARY KEY,
                sender_id INTEGER NOT NULL,
                receiver_id INTEGER NOT NULL,
                status TEXT NOT NULL DEFAULT 'PENDING',
                sent_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                UNIQUE (sender_id, receiver_id),
                FOREIGN KEY (sender_id) REFERENCES users(id),
                FOREIGN KEY (receiver_id) REFERENCES users(id)
            );
        """;

        String createFriendsTable = """
            CREATE TABLE IF NOT EXISTS friends (
                user_id INTEGER NOT NULL,
                friend_id INTEGER NOT NULL,
                PRIMARY KEY (user_id, friend_id),
                FOREIGN KEY (user_id) REFERENCES users(id),
                FOREIGN KEY (friend_id) REFERENCES users(id)
            );
        """;

        String createUserStatsTable = """
            CREATE TABLE IF NOT EXISTS user_stats (
                user_id INTEGER PRIMARY KEY,
                rank TEXT NOT NULL DEFAULT 'BRONZE',
                rating INTEGER NOT NULL DEFAULT 0,
                coin_balance INTEGER NOT NULL DEFAULT 0,
                study_streak INTEGER NOT NULL DEFAULT 0,
                total_study_time INTEGER NOT NULL DEFAULT 0,
                daily_study_time INTEGER NOT NULL DEFAULT 0,
                FOREIGN KEY (user_id) REFERENCES users(id)
            );
        """;

        String createUsersTable = """
            CREATE TABLE IF NOT EXISTS users (
                id INTEGER PRIMARY KEY,
                username TEXT NOT NULL UNIQUE,
                email TEXT NOT NULL UNIQUE,
                password_hash TEXT NOT NULL,
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
            );
        """;

        String createSessionsTable = """
            CREATE TABLE IF NOT EXISTS sessions (
                id INTEGER PRIMARY KEY,
                user_id INTEGER NOT NULL,
                start_time TIMESTAMP NOT NULL,
                end_time TIMESTAMP,
                multiplier_value REAL NOT NULL DEFAULT 1.0,
                coins_earned INTEGER NOT NULL DEFAULT 0,
                duration INTEGER NOT NULL DEFAULT 0,
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                FOREIGN KEY (user_id) REFERENCES users(id)
            );
        """;


        try (Statement stmt = connection.createStatement()) {
            stmt.execute(createUsersTable);
            stmt.execute(createUserStatsTable);
            stmt.execute(createSessionsTable);
            stmt.execute(createTasksTable);
            stmt.execute(createMultipliersTable);
            stmt.execute(createCosmeticsTable);
            stmt.execute(createInventoryTable);
            stmt.execute(createInventoryItemsTable);
            stmt.execute(createMascotCatsTable);
            stmt.execute(createSettingsTable);
            stmt.execute(createAchievementsTable);
            stmt.execute(createUserAchievementsTable);
            stmt.execute(createStudyGroupsTable);
            stmt.execute(createStudyGroupMembersTable);
            stmt.execute(createFriendRequestsTable);
            stmt.execute(createFriendsTable);

            System.out.println("Database Succesfuly initilized");
        } catch (SQLException e) {
            System.err.println("Error creating tables: " + e.getMessage());
        }
    }

    public Connection getConnection() {
        return connection;
    }
    
    public void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            System.err.println("Error closing database connection: " + e.getMessage());
        }
    }

}
