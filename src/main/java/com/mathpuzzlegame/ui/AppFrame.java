package com.mathpuzzlegame.ui;

import com.mathpuzzlegame.model.User;
import com.mathpuzzlegame.service.AuthService;
import com.mathpuzzlegame.service.GameService;
import com.mathpuzzlegame.theme.ThemeManager;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class AppFrame extends JFrame {

    private final AuthService authService;
    private final GameService gameService;
    private final ThemeManager themeManager;

    private final CardLayout cardLayout = new CardLayout();
    private final JPanel cardPanel = new JPanel(cardLayout);

    private final LoginView loginView;
    private final RegisterView registerView;
    private final HomeView homeView;
    private final DifficultyView difficultyView;
    private final GameView gameView;
    private final GameOverView gameOverView;
    private final LeaderboardView leaderboardView;

    public AppFrame(AuthService authService, GameService gameService, ThemeManager themeManager) {
        super("Math Puzzle Game");
        this.authService = authService;
        this.gameService = gameService;
        this.themeManager = themeManager;

        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setSize(900, 600);
        setLocationRelativeTo(null);

        loginView = new LoginView(this, authService, gameService);
        registerView = new RegisterView(this, authService, gameService);
        homeView = new HomeView(this, gameService, themeManager);
        difficultyView = new DifficultyView(this, gameService);
        gameView = new GameView(this, gameService);
        gameOverView = new GameOverView(this, gameService);
        leaderboardView = new LeaderboardView(this, gameService);

        cardPanel.add(loginView, "login");
        cardPanel.add(registerView, "register");
        cardPanel.add(homeView, "home");
        cardPanel.add(difficultyView, "difficulty");
        cardPanel.add(gameView, "game");
        cardPanel.add(gameOverView, "gameOver");
        cardPanel.add(leaderboardView, "leaderboard");

        setContentPane(cardPanel);
        showLogin();
    }

    public void showLogin() {
        gameService.setCurrentUser(null);
        cardLayout.show(cardPanel, "login");
    }

    public void showRegister() {
        cardLayout.show(cardPanel, "register");
    }

    public void onAuthenticated(User user) {
        gameService.setCurrentUser(user);
        showHome();
    }

    public void showHome() {
        homeView.refreshStats();
        cardLayout.show(cardPanel, "home");
    }

    public void showDifficultySelection() {
        difficultyView.refreshSelection();
        cardLayout.show(cardPanel, "difficulty");
    }

    public void startNewGame() {
        gameView.startGame();
        cardLayout.show(cardPanel, "game");
    }

    public void showGameOver(int finalScore) {
        int best = gameService.getCurrentUserBestScore();
        gameOverView.setScores(finalScore, best);
        cardLayout.show(cardPanel, "gameOver");
    }

    public void logout() {
        gameService.setCurrentUser(null);
        showLogin();
    }

    public void toggleTheme() {
        themeManager.toggleTheme(this);
    }

    public void showLeaderboard() {
        leaderboardView.refresh();
        cardLayout.show(cardPanel, "leaderboard");
    }
}