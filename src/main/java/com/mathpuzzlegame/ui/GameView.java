package com.mathpuzzlegame.ui;

import com.mathpuzzlegame.service.GameService;
import com.mathpuzzlegame.service.ImageService;
import com.mathpuzzlegame.service.MusicService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class GameView extends JPanel {

    private final AppFrame appFrame;
    private final GameService gameService;
    private final MusicService musicService;
    private final ImageService imageService;

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

    private final JToggleButton musicToggle = new JToggleButton();
    private final JSlider volumeSlider = new JSlider(0, 100, 80);

    // image handling
    private Map<Integer, CompletableFuture<ImageIcon>> imageFutures;
    private static final int CARD_ICON_SIZE = 72;

    public GameView(AppFrame appFrame, GameService gameService, MusicService musicService, ImageService imageService) {
        this.appFrame = appFrame;
        this.gameService = gameService;
        this.musicService = musicService;
        this.imageService = imageService;

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

        musicToggle.putClientProperty("JButton.buttonType", "roundRect");
        musicToggle.setFocusable(false);
        updateMusicToggleLabel();

        volumeSlider.setPreferredSize(new Dimension(80, volumeSlider.getPreferredSize().height));
        volumeSlider.setOpaque(false);
        volumeSlider.addChangeListener(e -> {
            float level = volumeSlider.getValue() / 100f;
            musicService.setVolume(level);
        });

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottom.add(new JLabel("Music"));
        bottom.add(musicToggle);
        bottom.add(volumeSlider);
        bottom.add(hintButton);
        bottom.add(endGameButton);

        setLayout(new BorderLayout());
        add(topBar, BorderLayout.NORTH);
        add(gridPanel, BorderLayout.CENTER);
        add(bottom, BorderLayout.SOUTH);

        hintButton.addActionListener(e -> useHint());
        endGameButton.addActionListener(e -> finishGame());
        musicToggle.addActionListener(e -> {
            boolean muted = musicToggle.isSelected();
            musicService.setMuted(muted);
            updateMusicToggleLabel();
            if (!muted) {
                musicService.startBackgroundMusic();
            }
        });

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

    private void updateMusicToggleLabel() {
        if (musicService.isMuted()) {
            musicToggle.setSelected(true);
            musicToggle.setText("Off");
        } else {
            musicToggle.setSelected(false);
            musicToggle.setText("On");
        }
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

        // kick off async image loading for each pair
        int pairCount = totalCards / 2;
        imageFutures = imageService.loadCardImagesAsync(pairCount, CARD_ICON_SIZE);

        JPanel gridPanel = (JPanel) this.getClientProperty("gridPanel");
        gridPanel.removeAll();
        gridPanel.setLayout(new GridLayout(gridSize, gridSize, 12, 12));

        for (int i = 0; i < totalCards; i++) {
            JButton card = new JButton();
            int pairIndex = cardValues.get(i);
            card.putClientProperty("value", pairIndex);
            card.putClientProperty("revealed", false);
            card.putClientProperty("matched", false);
            card.putClientProperty("index", i);
            card.putClientProperty("JButton.buttonType", "roundRect");
            styleCardAsHidden(card);
            card.addActionListener(new CardClickHandler(card));
            cardButtons.add(card);
            gridPanel.add(card);

            // attach image when it becomes available
            CompletableFuture<ImageIcon> future = imageFutures.get(pairIndex);
            if (future != null) {
                future.thenAccept(icon -> {
                    if (icon == null) {
                        return;
                    }
                    SwingUtilities.invokeLater(() -> {
                        card.putClientProperty("cardIcon", icon);
                        if (Boolean.TRUE.equals(card.getClientProperty("revealed"))) {
                            Object v = card.getClientProperty("value");
                            int value = (v instanceof Integer) ? (Integer) v : 0;
                            styleCardAsRevealed(card, value);
                        }
                    });
                });
            }
        }

        gridPanel.revalidate();
        gridPanel.repaint();
    }

    private static final Icon LOADING_ICON = createLoadingIcon();

    private static Icon createLoadingIcon() {
        int size = CARD_ICON_SIZE;
        BufferedImage img = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = img.createGraphics();
        try {
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(new Color(220, 220, 230));
            g2.fillRoundRect(2, 2, size - 4, size - 4, size / 3, size / 3);
            g2.setColor(new Color(200, 200, 210));
            g2.drawRoundRect(2, 2, size - 4, size - 4, size / 3, size / 3);
        } finally {
            g2.dispose();
        }
        return new ImageIcon(img);
    }

    private void styleCardAsHidden(JButton card) {
        card.setIcon(null);
        card.setText("");
    }

    private void styleCardAsRevealed(JButton card, int value) {
        // Use only images for the game: either the real Banana image or a soft placeholder.
        card.setText("");

        Object iconObj = card.getClientProperty("cardIcon");
        if (iconObj instanceof ImageIcon icon) {
            card.setIcon(icon);
        } else {
            card.setIcon(LOADING_ICON);
        }
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