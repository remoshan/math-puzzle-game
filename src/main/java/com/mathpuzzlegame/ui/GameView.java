package com.mathpuzzlegame.ui;

import com.mathpuzzlegame.service.GameService;
import com.mathpuzzlegame.service.ImageService;
import com.mathpuzzlegame.service.MusicService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class GameView extends JPanel {

    private final AppFrame     appFrame;
    private final GameService  gameService;
    private final MusicService musicService;
    private final ImageService imageService;

    private final JLabel difficultyLabel = new JLabel();
    private final JLabel timerLabel      = new JLabel();
    private final JLabel scoreLabel      = new JLabel("Score: 0");
    private final JLabel hintsLabel      = new JLabel("Hints: 0");

    private javax.swing.Timer timer;
    private int timeRemaining;
    private int score;
    private int hintsLeft;
    private int totalTimeSeconds;

    private final List<JButton> cardButtons  = new ArrayList<>();
    private final List<Integer> cardValues   = new ArrayList<>();
    private JButton firstRevealed;
    private JButton secondRevealed;

    // Stores icons keyed by pair index — populated before grid is built
    private Map<Integer, ImageIcon> gameIcons;

    private final JToggleButton musicToggle  = new JToggleButton();
    private final JSlider       volumeSlider = new JSlider(0, 100, 80);

    private static final int CARD_ICON_SIZE = 72;

    public GameView(AppFrame appFrame, GameService gameService,
                    MusicService musicService, ImageService imageService) {
        this.appFrame     = appFrame;
        this.gameService  = gameService;
        this.musicService = musicService;
        this.imageService = imageService;

        setLayout(new BorderLayout());
        setBorder(new EmptyBorder(20, 20, 20, 20));

        // ── Top bar ───────────────────────────────────────────────────────────
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

        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setOpaque(false);
        topBar.add(leftTop,     BorderLayout.WEST);
        topBar.add(timerLabel,  BorderLayout.CENTER);
        topBar.add(rightTop,    BorderLayout.EAST);

        // ── Grid panel (placeholder until images load) ────────────────────────
        JPanel gridPanel = new JPanel(new BorderLayout());
        gridPanel.setBorder(new EmptyBorder(32, 32, 32, 32));

        // ── Bottom bar ────────────────────────────────────────────────────────
        JButton hintButton   = new JButton("Use Hint");
        JButton endGameButton = new JButton("End Game");
        hintButton.putClientProperty("JButton.buttonType", "roundRect");
        endGameButton.putClientProperty("JButton.buttonType", "roundRect");

        musicToggle.putClientProperty("JButton.buttonType", "roundRect");
        musicToggle.setFocusable(false);
        updateMusicToggleLabel();

        volumeSlider.setPreferredSize(new Dimension(80, volumeSlider.getPreferredSize().height));
        volumeSlider.setOpaque(false);
        volumeSlider.addChangeListener(e ->
                musicService.setVolume(volumeSlider.getValue() / 100f));

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottom.add(new JLabel("Music"));
        bottom.add(musicToggle);
        bottom.add(volumeSlider);
        bottom.add(hintButton);
        bottom.add(endGameButton);

        add(topBar,   BorderLayout.NORTH);
        add(gridPanel, BorderLayout.CENTER);
        add(bottom,   BorderLayout.SOUTH);

        hintButton.addActionListener(e -> useHint());
        endGameButton.addActionListener(e -> finishGame());
        musicToggle.addActionListener(e -> {
            boolean muted = musicToggle.isSelected();
            musicService.setMuted(muted);
            updateMusicToggleLabel();
            if (!muted) musicService.startBackgroundMusic();
        });

        this.putClientProperty("gridPanel", gridPanel);
    }

    // ── Game entry point ──────────────────────────────────────────────────────

    public void startGame() {
        int gridSize = gameService.getGridSize();
        hintsLeft         = gameService.getInitialHints();
        totalTimeSeconds  = gameService.getTotalTimeSeconds();
        timeRemaining     = totalTimeSeconds;
        score             = 0;

        difficultyLabel.setText("Difficulty: "
                + gameService.getCurrentDifficulty().getDisplayName());
        scoreLabel.setText("Score: 0");
        hintsLabel.setText("Hints: " + hintsLeft);
        timerLabel.setText("Time left: " + timeRemaining + "s");

        if (timer != null) timer.stop();

        // Show loading screen while images download
        showLoadingScreen();

        int pairCount = (gridSize * gridSize) / 2;

        // loadGameImages always runs on bgExecutor — safe to call from EDT
        imageService.loadGameImages(pairCount, CARD_ICON_SIZE)
                .thenAcceptAsync(icons -> {
                    // Back on EDT once all images are ready
                    gameIcons = icons;
                    buildGrid(gridSize);
                    startTimer();
                }, SwingUtilities::invokeLater);
    }

    // ── Loading screen ────────────────────────────────────────────────────────

    private void showLoadingScreen() {
        JPanel gridPanel = (JPanel) getClientProperty("gridPanel");
        gridPanel.removeAll();
        gridPanel.setLayout(new BorderLayout());

        JLabel loadingLabel = new JLabel("Loading fruit images…", SwingConstants.CENTER);
        loadingLabel.setFont(loadingLabel.getFont().deriveFont(Font.PLAIN, 18f));
        loadingLabel.setForeground(new Color(120, 120, 140));
        gridPanel.add(loadingLabel, BorderLayout.CENTER);

        gridPanel.revalidate();
        gridPanel.repaint();
    }

    // ── Grid construction (called after all images are ready) ─────────────────

    private void buildGrid(int gridSize) {
        cardButtons.clear();
        cardValues.clear();
        firstRevealed  = null;
        secondRevealed = null;

        int totalCards = gridSize * gridSize;
        int pairCount  = totalCards / 2;

        for (int i = 0; i < pairCount; i++) {
            cardValues.add(i);
            cardValues.add(i);
        }
        Collections.shuffle(cardValues);

        JPanel gridPanel = (JPanel) getClientProperty("gridPanel");
        gridPanel.removeAll();
        gridPanel.setLayout(new GridLayout(gridSize, gridSize, 12, 12));

        for (int i = 0; i < totalCards; i++) {
            JButton card     = new JButton();
            int     pairIdx  = cardValues.get(i);

            card.putClientProperty("value",   pairIdx);
            card.putClientProperty("revealed", false);
            card.putClientProperty("matched",  false);
            card.putClientProperty("JButton.buttonType", "roundRect");

            // Store the icon on the card now — it's ready before the grid is shown
            ImageIcon icon = (gameIcons != null) ? gameIcons.get(pairIdx) : null;
            if (icon != null) {
                card.putClientProperty("cardIcon", icon);
            }

            styleCardAsHidden(card);
            card.addActionListener(new CardClickHandler(card));
            cardButtons.add(card);
            gridPanel.add(card);
        }

        gridPanel.revalidate();
        gridPanel.repaint();
    }

    // ── Timer ─────────────────────────────────────────────────────────────────

    private void startTimer() {
        timer = new javax.swing.Timer(1000, e -> tick());
        timer.start();
    }

    private void tick() {
        timeRemaining--;
        timerLabel.setText("Time left: " + timeRemaining + "s");
        if (timeRemaining <= 0) finishGame();
    }

    // ── Card styling ──────────────────────────────────────────────────────────

    private void styleCardAsHidden(JButton card) {
        card.setIcon(null);
        card.setText("");
    }

    private void styleCardAsRevealed(JButton card) {
        card.setText("");
        Object iconObj = card.getClientProperty("cardIcon");
        if (iconObj instanceof ImageIcon icon) {
            card.setIcon(icon);
        } else {
            // Icon not ready yet — show a neutral placeholder
            card.setText("🍓");
            card.setFont(card.getFont().deriveFont(Font.PLAIN, 28f));
        }
    }

    // ── Game actions ──────────────────────────────────────────────────────────

    private void finishGame() {
        if (timer != null) timer.stop();
        int timeTaken = Math.max(0, totalTimeSeconds - timeRemaining);
        gameService.saveSessionResult(score, timeTaken);
        appFrame.showGameOver(score);
    }

    private void useHint() {
        if (hintsLeft <= 0) return;
        hintsLeft--;
        hintsLabel.setText("Hints: " + hintsLeft);

        for (JButton card : cardButtons) {
            if (Boolean.TRUE.equals(card.getClientProperty("matched"))) continue;
            styleCardAsRevealed(card);
        }

        new javax.swing.Timer(1500, e -> {
            for (JButton card : cardButtons) {
                if (Boolean.TRUE.equals(card.getClientProperty("matched"))) continue;
                styleCardAsHidden(card);
                card.putClientProperty("revealed", false);
            }
        }) {{ setRepeats(false); start(); }};
    }

    private void updateMusicToggleLabel() {
        boolean muted = musicService.isMuted();
        musicToggle.setSelected(muted);
        musicToggle.setText(muted ? "Off" : "On");
    }

    // ── Card click handler ────────────────────────────────────────────────────

    private class CardClickHandler implements ActionListener {
        private final JButton card;

        CardClickHandler(JButton card) { this.card = card; }

        @Override
        public void actionPerformed(ActionEvent e) {
            if (Boolean.TRUE.equals(card.getClientProperty("matched")))  return;
            if (Boolean.TRUE.equals(card.getClientProperty("revealed"))) return;

            styleCardAsRevealed(card);
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
                // Match!
                firstRevealed.putClientProperty("matched", true);
                secondRevealed.putClientProperty("matched", true);
                score += 10;
                scoreLabel.setText("Score: " + score);
                firstRevealed  = null;
                secondRevealed = null;

                boolean allMatched = cardButtons.stream()
                        .allMatch(b -> Boolean.TRUE.equals(b.getClientProperty("matched")));
                if (allMatched) finishGame();

            } else {
                // No match — flip back after 800ms
                JButton a = firstRevealed;
                JButton b = secondRevealed;
                firstRevealed  = null;
                secondRevealed = null;

                new javax.swing.Timer(800, ev -> {
                    styleCardAsHidden(a);
                    styleCardAsHidden(b);
                    a.putClientProperty("revealed", false);
                    b.putClientProperty("revealed", false);
                }) {{ setRepeats(false); start(); }};
            }
        }
    }
}