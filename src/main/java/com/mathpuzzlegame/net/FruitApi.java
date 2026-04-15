package com.mathpuzzlegame.net;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.Proxy;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Fetches fruit image URLs from two external REST APIs:
 *
 *  1. Fruityvice API  – https://www.fruityvice.com/api/fruit/all
 *     Returns a JSON array of real fruit names.
 *
 *  2. Wikipedia REST API – https://en.wikipedia.org/api/rest_v1/page/summary/{name}
 *     Returns a 330px thumbnail URL for each fruit. These are pre-cached by
 *     Wikimedia's CDN and do not trigger rate-limiting like 240px thumbnails do.
 *
 * Note: Only Wikipedia thumbnail (330px) URLs are used. No hardcoded fallback
 * URLs are needed — the Wikipedia API is reliable and covers all 49 fruits.
 */
public class FruitApi {

    private static final String FRUITYVICE_URL = "https://www.fruityvice.com/api/fruit/all";
    private static final String WIKIPEDIA_URL  = "https://en.wikipedia.org/api/rest_v1/page/summary/";

    // Stagger between Wikipedia metadata calls (milliseconds)
    private static final int WIKIPEDIA_STAGGER_MS = 300;

    // ── Public API ────────────────────────────────────────────────────────────

    /**
     * Fetches all fruit names from Fruityvice, then fetches a 330px thumbnail
     * URL for each from Wikipedia. Returns a deduplicated map of
     * fruitName → imageUrl.
     *
     * Called once at startup; results are cached by ImageService.
     */
    public static Map<String, String> fetchAllFruitImageUrls() {
        System.out.println("[FruitApi] Fetching fruit list from Fruityvice...");
        List<String> fruitNames = fetchFruitNamesFromFruityvice();

        if (fruitNames.isEmpty()) {
            System.err.println("[FruitApi] Fruityvice unavailable — using built-in list.");
            fruitNames = getBuiltInFruitNames();
        }

        Map<String, String> result = new LinkedHashMap<>();

        for (int i = 0; i < fruitNames.size(); i++) {
            String fruit = fruitNames.get(i);
            if (i > 0) {
                try { Thread.sleep(WIKIPEDIA_STAGGER_MS); } catch (InterruptedException ignored) {}
            }
            String url = fetchWikipediaThumbnail(fruit);
            if (url != null && !result.containsValue(url)) {
                result.put(fruit, url);
                System.out.println("[FruitApi] ✓ " + fruit + " → " + url);
            }
        }

        System.out.println("[FruitApi] Ready — " + result.size() + " unique fruit URLs fetched.");
        return result;
    }

    /**
     * Picks {@code count} shuffled image URLs from a pre-built map.
     */
    public static String[] pickUrls(Map<String, String> allUrls, int count) {
        List<String> urls = new ArrayList<>(allUrls.values());
        Collections.shuffle(urls);
        String[] result = new String[count];
        for (int i = 0; i < count; i++) {
            result[i] = urls.get(i % urls.size());
        }
        return result;
    }

    // ── Step 1: Fruityvice ────────────────────────────────────────────────────

    private static List<String> fetchFruitNamesFromFruityvice() {
        List<String> names = new ArrayList<>();
        try {
            String json = httpGet(FRUITYVICE_URL);
            if (json == null || json.isEmpty()) return names;
            int idx = 0;
            while (true) {
                int nameKey = json.indexOf("\"name\"", idx);
                if (nameKey < 0) break;
                int colon  = json.indexOf(":", nameKey);
                int qOpen  = json.indexOf("\"", colon + 1);
                int qClose = json.indexOf("\"", qOpen + 1);
                if (colon < 0 || qOpen < 0 || qClose < 0) break;
                String name = json.substring(qOpen + 1, qClose).trim();
                if (!name.isEmpty()) names.add(name);
                idx = qClose + 1;
            }
            System.out.println("[FruitApi] Fruityvice returned " + names.size() + " fruits.");
        } catch (Exception e) {
            System.err.println("[FruitApi] Fruityvice error: " + e.getMessage());
        }
        return names;
    }

    // ── Step 2: Wikipedia thumbnail ───────────────────────────────────────────

    /**
     * Calls the Wikipedia REST API and returns the thumbnail.source URL.
     * These are 330px images, pre-cached by Wikimedia CDN — no rate-limiting.
     */
    private static String fetchWikipediaThumbnail(String fruitName) {
        try {
            String encoded = URLEncoder.encode(fruitName, StandardCharsets.UTF_8)
                    .replace("+", "_");
            String json = httpGet(WIKIPEDIA_URL + encoded);
            if (json == null) return null;
            return extractThumbnailSource(json);
        } catch (Exception e) {
            System.err.println("[FruitApi] Wikipedia error for '" + fruitName
                    + "': " + e.getMessage());
            return null;
        }
    }

    // ── Built-in fruit name list (fallback if Fruityvice is down) ─────────────

    private static List<String> getBuiltInFruitNames() {
        List<String> names = new ArrayList<>();
        String[] builtIn = {
                "Apple", "Banana", "Strawberry", "Orange", "Kiwi", "Pineapple",
                "Mango", "Watermelon", "Blueberry", "Raspberry", "Lemon", "Pear",
                "Grape", "Peach", "Cherry", "Plum", "Lime", "Avocado",
                "Pomegranate", "Apricot", "Guava", "Papaya", "Melon", "Tangerine",
                "Lychee", "Jackfruit", "Durian", "Persimmon", "Feijoa", "Cranberry",
                "Fig", "Blackberry", "Gooseberry", "Passionfruit", "Pomelo", "Mango"
        };
        for (String s : builtIn) names.add(s);
        return names;
    }

    // ── JSON helpers ──────────────────────────────────────────────────────────

    private static String extractThumbnailSource(String json) {
        int thumbIdx = json.indexOf("\"thumbnail\"");
        if (thumbIdx < 0) return null;
        int sourceIdx = json.indexOf("\"source\"", thumbIdx);
        if (sourceIdx < 0) return null;
        int colon  = json.indexOf(":", sourceIdx);
        int qOpen  = json.indexOf("\"", colon + 1);
        int qClose = json.indexOf("\"", qOpen + 1);
        if (colon < 0 || qOpen < 0 || qClose < 0) return null;
        return json.substring(qOpen + 1, qClose).replace("\\/", "/");
    }

    // ── HTTP helper ───────────────────────────────────────────────────────────

    private static String httpGet(String urlStr) throws Exception {
        URL url = new URL(urlStr);
        HttpURLConnection con = (HttpURLConnection) url.openConnection(Proxy.NO_PROXY);
        con.setConnectTimeout(8_000);
        con.setReadTimeout(10_000);
        con.setRequestMethod("GET");
        con.setRequestProperty("User-Agent",
                "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36");
        con.setRequestProperty("Accept", "application/json");
        int code = con.getResponseCode();
        if (code != HttpURLConnection.HTTP_OK) throw new Exception("HTTP " + code);
        StringBuilder sb = new StringBuilder();
        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(con.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            while ((line = br.readLine()) != null) sb.append(line);
        }
        return sb.toString().trim();
    }
}