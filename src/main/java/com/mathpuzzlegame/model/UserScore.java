package com.mathpuzzlegame.model;

public class UserScore {
    private final String username;
    private final int score;
    private final int timeTakenSeconds;
    private final String timestamp;

    public UserScore(String username, int score, int timeTakenSeconds, String timestamp) {
        this.username = username;
        this.score = score;
        this.timeTakenSeconds = timeTakenSeconds;
        this.timestamp = timestamp;
    }

    public String getUsername() {
        return username;
    }

    public int getScore() {
        return score;
    }

    public int getTimeTakenSeconds() {
        return timeTakenSeconds;
    }

    public String getTimestamp() {
        return timestamp;
    }
}