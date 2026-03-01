package com.mathpuzzlegame.ui;

import com.mathpuzzlegame.service.GameService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class GameOverView extends JPanel {

    private final AppFrame appFrame;
    private final GameService gameService;

    private final JLabel finalScoreLabel = new JLabel("0");
    private final JLabel highestScoreLabel = new JLabel("0");

    public GameOverView(AppFrame appFrame, GameService gameService) {
        this.appFrame = appFrame;
        this.gameService = gameService;

        setLayout(new BorderLayout());
        setBorder(new EmptyBorder(20, 20, 20, 20));

        JLabel title = new JLabel("Game Over");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 32f));
        title.setHorizontalAlignment(SwingConstants.CENTER);

        JPanel stats = new JPanel();
        stats.setLayout(new BoxLayout(stats, BoxLayout.Y_AXIS));
        stats.setBorder(new EmptyBorder(40, 32, 40, 32));

        JLabel finalScoreText = new JLabel("Final score");
        JLabel highestScoreText = new JLabel("Highest score");

        finalScoreText.setAlignmentX(Component.CENTER_ALIGNMENT);
        highestScoreText.setAlignmentX(Component.CENTER_ALIGNMENT);
        finalScoreLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        highestScoreLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        finalScoreLabel.setFont(finalScoreLabel.getFont().deriveFont(Font.BOLD, 28f));
        highestScoreLabel.setFont(highestScoreLabel.getFont().deriveFont(Font.BOLD, 24f));

        stats.add(finalScoreText);
        stats.add(Box.createVerticalStrut(8));
        stats.add(finalScoreLabel);
        stats.add(Box.createVerticalStrut(20));
        stats.add(highestScoreText);
        stats.add(Box.createVerticalStrut(8));
        stats.add(highestScoreLabel);

        JButton restartButton = new JButton("Restart Game");
        JButton newGameButton = new JButton("Start New Game");
        JButton changeDifficultyButton = new JButton("Change Difficulty");
        JButton homeButton = new JButton("Home");

        restartButton.putClientProperty("JButton.buttonType", "roundRect");
        newGameButton.putClientProperty("JButton.buttonType", "roundRect");
        changeDifficultyButton.putClientProperty("JButton.buttonType", "roundRect");
        homeButton.putClientProperty("JButton.buttonType", "roundRect");

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        actions.add(changeDifficultyButton);
        actions.add(newGameButton);
        actions.add(restartButton);
        actions.add(homeButton);

        add(title, BorderLayout.NORTH);
        add(stats, BorderLayout.CENTER);
        add(actions, BorderLayout.SOUTH);

        restartButton.addActionListener(e -> appFrame.startNewGame());
        newGameButton.addActionListener(e -> appFrame.startNewGame());
        changeDifficultyButton.addActionListener(e -> appFrame.showDifficultySelection());
        homeButton.addActionListener(e -> appFrame.showHome());
    }

    public void setScores(int finalScore, int highestScore) {
        finalScoreLabel.setText(String.valueOf(finalScore));
        highestScoreLabel.setText(String.valueOf(highestScore));
    }
}