package com.mathpuzzlegame.model;

public class GameRecord {
    private final int id;
    private final int userId;
    private final Difficulty difficulty;
    private final int score;
    private final int totalQuestions;
    private final String playedAt;

    public GameRecord(int id, int userId, Difficulty difficulty, int score, int totalQuestions, String playedAt) {
        this.id = id;
        this.userId = userId;
        this.difficulty = difficulty;
        this.score = score;
        this.totalQuestions = totalQuestions;
        this.playedAt = playedAt;
    }

    public int getId() {
        return id;
    }

    public int getUserId() {
        return userId;
    }

    public Difficulty getDifficulty() {
        return difficulty;
    }

    public int getScore() {
        return score;
    }

    public int getTotalQuestions() {
        return totalQuestions;
    }

    public String getPlayedAt() {
        return playedAt;
    }
}