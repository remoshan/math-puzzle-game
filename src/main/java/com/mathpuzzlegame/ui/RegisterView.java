package com.mathpuzzlegame.ui;

import com.mathpuzzlegame.model.User;
import com.mathpuzzlegame.service.AuthService;
import com.mathpuzzlegame.service.GameService;

import javax.swing.*;
import java.awt.*;

public class RegisterView extends JPanel {

    private final AppFrame appFrame;
    private final AuthService authService;
    private final GameService gameService;

    private final JTextField usernameField = new JTextField(20);
    private final JPasswordField passwordField = new JPasswordField(20);
    private final JPasswordField confirmField = new JPasswordField(20);
    private final JLabel errorLabel = new JLabel(" ");

    public RegisterView(AppFrame appFrame, AuthService authService, GameService gameService) {
        this.appFrame = appFrame;
        this.authService = authService;
        this.gameService = gameService;

        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(40, 40, 40, 40));

        JLabel title = new JLabel("Create Account");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 28f));
        title.setHorizontalAlignment(SwingConstants.CENTER);

        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
        centerPanel.setBorder(BorderFactory.createEmptyBorder(20, 32, 32, 32));
        centerPanel.setOpaque(false);

        Dimension fieldSize = new Dimension(260, usernameField.getPreferredSize().height);
        usernameField.setMaximumSize(fieldSize);
        passwordField.setMaximumSize(fieldSize);
        confirmField.setMaximumSize(fieldSize);
        usernameField.setAlignmentX(Component.CENTER_ALIGNMENT);
        passwordField.setAlignmentX(Component.CENTER_ALIGNMENT);
        confirmField.setAlignmentX(Component.CENTER_ALIGNMENT);

        JButton registerButton = new JButton("Register");
        JButton backToLoginButton = new JButton("Back to login");
        JButton themeButton = new JButton("Toggle Light/Dark");
        registerButton.putClientProperty("JButton.buttonType", "roundRect");
        backToLoginButton.putClientProperty("JButton.buttonType", "roundRect");
        themeButton.putClientProperty("JButton.buttonType", "roundRect");

        registerButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        backToLoginButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        themeButton.setAlignmentX(Component.CENTER_ALIGNMENT);

        errorLabel.setForeground(Color.RED);
        errorLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel userLabel = new JLabel("Username");
        userLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        centerPanel.add(userLabel);
        centerPanel.add(usernameField);
        centerPanel.add(Box.createVerticalStrut(10));
        JLabel passLabel = new JLabel("Password");
        passLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        centerPanel.add(passLabel);
        centerPanel.add(passwordField);
        centerPanel.add(Box.createVerticalStrut(10));
        JLabel confirmLabel = new JLabel("Confirm Password");
        confirmLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        centerPanel.add(confirmLabel);
        centerPanel.add(confirmField);
        centerPanel.add(Box.createVerticalStrut(15));
        centerPanel.add(registerButton);
        centerPanel.add(Box.createVerticalStrut(10));
        centerPanel.add(backToLoginButton);
        centerPanel.add(Box.createVerticalStrut(10));
        centerPanel.add(themeButton);
        centerPanel.add(Box.createVerticalStrut(10));
        centerPanel.add(errorLabel);

        JPanel centerWrapper = new JPanel(new GridBagLayout());
        centerWrapper.setOpaque(false);
        centerWrapper.add(centerPanel, new GridBagConstraints());

        add(title, BorderLayout.NORTH);
        add(centerWrapper, BorderLayout.CENTER);

        registerButton.addActionListener(e -> doRegister());
        backToLoginButton.addActionListener(e -> appFrame.showLogin());
        themeButton.addActionListener(e -> appFrame.toggleTheme());
        confirmField.addActionListener(e -> doRegister());
    }

    private void doRegister() {
        String username = usernameField.getText();
        String password = new String(passwordField.getPassword());
        String confirm = new String(confirmField.getPassword());

        if (!password.equals(confirm)) {
            errorLabel.setText("Passwords do not match");
            return;
        }

        authService.register(username, password).ifPresentOrElse(user -> {
            errorLabel.setText(" ");
            clearFields();
            gameService.setCurrentUser(user);
            appFrame.onAuthenticated(user);
        }, () -> errorLabel.setText("Unable to register (username taken or invalid input)"));
    }

    private void clearFields() {
        usernameField.setText("");
        passwordField.setText("");
        confirmField.setText("");
    }
}