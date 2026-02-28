package com.mathpuzzlegame.ui;

import com.mathpuzzlegame.model.User;
import com.mathpuzzlegame.service.AuthService;
import com.mathpuzzlegame.service.GameService;

import javax.swing.*;
import java.awt.*;

public class LoginView extends JPanel {

    private final AppFrame appFrame;
    private final AuthService authService;
    private final GameService gameService;

    private final JTextField usernameField = new JTextField(20);
    private final JPasswordField passwordField = new JPasswordField(20);
    private final JLabel errorLabel = new JLabel(" ");

    public LoginView(AppFrame appFrame, AuthService authService, GameService gameService) {
        this.appFrame = appFrame;
        this.authService = authService;
        this.gameService = gameService;

        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(40, 40, 40, 40));

        JLabel title = new JLabel("Math Puzzle Game");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 28f));
        title.setHorizontalAlignment(SwingConstants.CENTER);

        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
        centerPanel.setBorder(BorderFactory.createEmptyBorder(20, 32, 32, 32));
        centerPanel.setOpaque(false);

        JLabel subtitle = new JLabel("Sign in to continue");
        subtitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        Dimension fieldSize = new Dimension(260, usernameField.getPreferredSize().height);
        usernameField.setMaximumSize(fieldSize);
        passwordField.setMaximumSize(fieldSize);
        usernameField.setAlignmentX(Component.CENTER_ALIGNMENT);
        passwordField.setAlignmentX(Component.CENTER_ALIGNMENT);

        JButton loginButton = new JButton("Login");
        JButton registerButton = new JButton("Create account");
        JButton themeButton = new JButton("Toggle Light/Dark");
        loginButton.putClientProperty("JButton.buttonType", "roundRect");
        registerButton.putClientProperty("JButton.buttonType", "roundRect");
        themeButton.putClientProperty("JButton.buttonType", "roundRect");

        loginButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        registerButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        themeButton.setAlignmentX(Component.CENTER_ALIGNMENT);

        errorLabel.setForeground(Color.RED);
        errorLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        centerPanel.add(Box.createVerticalStrut(10));
        centerPanel.add(subtitle);
        centerPanel.add(Box.createVerticalStrut(20));
        JLabel userLabel = new JLabel("Username");
        userLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        centerPanel.add(userLabel);
        centerPanel.add(usernameField);
        centerPanel.add(Box.createVerticalStrut(10));
        JLabel passLabel = new JLabel("Password");
        passLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        centerPanel.add(passLabel);
        centerPanel.add(passwordField);
        centerPanel.add(Box.createVerticalStrut(15));
        centerPanel.add(loginButton);
        centerPanel.add(Box.createVerticalStrut(10));
        centerPanel.add(registerButton);
        centerPanel.add(Box.createVerticalStrut(10));
        centerPanel.add(themeButton);
        centerPanel.add(Box.createVerticalStrut(10));
        centerPanel.add(errorLabel);

        JPanel centerWrapper = new JPanel(new GridBagLayout());
        centerWrapper.setOpaque(false);
        centerWrapper.add(centerPanel, new GridBagConstraints());

        add(title, BorderLayout.NORTH);
        add(centerWrapper, BorderLayout.CENTER);

        loginButton.addActionListener(e -> doLogin());
        registerButton.addActionListener(e -> appFrame.showRegister());
        themeButton.addActionListener(e -> appFrame.toggleTheme());
        passwordField.addActionListener(e -> doLogin());
    }

    private void doLogin() {
        String username = usernameField.getText();
        String password = new String(passwordField.getPassword());

        authService.login(username, password).ifPresentOrElse(user -> {
            errorLabel.setText(" ");
            clearFields();
            gameService.setCurrentUser(user);
            appFrame.onAuthenticated(user);
        }, () -> errorLabel.setText("Invalid username or password"));
    }

    private void clearFields() {
        usernameField.setText("");
        passwordField.setText("");
    }
}