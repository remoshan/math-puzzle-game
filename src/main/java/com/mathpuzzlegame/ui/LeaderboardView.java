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
    private final JScrollPane scrollPane;

    public LeaderboardView(AppFrame appFrame, GameService gameService) {
        this.appFrame = appFrame;
        this.gameService = gameService;

        setLayout(new BorderLayout());
        setBorder(new EmptyBorder(20, 20, 20, 20));

        JLabel title = new JLabel("Leaderboard");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 26f));
        title.setHorizontalAlignment(SwingConstants.CENTER);

        rowsPanel.setLayout(new BoxLayout(rowsPanel, BoxLayout.Y_AXIS));
        rowsPanel.setOpaque(true);
        Color bg = UIManager.getColor("Panel.background");
        if (bg != null) {
            rowsPanel.setBackground(bg);
        }

        scrollPane = new JScrollPane(rowsPanel);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        if (scrollPane.getViewport() != null && bg != null) {
            scrollPane.getViewport().setBackground(bg);
        }

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
        header.setOpaque(true);
        Color headerBg = UIManager.getColor("TableHeader.background");
        if (headerBg == null) {
            headerBg = UIManager.getColor("Panel.background");
        }
        if (headerBg != null) {
            header.setBackground(headerBg);
        }
        Color sep = UIManager.getColor("Separator.foreground");
        if (sep == null) {
            sep = new Color(0, 0, 0, 30);
        }
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, sep));
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
        Color tableBg = UIManager.getColor("Table.background");
        if (tableBg == null) {
            tableBg = UIManager.getColor("Panel.background");
        }
        Color alt = UIManager.getColor("Table.alternateRowColor");
        if (alt == null && tableBg != null) {
            alt = new Color(
                    Math.max(0, Math.min(255, tableBg.getRed() - 6)),
                    Math.max(0, Math.min(255, tableBg.getGreen() - 6)),
                    Math.max(0, Math.min(255, tableBg.getBlue() - 6))
            );
        }

        for (UserScore score : scores) {
            JPanel row = new JPanel(new GridLayout(1, 4, 8, 8));
            row.setOpaque(true);

            if (tableBg != null) {
                row.setBackground((rank % 2 == 0 && alt != null) ? alt : tableBg);
            }
            if (currentUsername != null && currentUsername.equals(score.getUsername())) {
                Color sel = UIManager.getColor("List.selectionBackground");
                if (sel == null) {
                    sel = new Color(230, 240, 255);
                }
                row.setBackground(sel);
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
        Color fg = UIManager.getColor("TableHeader.foreground");
        if (fg == null) {
            fg = UIManager.getColor("Label.foreground");
        }
        if (fg != null) {
            label.setForeground(fg);
        }
        return label;
    }

    private JLabel createCellLabel(String text) {
        JLabel label = new JLabel(text);
        label.setHorizontalAlignment(SwingConstants.CENTER);
        Color fg = UIManager.getColor("Label.foreground");
        if (fg != null) {
            label.setForeground(fg);
        }
        return label;
    }
}