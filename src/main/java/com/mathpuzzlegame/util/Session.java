package com.mathpuzzlegame.util;

public class Session {

    private static String loggedInUsername;

    public static String getLoggedInUsername() {
        return loggedInUsername;
    }

    public static void setLoggedInUsername(String username) {
        loggedInUsername = username;
    }

    public static boolean isUserLoggedIn() {
        return loggedInUsername != null && !loggedInUsername.isEmpty();
    }

    public static void logout() {
        loggedInUsername = null;
    }
}