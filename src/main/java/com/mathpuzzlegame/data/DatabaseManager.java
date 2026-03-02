package com.mathpuzzlegame.data;

import com.mathpuzzlegame.model.Difficulty;
import com.mathpuzzlegame.model.GameRecord;
import com.mathpuzzlegame.model.User;
import com.mathpuzzlegame.model.UserScore;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class DatabaseManager {

    private final String url;

    public DatabaseManager(String url) {
        this.url = url;
        initSchema();
    }

    private void initSchema() {
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {

            stmt.execute("""
                    CREATE TABLE IF NOT EXISTS users (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        username TEXT UNIQUE NOT NULL,
                        password_hash TEXT NOT NULL,
                        created_at TEXT NOT NULL
                    )
                    """);

            stmt.execute("""
                    CREATE TABLE IF NOT EXISTS games (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        user_id INTEGER NOT NULL,
                        difficulty TEXT NOT NULL,
                        score INTEGER NOT NULL,
                        total_questions INTEGER NOT NULL,
                        played_at TEXT NOT NULL,
                        FOREIGN KEY(user_id) REFERENCES users(id)
                    )
                    """);
            stmt.execute("""
                    CREATE TABLE IF NOT EXISTS scores (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        username TEXT NOT NULL,
                        score INTEGER NOT NULL,
                        time_taken_seconds INTEGER NOT NULL,
                        played_at TEXT NOT NULL
                    )
                    """);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(url);
    }

    private String hashPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hashedBytes = md.digest(password.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : hashedBytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Error hashing password", e);
        }
    }

    // Sample-style APIs

    public boolean register(String username, String password) throws SQLException {
        if (password == null || password.length() < 8) {
            return false;
        }
        String hashedPassword = hashPassword(password);
        String sql = "INSERT INTO users (username, password_hash, created_at) VALUES (?, ?, ?)";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            pstmt.setString(2, hashedPassword);
            pstmt.setString(3, LocalDateTime.now().toString());
            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            // likely duplicate username or other error
            return false;
        }
    }

    public boolean login(String username, String password) throws SQLException {
        String hashedPassword = hashPassword(password);
        String sql = "SELECT id FROM users WHERE username = ? AND password_hash = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            pstmt.setString(2, hashedPassword);
            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next();
            }
        }
    }

    public void saveScore(String username, int score, int timeTakenSeconds) throws SQLException {
        String sql = "INSERT INTO scores (username, score, time_taken_seconds, played_at) VALUES (?, ?, ?, ?)";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, username);
            stmt.setInt(2, score);
            stmt.setInt(3, timeTakenSeconds);
            stmt.setString(4, LocalDateTime.now().toString());
            stmt.executeUpdate();
        }
    }

    public List<UserScore> getTopScoresByTime() {
        List<UserScore> topScores = new ArrayList<>();
        String query = "SELECT username, score, time_taken_seconds, played_at FROM scores ORDER BY time_taken_seconds ASC LIMIT 10";
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                topScores.add(new UserScore(
                        rs.getString("username"),
                        rs.getInt("score"),
                        rs.getInt("time_taken_seconds"),
                        rs.getString("played_at")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return topScores;
    }

    public List<UserScore> getTopScores() {
        List<UserScore> topScores = new ArrayList<>();
        String query = "SELECT username, score, time_taken_seconds, played_at FROM scores ORDER BY score DESC LIMIT 10";
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                topScores.add(new UserScore(
                        rs.getString("username"),
                        rs.getInt("score"),
                        rs.getInt("time_taken_seconds"),
                        rs.getString("played_at")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return topScores;
    }

    public int getUserScore(String username) {
        String query = "SELECT MAX(score) AS best_score FROM scores WHERE username = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, username);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("best_score");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public Optional<User> findUserByUsername(String username) {
        String sql = "SELECT id, username, password_hash, created_at FROM users WHERE username = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    User user = new User(
                            rs.getInt("id"),
                            rs.getString("username"),
                            rs.getString("password_hash"),
                            rs.getString("created_at")
                    );
                    return Optional.of(user);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }

    public boolean createUser(String username, String passwordHash) {
        String sql = "INSERT INTO users(username, password_hash, created_at) VALUES(?,?,?)";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            ps.setString(2, passwordHash);
            ps.setString(3, LocalDateTime.now().toString());
            ps.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public void saveGameRecord(int userId, Difficulty difficulty, int score, int totalQuestions) {
        String sql = "INSERT INTO games(user_id, difficulty, score, total_questions, played_at) VALUES(?,?,?,?,?)";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setString(2, difficulty.name());
            ps.setInt(3, score);
            ps.setInt(4, totalQuestions);
            ps.setString(5, LocalDateTime.now().toString());
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public int getTotalGamesForUser(int userId) {
        String sql = "SELECT COUNT(*) FROM games WHERE user_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public int getTotalGamesForUserByDifficulty(int userId, Difficulty difficulty) {
        String sql = "SELECT COUNT(*) FROM games WHERE user_id = ? AND difficulty = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setString(2, difficulty.name());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public List<GameRecord> getGameRecordsForUser(int userId) {
        List<GameRecord> records = new ArrayList<>();
        String sql = "SELECT id, user_id, difficulty, score, total_questions, played_at FROM games WHERE user_id = ? ORDER BY played_at DESC";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    GameRecord record = new GameRecord(
                            rs.getInt("id"),
                            rs.getInt("user_id"),
                            Difficulty.valueOf(rs.getString("difficulty")),
                            rs.getInt("score"),
                            rs.getInt("total_questions"),
                            rs.getString("played_at")
                    );
                    records.add(record);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return records;
    }

    public int getHighestScoreForUser(int userId) {
        String sql = "SELECT MAX(score) FROM games WHERE user_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }
}