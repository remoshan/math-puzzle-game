package com.mathpuzzlegame.ui;

import com.mathpuzzlegame.service.GameService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class GameView extends JPanel {

    private final AppFrame appFrame;
    private final GameService gameService;

    private final JLabel difficultyLabel = new JLabel();
    private final JLabel timerLabel = new JLabel();
    private final JLabel scoreLabel = new JLabel("Score: 0");
    private final JLabel hintsLabel = new JLabel("Hints: 0");

    private javax.swing.Timer timer;
    private int timeRemaining;
    private int score;
    private int hintsLeft;
    private int totalTimeSeconds;

    private final List<JButton> cardButtons = new ArrayList<>();
    private final List<Integer> cardValues = new ArrayList<>();
    private JButton firstRevealed;
    private JButton secondRevealed;

    public GameView(AppFrame appFrame, GameService gameService) {
        this.appFrame = appFrame;
        this.gameService = gameService;

        setLayout(new BorderLayout());
        setBorder(new EmptyBorder(20, 20, 20, 20));

        JPanel topBar = new JPanel(new BorderLayout());
        difficultyLabel.setFont(difficultyLabel.getFont().deriveFont(Font.PLAIN, 16f));
        timerLabel.setFont(timerLabel.getFont().deriveFont(Font.BOLD, 18f));
        scoreLabel.setFont(scoreLabel.getFont().deriveFont(Font.PLAIN, 16f));
        hintsLabel.setFont(hintsLabel.getFont().deriveFont(Font.PLAIN, 16f));

        JPanel leftTop = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        leftTop.setOpaque(false);
        leftTop.add(difficultyLabel);

        JPanel rightTop = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        rightTop.setOpaque(false);
        rightTop.add(scoreLabel);
        rightTop.add(hintsLabel);

        topBar.add(leftTop, BorderLayout.WEST);
        topBar.add(timerLabel, BorderLayout.CENTER);
        topBar.add(rightTop, BorderLayout.EAST);

        JPanel gridPanel = new JPanel();
        gridPanel.setBorder(new EmptyBorder(32, 32, 32, 32));

        JButton hintButton = new JButton("Use Hint");
        hintButton.putClientProperty("JButton.buttonType", "roundRect");
        JButton endGameButton = new JButton("End Game");
        endGameButton.putClientProperty("JButton.buttonType", "roundRect");

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottom.add(hintButton);
        bottom.add(endGameButton);

        setLayout(new BorderLayout());
        add(topBar, BorderLayout.NORTH);
        add(gridPanel, BorderLayout.CENTER);
        add(bottom, BorderLayout.SOUTH);

        hintButton.addActionListener(e -> useHint());
        endGameButton.addActionListener(e -> finishGame());

        // gridPanel configured when game starts
        this.putClientProperty("gridPanel", gridPanel);
    }

    public void startGame() {
        int gridSize = gameService.getGridSize();
        hintsLeft = gameService.getInitialHints();
        totalTimeSeconds = gameService.getTotalTimeSeconds();
        timeRemaining = totalTimeSeconds;
        score = 0;

        difficultyLabel.setText("Difficulty: " + gameService.getCurrentDifficulty().getDisplayName());
        scoreLabel.setText("Score: 0");
        hintsLabel.setText("Hints: " + hintsLeft);
        timerLabel.setText("Time left: " + timeRemaining + "s");

        setupGrid(gridSize);

        if (timer != null) {
            timer.stop();
        }
        timer = new javax.swing.Timer(1000, e -> tick());
        timer.start();
    }

    private void tick() {
        timeRemaining--;
        timerLabel.setText("Time left: " + timeRemaining + "s");
        if (timeRemaining <= 0) {
            finishGame();
        }
    }

    private void setupGrid(int gridSize) {
        cardButtons.clear();
        cardValues.clear();
        firstRevealed = null;
        secondRevealed = null;

        int totalCards = gridSize * gridSize;
        for (int i = 0; i < totalCards / 2; i++) {
            cardValues.add(i);
            cardValues.add(i);
        }
        Collections.shuffle(cardValues);

        JPanel gridPanel = (JPanel) this.getClientProperty("gridPanel");
        gridPanel.removeAll();
        gridPanel.setLayout(new GridLayout(gridSize, gridSize, 12, 12));

        for (int i = 0; i < totalCards; i++) {
            JButton card = new JButton();
            card.putClientProperty("value", cardValues.get(i));
            card.putClientProperty("revealed", false);
            card.putClientProperty("matched", false);
            card.putClientProperty("index", i);
            card.putClientProperty("JButton.buttonType", "roundRect");
            styleCardAsHidden(card);
            card.addActionListener(new CardClickHandler(card));
            cardButtons.add(card);
            gridPanel.add(card);
        }

        gridPanel.revalidate();
        gridPanel.repaint();
    }

    private void styleCardAsHidden(JButton card) {
        card.setText("");
    }

    private void styleCardAsRevealed(JButton card, int value) {
        card.setText(String.valueOf(value));
    }

    private void finishGame() {
        if (timer != null) {
            timer.stop();
        }
        int timeTaken = Math.max(0, totalTimeSeconds - timeRemaining);
        gameService.saveSessionResult(score, timeTaken);
        appFrame.showGameOver(score);
    }

    private void useHint() {
        if (hintsLeft <= 0) {
            return;
        }
        hintsLeft--;
        hintsLabel.setText("Hints: " + hintsLeft);

        for (JButton card : cardButtons) {
            if (Boolean.TRUE.equals(card.getClientProperty("matched"))) {
                continue;
            }
            int value = (int) card.getClientProperty("value");
            styleCardAsRevealed(card, value);
        }

        javax.swing.Timer hideTimer = new javax.swing.Timer(1500, e -> {
            for (JButton card : cardButtons) {
                if (Boolean.TRUE.equals(card.getClientProperty("matched"))) {
                    continue;
                }
                styleCardAsHidden(card);
                card.putClientProperty("revealed", false);
            }
        });
        hideTimer.setRepeats(false);
        hideTimer.start();
    }

    private class CardClickHandler implements ActionListener {
        private final JButton card;

        CardClickHandler(JButton card) {
            this.card = card;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            if (Boolean.TRUE.equals(card.getClientProperty("matched"))) {
                return;
            }
            if (Boolean.TRUE.equals(card.getClientProperty("revealed"))) {
                return;
            }

            int value = (int) card.getClientProperty("value");
            styleCardAsRevealed(card, value);
            card.putClientProperty("revealed", true);

            if (firstRevealed == null) {
                firstRevealed = card;
            } else if (secondRevealed == null && card != firstRevealed) {
                secondRevealed = card;
                checkMatch();
            }
        }

        private void checkMatch() {
            int v1 = (int) firstRevealed.getClientProperty("value");
            int v2 = (int) secondRevealed.getClientProperty("value");
            if (v1 == v2) {
                firstRevealed.putClientProperty("matched", true);
                secondRevealed.putClientProperty("matched", true);
                score += 10;
                scoreLabel.setText("Score: " + score);
                firstRevealed = null;
                secondRevealed = null;

                boolean allMatched = cardButtons.stream()
                        .allMatch(b -> Boolean.TRUE.equals(b.getClientProperty("matched")));
                if (allMatched) {
                    finishGame();
                }
            } else {
                javax.swing.Timer hideTimer = new javax.swing.Timer(800, e -> {
                    styleCardAsHidden(firstRevealed);
                    styleCardAsHidden(secondRevealed);
                    firstRevealed.putClientProperty("revealed", false);
                    secondRevealed.putClientProperty("revealed", false);
                    firstRevealed = null;
                    secondRevealed = null;
                });
                hideTimer.setRepeats(false);
                hideTimer.start();
            }
        }
    }
}