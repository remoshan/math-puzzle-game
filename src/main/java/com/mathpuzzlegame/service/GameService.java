package com.mathpuzzlegame.service;

import com.mathpuzzlegame.data.DatabaseManager;
import com.mathpuzzlegame.model.Difficulty;
import com.mathpuzzlegame.model.User;
import com.mathpuzzlegame.model.UserScore;

import java.sql.SQLException;
import java.util.List;

public class GameService {

    private final DatabaseManager databaseManager;

    private User currentUser;
    private Difficulty currentDifficulty = Difficulty.BEGINNER;

    public GameService(DatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
    }

    public void setCurrentUser(User user) {
        this.currentUser = user;
    }

    public User getCurrentUser() {
        return currentUser;
    }

    public Difficulty getCurrentDifficulty() {
        return currentDifficulty;
    }

    public void setCurrentDifficulty(Difficulty difficulty) {
        this.currentDifficulty = difficulty;
    }

    public int getGridSize() {
        return currentDifficulty.getGridSize();
    }

    public int getInitialHints() {
        return currentDifficulty.getInitialHints();
    }

    public int getTotalTimeSeconds() {
        return currentDifficulty.getTotalTimeSeconds();
    }

    public void saveSessionResult(int score, int timeTakenSeconds) {
        if (currentUser == null) {
            return;
        }
        try {
            databaseManager.saveScore(currentUser.getUsername(), score, timeTakenSeconds);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // Also persist into the structured games table so that
        // per-user and per-difficulty statistics stay accurate.
        int totalPairs = (getGridSize() * getGridSize()) / 2;
        databaseManager.saveGameRecord(currentUser.getId(), currentDifficulty, score, totalPairs);
    }

    public int getCurrentUserBestScore() {
        if (currentUser == null) {
            return 0;
        }
        return databaseManager.getUserScore(currentUser.getUsername());
    }

    public List<UserScore> getTopScoresByTime() {
        return databaseManager.getTopScoresByTime();
    }

    public List<UserScore> getTopScores() {
        return databaseManager.getTopScores();
    }

    // Convenience wrappers for HomeView / stats panels

    public int getTotalGamesForCurrentUser() {
        if (currentUser == null) {
            return 0;
        }
        return databaseManager.getTotalGamesForUser(currentUser.getId());
    }

    public int getTotalGamesForCurrentUser(Difficulty difficulty) {
        if (currentUser == null) {
            return 0;
        }
        return databaseManager.getTotalGamesForUserByDifficulty(currentUser.getId(), difficulty);
    }
}