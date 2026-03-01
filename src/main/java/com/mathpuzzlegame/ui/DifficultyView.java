package com.mathpuzzlegame.ui;

import com.mathpuzzlegame.model.Difficulty;
import com.mathpuzzlegame.service.GameService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class DifficultyView extends JPanel {

    private final AppFrame appFrame;
    private final GameService gameService;

    private final JRadioButton beginnerButton = new JRadioButton("Beginner");
    private final JRadioButton intermediateButton = new JRadioButton("Intermediate");
    private final JRadioButton advancedButton = new JRadioButton("Advanced");

    public DifficultyView(AppFrame appFrame, GameService gameService) {
        this.appFrame = appFrame;
        this.gameService = gameService;

        setLayout(new BorderLayout());
        setBorder(new EmptyBorder(20, 20, 20, 20));

        JLabel title = new JLabel("Choose Difficulty");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 24f));
        title.setHorizontalAlignment(SwingConstants.CENTER);

        ButtonGroup group = new ButtonGroup();
        group.add(beginnerButton);
        group.add(intermediateButton);
        group.add(advancedButton);

        JPanel options = new JPanel();
        options.setLayout(new BoxLayout(options, BoxLayout.Y_AXIS));
        options.setBorder(new EmptyBorder(40, 32, 40, 32));
        options.setOpaque(false);

        beginnerButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        intermediateButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        advancedButton.setAlignmentX(Component.CENTER_ALIGNMENT);

        options.add(beginnerButton);
        options.add(Box.createVerticalStrut(10));
        options.add(intermediateButton);
        options.add(Box.createVerticalStrut(10));
        options.add(advancedButton);

        JButton saveButton = new JButton("Save");
        JButton backButton = new JButton("Back");
        saveButton.putClientProperty("JButton.buttonType", "roundRect");
        backButton.putClientProperty("JButton.buttonType", "roundRect");

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        actions.add(backButton);
        actions.add(saveButton);

        JPanel centerWrapper = new JPanel(new GridBagLayout());
        centerWrapper.setOpaque(false);
        centerWrapper.add(options, new GridBagConstraints());

        add(title, BorderLayout.NORTH);
        add(centerWrapper, BorderLayout.CENTER);
        add(actions, BorderLayout.SOUTH);

        saveButton.addActionListener(e -> saveSelection());
        backButton.addActionListener(e -> appFrame.showHome());
    }

    public void refreshSelection() {
        Difficulty diff = gameService.getCurrentDifficulty();
        switch (diff) {
            case BEGINNER -> beginnerButton.setSelected(true);
            case INTERMEDIATE -> intermediateButton.setSelected(true);
            case ADVANCED -> advancedButton.setSelected(true);
            default -> beginnerButton.setSelected(true);
        }
    }

    private void saveSelection() {
        if (beginnerButton.isSelected()) {
            gameService.setCurrentDifficulty(Difficulty.BEGINNER);
        } else if (intermediateButton.isSelected()) {
            gameService.setCurrentDifficulty(Difficulty.INTERMEDIATE);
        } else if (advancedButton.isSelected()) {
            gameService.setCurrentDifficulty(Difficulty.ADVANCED);
        }
        appFrame.showHome();
    }
}