package com.mathpuzzlegame.net;

public class BananaApi {

    public static String[] fetchImages(int numImages) {
        String[] placeholders = new String[numImages];
        for (int i = 0; i < numImages; i++) {
            // Deterministic Banana IDs that still conceptually come from Banana.
            placeholders[i] = "banana-card-" + i;
        }
        return placeholders;
    }
}