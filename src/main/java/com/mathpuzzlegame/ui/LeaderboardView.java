package com.mathpuzzlegame.ui;

import com.mathpuzzlegame.model.UserScore;
import com.mathpuzzlegame.service.GameService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.List;

public class LeaderboardView extends JPanel {

    private final AppFrame appFrame;
    private final GameService gameService;

    private final JPanel rowsPanel = new JPanel();

    public LeaderboardView(AppFrame appFrame, GameService gameService) {
        this.appFrame = appFrame;
        this.gameService = gameService;

        setLayout(new BorderLayout());
        setBorder(new EmptyBorder(20, 20, 20, 20));

        JLabel title = new JLabel("Leaderboard");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 26f));
        title.setHorizontalAlignment(SwingConstants.CENTER);

        rowsPanel.setLayout(new BoxLayout(rowsPanel, BoxLayout.Y_AXIS));

        JScrollPane scrollPane = new JScrollPane(rowsPanel);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());

        JButton backButton = new JButton("Back");
        backButton.putClientProperty("JButton.buttonType", "roundRect");
        backButton.addActionListener(e -> appFrame.showHome());

        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        footer.add(backButton);

        add(title, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        add(footer, BorderLayout.SOUTH);
    }

    public void refresh() {
        rowsPanel.removeAll();

        JPanel header = new JPanel(new GridLayout(1, 4, 8, 8));
        header.add(createHeaderLabel("Rank"));
        header.add(createHeaderLabel("Player"));
        header.add(createHeaderLabel("Score"));
        header.add(createHeaderLabel("Time (s)"));
        rowsPanel.add(header);

        List<UserScore> scores = gameService.getTopScoresByTime();
        String currentUsername = gameService.getCurrentUser() != null
                ? gameService.getCurrentUser().getUsername()
                : null;

        int rank = 1;
        for (UserScore score : scores) {
            JPanel row = new JPanel(new GridLayout(1, 4, 8, 8));
            if (currentUsername != null && currentUsername.equals(score.getUsername())) {
                row.setBackground(new Color(230, 240, 255));
            }
            row.add(createCellLabel(String.valueOf(rank++)));
            row.add(createCellLabel(score.getUsername()));
            row.add(createCellLabel(String.valueOf(score.getScore())));
            row.add(createCellLabel(String.valueOf(score.getTimeTakenSeconds())));
            rowsPanel.add(row);
        }

        rowsPanel.revalidate();
        rowsPanel.repaint();
    }

    private JLabel createHeaderLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(label.getFont().deriveFont(Font.BOLD, 14f));
        label.setHorizontalAlignment(SwingConstants.CENTER);
        return label;
    }

    private JLabel createCellLabel(String text) {
        JLabel label = new JLabel(text);
        label.setHorizontalAlignment(SwingConstants.CENTER);
        return label;
    }
}