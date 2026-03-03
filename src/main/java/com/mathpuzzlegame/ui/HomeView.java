package com.mathpuzzlegame.ui;

import com.mathpuzzlegame.service.GameService;
import com.mathpuzzlegame.theme.ThemeManager;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.List;

public class HomeView extends JPanel {

    private final AppFrame appFrame;
    private final GameService gameService;
    private final ThemeManager themeManager;

    private final JLabel bestScoreLabel = new JLabel("0");
    private final JLabel totalGamesLabel = new JLabel("0");
    private final JLabel gamesByDifficultyLabel = new JLabel("0 / 0 / 0");
    private final DefaultListModel<String> leaderboardPreviewModel = new DefaultListModel<>();

    public HomeView(AppFrame appFrame, GameService gameService, ThemeManager themeManager) {
        this.appFrame = appFrame;
        this.gameService = gameService;
        this.themeManager = themeManager;

        setLayout(new BorderLayout());
        setBorder(new EmptyBorder(20, 20, 20, 20));

        JLabel title = new JLabel("Math Puzzle Studio");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 24f));
        title.setHorizontalAlignment(SwingConstants.CENTER);

        JPanel statsPanel = new JPanel(new GridLayout(2, 2, 16, 16));
        statsPanel.add(createStatCard("Best score", bestScoreLabel));
        statsPanel.add(createStatCard("Total games", totalGamesLabel));
        statsPanel.add(createStatCard("Beginner / Intermediate / Advanced", gamesByDifficultyLabel));

        JList<String> leaderboardPreview = new JList<>(leaderboardPreviewModel);
        JScrollPane leaderboardScroll = new JScrollPane(leaderboardPreview);
        leaderboardScroll.setBorder(BorderFactory.createTitledBorder("Top players (by time)"));

        JPanel centerPanel = new JPanel(new BorderLayout(16, 16));
        centerPanel.add(statsPanel, BorderLayout.NORTH);
        centerPanel.add(leaderboardScroll, BorderLayout.CENTER);

        JButton newGameButton = new JButton("Start New Game");
        JButton changeDifficultyButton = new JButton("Change Difficulty");
        JButton leaderboardButton = new JButton("Leaderboard");
        JButton logoutButton = new JButton("Logout");
        JButton toggleThemeButton = new JButton("Toggle Light/Dark");

        newGameButton.putClientProperty("JButton.buttonType", "roundRect");
        changeDifficultyButton.putClientProperty("JButton.buttonType", "roundRect");
        leaderboardButton.putClientProperty("JButton.buttonType", "roundRect");
        logoutButton.putClientProperty("JButton.buttonType", "roundRect");
        toggleThemeButton.putClientProperty("JButton.buttonType", "roundRect");

        JPanel actions = new JPanel();
        actions.setLayout(new FlowLayout(FlowLayout.RIGHT));
        actions.add(toggleThemeButton);
        actions.add(leaderboardButton);
        actions.add(changeDifficultyButton);
        actions.add(newGameButton);
        actions.add(logoutButton);

        add(title, BorderLayout.NORTH);
        add(centerPanel, BorderLayout.CENTER);
        add(actions, BorderLayout.SOUTH);

        newGameButton.addActionListener(e -> appFrame.startNewGame());
        changeDifficultyButton.addActionListener(e -> appFrame.showDifficultySelection());
        leaderboardButton.addActionListener(e -> appFrame.showLeaderboard());
        logoutButton.addActionListener(e -> appFrame.logout());
        toggleThemeButton.addActionListener(e -> appFrame.toggleTheme());
    }

    private JPanel createStatCard(String label, JLabel valueLabel) {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(new EmptyBorder(12, 12, 12, 12));

        JLabel title = new JLabel(label);
        title.setFont(title.getFont().deriveFont(Font.PLAIN, 14f));

        valueLabel.setFont(valueLabel.getFont().deriveFont(Font.BOLD, 20f));

        card.add(title);
        card.add(Box.createVerticalStrut(8));
        card.add(valueLabel);

        return card;
    }

    public void refreshStats() {
        int best = gameService.getCurrentUserBestScore();
        bestScoreLabel.setText(String.valueOf(best));

        int totalGames = gameService.getTotalGamesForCurrentUser();
        totalGamesLabel.setText(String.valueOf(totalGames));

        int beginnerGames = gameService.getTotalGamesForCurrentUser(com.mathpuzzlegame.model.Difficulty.BEGINNER);
        int intermediateGames = gameService.getTotalGamesForCurrentUser(com.mathpuzzlegame.model.Difficulty.INTERMEDIATE);
        int advancedGames = gameService.getTotalGamesForCurrentUser(com.mathpuzzlegame.model.Difficulty.ADVANCED);
        gamesByDifficultyLabel.setText(
                beginnerGames + " / " + intermediateGames + " / " + advancedGames
        );

        leaderboardPreviewModel.clear();
        gameService.getTopScoresByTime().stream()
                .limit(5)
                .forEach(score -> leaderboardPreviewModel.addElement(
                        score.getUsername() + " • " + score.getTimeTakenSeconds() + "s • " + score.getScore()
                ));
    }
}