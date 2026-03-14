package com.mathpuzzlegame.net;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.Proxy;
import java.net.URL;

/**
 * Client for the Dog CEO public API.
 * Endpoint: https://dog.ceo/api/breeds/image/random
 * Response:  { "message": "https://images.dog.ceo/...", "status": "success" }
 * No API key required.
 */
public class DogApi {

    private static final String API_URL = "https://dog.ceo/api/breeds/image/random";

    /** Fetches one random dog image URL. Returns null on failure. */
    public static String fetchImageUrl() {
        try {
            URL url = new URL(API_URL);

            // Proxy.NO_PROXY bypasses any system proxy that causes "Failed to select a proxy"
            HttpURLConnection con = (HttpURLConnection) url.openConnection(Proxy.NO_PROXY);
            con.setConnectTimeout(8_000);
            con.setReadTimeout(10_000);
            con.setRequestMethod("GET");
            con.setRequestProperty("User-Agent",
                    "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36");
            con.setRequestProperty("Accept", "application/json");

            StringBuilder sb = new StringBuilder();
            try (BufferedReader br = new BufferedReader(
                    new InputStreamReader(con.getInputStream()))) {
                String line;
                while ((line = br.readLine()) != null) sb.append(line);
            }

            String raw = sb.toString().trim();
            System.out.println("[DogApi] Raw response: " + raw);

            String imageUrl = extractMessage(raw);

            // Fix escaped forward slashes returned by the API: \/ → /
            if (imageUrl != null) {
                imageUrl = imageUrl.replace("\\/", "/");
            }

            System.out.println("[DogApi] Parsed URL: " + imageUrl);
            return imageUrl;

        } catch (Exception e) {
            System.err.println("[DogApi] fetchImageUrl error: " + e.getMessage());
            return null;
        }
    }

    /**
     * Fetches {@code count} unique dog image URLs concurrently.
     * Each URL is used for one matching card pair.
     */
    public static String[] fetchImageUrls(int count) {
        String[] results = new String[count];
        Thread[] threads = new Thread[count];

        for (int i = 0; i < count; i++) {
            final int idx = i;
            threads[i] = new Thread(() -> results[idx] = fetchImageUrl());
            threads[i].start();
        }
        for (Thread t : threads) {
            try { t.join(); } catch (InterruptedException ignored) {}
        }
        return results;
    }

    // ── Minimal JSON parser ───────────────────────────────────────────────────

    private static String extractMessage(String json) {
        // Format: { "message" : "https:\/\/...", "status": "success" }
        int keyIdx = json.indexOf("\"message\"");
        if (keyIdx < 0) return null;
        int colon  = json.indexOf(":", keyIdx);
        int qOpen  = json.indexOf("\"", colon + 1);
        int qClose = json.indexOf("\"", qOpen + 1);
        if (colon < 0 || qOpen < 0 || qClose < 0) return null;
        return json.substring(qOpen + 1, qClose);
    }
}