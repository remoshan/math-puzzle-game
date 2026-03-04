package com.mathpuzzlegame.model;

public enum Difficulty {
    BEGINNER("Beginner", 4, 6, 120, "Relaxed 4×4 board, generous hints"),
    INTERMEDIATE("Intermediate", 6, 4, 90, "6×6 board, moderate hints"),
    ADVANCED("Advanced", 8, 3, 60, "Challenging 8×8 board, few hints");

    private final String displayName;
    private final int gridSize;
    private final int initialHints;
    private final int totalTimeSeconds;
    private final String description;

    Difficulty(String displayName,
               int gridSize,
               int initialHints,
               int totalTimeSeconds,
               String description) {
        this.displayName = displayName;
        this.gridSize = gridSize;
        this.initialHints = initialHints;
        this.totalTimeSeconds = totalTimeSeconds;
        this.description = description;
    }

    public String getDisplayName() {
        return displayName;
    }

    public int getGridSize() {
        return gridSize;
    }

    public int getInitialHints() {
        return initialHints;
    }

    public int getTotalTimeSeconds() {
        return totalTimeSeconds;
    }

    public String getDescription() {
        return description;
    }
}