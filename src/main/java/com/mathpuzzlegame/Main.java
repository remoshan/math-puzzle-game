package com.mathpuzzlegame;

import com.mathpuzzlegame.net.MusicApi;
import com.mathpuzzlegame.service.ImageService;
import com.mathpuzzlegame.service.MusicService;
import com.mathpuzzlegame.data.DatabaseManager;
import com.mathpuzzlegame.service.AuthService;
import com.mathpuzzlegame.service.GameService;
import com.mathpuzzlegame.theme.ThemeManager;
import com.mathpuzzlegame.ui.AppFrame;

import javax.swing.*;

public class Main {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            DatabaseManager databaseManager = new DatabaseManager("jdbc:sqlite:math_puzzle.db");
            AuthService authService = new AuthService(databaseManager);
            GameService gameService = new GameService(databaseManager);
            ImageService imageService = new ImageService();  // ← no BananaApi arg
            MusicApi musicApi = new MusicApi();
            MusicService musicService = new MusicService(musicApi);
            ThemeManager themeManager = new ThemeManager();

            themeManager.applyInitialTheme();

            AppFrame appFrame = new AppFrame(authService, gameService, themeManager, musicService, imageService);
            appFrame.setVisible(true);
        });
    }
}