package com.mathpuzzlegame.service;

<<<<<<< Updated upstream
import com.mathpuzzlegame.net.DogApi;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
=======
import com.mathpuzzlegame.net.FruitApi;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
>>>>>>> Stashed changes
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.Proxy;
import java.net.URL;
<<<<<<< Updated upstream
=======
import java.util.ArrayList;
import java.util.Collections;
>>>>>>> Stashed changes
import java.util.HashMap;
import java.util.List;
import java.util.Map;
<<<<<<< Updated upstream
import java.util.concurrent.*;

/**
 * Loads card images from the Dog CEO public API.
 * Each card pair displays the same randomly fetched dog photo.
 */
public class ImageService {

    private static final int MAX_RETRIES = 3;

    private final ExecutorService imageExecutor = Executors.newFixedThreadPool(4, r -> {
        Thread t = new Thread(r, "image-loader");
=======
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Loads fruit card images using FruitApi (Fruityvice + Wikipedia 330px URLs).
 *
 * On startup, Wikipedia metadata is fetched in the background (JSON only, fast).
 * When a game starts, loadGameImages() returns a single CompletableFuture that
 * resolves with a complete Map<pairIndex → ImageIcon> once every image for that
 * game has been downloaded. GameView waits for this future before building the
 * card grid, so every card always has its image ready when first shown.
 */
public class ImageService {

    // Stagger between consecutive image downloads to avoid Wikimedia 429s
    private static final int STAGGER_MS  = 1200;
    private static final int RETRY_429_MS = 4_000;
    private static final int MAX_RETRIES  = 3;

    // Single background thread — all network work is serialised here
    private final ExecutorService bgExecutor = Executors.newSingleThreadExecutor(r -> {
        Thread t = new Thread(r, "fruit-loader");
>>>>>>> Stashed changes
        t.setDaemon(true);
        return t;
    });

<<<<<<< Updated upstream
    private final Map<String, ImageIcon> urlCache = new ConcurrentHashMap<>();

    public ImageService() {}

    // ── Public API ────────────────────────────────────────────────────────────
=======
    // imageUrl → ImageIcon, reused across games
    private final Map<String, ImageIcon> iconCache = new ConcurrentHashMap<>();

    // fruitName → imageUrl, populated once URL collection completes
    private volatile Map<String, String> fruitUrlMap = Collections.emptyMap();

    // Completes when Wikipedia URL collection finishes (JSON only, no image bytes)
    private final CompletableFuture<Void> urlsReady = new CompletableFuture<>();

    public ImageService() {
        // Collect all Wikipedia image URLs in the background immediately
        bgExecutor.submit(this::collectUrls);
    }
>>>>>>> Stashed changes

    // ── Public API ────────────────────────────────────────────────────────────

    /**
<<<<<<< Updated upstream
     * Returns a mapping from card-pair index → ImageIcon future.
     * Each future resolves once its dog image has been downloaded.
     */
    public Map<Integer, CompletableFuture<ImageIcon>> loadCardImagesAsync(int pairCount, int iconSize) {
        Map<Integer, CompletableFuture<ImageIcon>> futures = new HashMap<>();

        CompletableFuture<String[]> urlsFuture =
                CompletableFuture.supplyAsync(
                        () -> DogApi.fetchImageUrls(pairCount), imageExecutor);

        for (int i = 0; i < pairCount; i++) {
            final int index = i;
            futures.put(index, urlsFuture.thenApplyAsync(urls -> {
                String url = (urls != null && urls.length > index) ? urls[index] : null;
                System.out.println("[ImageService] Pair " + index + " → URL: " + url);
                return buildIcon(url, iconSize);
            }, imageExecutor));
        }

        return futures;
    }

    // ── Image building ────────────────────────────────────────────────────────

    private ImageIcon buildIcon(String imageUrl, int iconSize) {
        if (imageUrl == null || !imageUrl.startsWith("http")) {
            System.err.println("[ImageService] Invalid or null URL, using placeholder.");
            return createPlaceholderIcon(iconSize);
        }

        // Return cached icon if already downloaded
        ImageIcon cached = urlCache.get(imageUrl);
        if (cached != null) return cached;

        // Attempt download with retries
        for (int attempt = 1; attempt <= MAX_RETRIES; attempt++) {
            try {
                System.out.println("[ImageService] Attempt " + attempt + " downloading: " + imageUrl);
                BufferedImage img = downloadWithTimeouts(imageUrl);
                if (img != null) {
                    ImageIcon icon = new ImageIcon(scaleToSquare(img, iconSize));
                    urlCache.put(imageUrl, icon);
                    System.out.println("[ImageService] ✓ Downloaded successfully: " + imageUrl);
=======
     * Returns a CompletableFuture that resolves with a complete map of
     * pairIndex → ImageIcon once every image for the current game has been
     * downloaded. The caller (GameView) should wait for this future before
     * building the card grid.
     *
     * Always runs on bgExecutor — never blocks the EDT.
     */
    public CompletableFuture<Map<Integer, ImageIcon>> loadGameImages(
            int pairCount, int iconSize) {

        return urlsReady.thenApplyAsync(v -> {
            // Pick pairCount URLs from the available fruit map
            String[] urls = FruitApi.pickUrls(fruitUrlMap, pairCount);
            System.out.println("[ImageService] Downloading " + pairCount
                    + " images for game...");

            Map<Integer, ImageIcon> result = new HashMap<>();
            for (int i = 0; i < pairCount; i++) {
                if (i > 0) {
                    try { Thread.sleep(STAGGER_MS); }
                    catch (InterruptedException ignored) {}
                }
                String url = (urls != null && i < urls.length) ? urls[i] : null;
                ImageIcon icon = downloadAndCache(url, iconSize);
                result.put(i, icon);
                System.out.println("[ImageService] Ready " + (i + 1)
                        + "/" + pairCount);
            }

            System.out.println("[ImageService] All " + pairCount
                    + " game images ready.");
            return result;

        }, bgExecutor); // Always runs on bgExecutor, NEVER on EDT
    }

    // ── URL collection ────────────────────────────────────────────────────────

    private void collectUrls() {
        try {
            System.out.println("[ImageService] Collecting fruit image URLs...");
            fruitUrlMap = FruitApi.fetchAllFruitImageUrls();
            System.out.println("[ImageService] "
                    + fruitUrlMap.size() + " fruit URLs ready.");
            urlsReady.complete(null);
        } catch (Exception e) {
            System.err.println("[ImageService] URL collection failed: "
                    + e.getMessage());
            urlsReady.complete(null); // unblock games even on failure
        }
    }

    // ── Image downloading ─────────────────────────────────────────────────────

    private ImageIcon downloadAndCache(String imageUrl, int iconSize) {
        if (imageUrl == null || !imageUrl.startsWith("http")) {
            return createPlaceholderIcon(iconSize);
        }

        // Return cached icon immediately if already downloaded
        ImageIcon cached = iconCache.get(imageUrl);
        if (cached != null) {
            System.out.println("[ImageService] Cache hit: " + imageUrl);
            return cached;
        }

        for (int attempt = 1; attempt <= MAX_RETRIES; attempt++) {
            try {
                BufferedImage img = downloadWithTimeouts(imageUrl);
                if (img != null) {
                    ImageIcon icon = new ImageIcon(scaleToSquare(img, iconSize));
                    iconCache.put(imageUrl, icon);
                    System.out.println("[ImageService] ✓ " + imageUrl);
>>>>>>> Stashed changes
                    return icon;
                } else {
                    System.err.println("[ImageService] ImageIO.read returned null for: " + imageUrl);
                }
            } catch (Exception e) {
                System.err.println("[ImageService] Attempt " + attempt + " failed: " + e.getMessage());
                if (attempt < MAX_RETRIES) {
                    try { Thread.sleep(500L * attempt); } catch (InterruptedException ignored) {}
                }
<<<<<<< Updated upstream
            }
        }

        System.err.println("[ImageService] All retries failed for: " + imageUrl + " — using placeholder.");
=======
            } catch (RateLimitException e) {
                System.err.println("[ImageService] 429 attempt " + attempt
                        + " — waiting " + RETRY_429_MS + "ms");
                try { Thread.sleep(RETRY_429_MS); }
                catch (InterruptedException ignored) {}
            } catch (Exception e) {
                System.err.println("[ImageService] Attempt " + attempt
                        + " failed: " + e.getMessage());
                try { Thread.sleep(1_000L * attempt); }
                catch (InterruptedException ignored) {}
            }
        }

        System.err.println("[ImageService] All retries exhausted: " + imageUrl);
>>>>>>> Stashed changes
        return createPlaceholderIcon(iconSize);
    }

    private BufferedImage downloadWithTimeouts(String urlStr) throws Exception {
        URL url = new URL(urlStr);
<<<<<<< Updated upstream

        // Proxy.NO_PROXY bypasses system proxy settings that cause "Failed to select a proxy"
        HttpURLConnection con = (HttpURLConnection) url.openConnection(Proxy.NO_PROXY);
        con.setConnectTimeout(8_000);
        con.setReadTimeout(12_000);
=======
        HttpURLConnection con = (HttpURLConnection) url.openConnection(Proxy.NO_PROXY);
        con.setConnectTimeout(8_000);
        con.setReadTimeout(15_000);
>>>>>>> Stashed changes
        con.setRequestMethod("GET");
        con.setInstanceFollowRedirects(true);
        con.setRequestProperty("User-Agent",
                "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36");
        con.setRequestProperty("Accept", "image/jpeg,image/png,image/*,*/*");
<<<<<<< Updated upstream

        int responseCode = con.getResponseCode();
        System.out.println("[ImageService] HTTP " + responseCode + " for " + urlStr);

        if (responseCode != HttpURLConnection.HTTP_OK) {
            throw new Exception("HTTP error: " + responseCode);
        }

=======
        int code = con.getResponseCode();
        if (code == 429) throw new RateLimitException();
        if (code != HttpURLConnection.HTTP_OK) throw new Exception("HTTP " + code);
>>>>>>> Stashed changes
        try (InputStream in = con.getInputStream()) {
            return ImageIO.read(in);
        }
    }

    // ── Scaling ───────────────────────────────────────────────────────────────

    private BufferedImage scaleToSquare(BufferedImage src, int size) {
        BufferedImage dst = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = dst.createGraphics();
        try {
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                    RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g2.setRenderingHint(RenderingHints.KEY_RENDERING,
                    RenderingHints.VALUE_RENDER_QUALITY);
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
            g2.drawImage(src, 0, 0, size, size, null);
        } finally {
            g2.dispose();
        }
        return dst;
    }

<<<<<<< Updated upstream
    // ── Placeholder (shown only if all retries fail) ──────────────────────────

    private ImageIcon createPlaceholderIcon(int iconSize) {
        BufferedImage img = new BufferedImage(iconSize, iconSize, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = img.createGraphics();
        try {
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(new Color(180, 180, 190));
            g2.fillRoundRect(2, 2, iconSize - 4, iconSize - 4, iconSize / 3, iconSize / 3);
            g2.setColor(new Color(140, 140, 150));
            g2.setStroke(new BasicStroke(1.5f));
            g2.drawRoundRect(2, 2, iconSize - 4, iconSize - 4, iconSize / 3, iconSize / 3);
=======
    // ── Placeholder ───────────────────────────────────────────────────────────

    private ImageIcon createPlaceholderIcon(int iconSize) {
        BufferedImage img = new BufferedImage(iconSize, iconSize,
                BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = img.createGraphics();
        try {
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(new Color(180, 180, 190));
            g2.fillRoundRect(2, 2, iconSize - 4, iconSize - 4,
                    iconSize / 3, iconSize / 3);
            g2.setColor(new Color(140, 140, 150));
            g2.setStroke(new BasicStroke(1.5f));
            g2.drawRoundRect(2, 2, iconSize - 4, iconSize - 4,
                    iconSize / 3, iconSize / 3);
>>>>>>> Stashed changes
            g2.setColor(Color.WHITE);
            g2.setFont(new Font(Font.SANS_SERIF, Font.BOLD, iconSize / 3));
            FontMetrics fm = g2.getFontMetrics();
            String q = "?";
<<<<<<< Updated upstream
            g2.drawString(q, (iconSize - fm.stringWidth(q)) / 2,
=======
            g2.drawString(q,
                    (iconSize - fm.stringWidth(q)) / 2,
>>>>>>> Stashed changes
                    (iconSize - fm.getHeight()) / 2 + fm.getAscent());
        } finally {
            g2.dispose();
        }
        return new ImageIcon(img);
    }
<<<<<<< Updated upstream
=======

    // ── Custom exception for HTTP 429 ─────────────────────────────────────────

    private static class RateLimitException extends Exception {
        RateLimitException() { super("HTTP 429 Too Many Requests"); }
    }
>>>>>>> Stashed changes
}