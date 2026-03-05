package com.mathpuzzlegame.service;

import com.mathpuzzlegame.data.DatabaseManager;
import com.mathpuzzlegame.model.User;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.Optional;

public class AuthService {

    private final DatabaseManager databaseManager;

    public AuthService(DatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
    }

    public Optional<User> login(String username, String passwordPlain) {
        if (username == null || username.isBlank() || passwordPlain == null || passwordPlain.isEmpty()) {
            return Optional.empty();
        }
        String passwordHash = hashPassword(passwordPlain);
        Optional<User> userOpt = databaseManager.findUserByUsername(username.trim());
        if (userOpt.isPresent() && userOpt.get().getPasswordHash().equals(passwordHash)) {
            return userOpt;
        }
        return Optional.empty();
    }

    public Optional<User> register(String username, String passwordPlain) {
        if (username == null || username.isBlank() || passwordPlain == null || passwordPlain.length() < 4) {
            return Optional.empty();
        }
        String cleanUsername = username.trim();
        if (databaseManager.findUserByUsername(cleanUsername).isPresent()) {
            return Optional.empty();
        }
        String passwordHash = hashPassword(passwordPlain);
        boolean ok = databaseManager.createUser(cleanUsername, passwordHash);
        if (!ok) {
            return Optional.empty();
        }
        return databaseManager.findUserByUsername(cleanUsername);
    }

    private String hashPassword(String passwordPlain) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest(passwordPlain.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }
}