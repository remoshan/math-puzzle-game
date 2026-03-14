package com.mathpuzzlegame.service;

import com.mathpuzzlegame.net.DogApi;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.Proxy;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.*;

/**
 * Loads card images from the Dog CEO public API.
 * Each card pair displays the same randomly fetched dog photo.
 */
public class ImageService {

    private static final int MAX_RETRIES = 3;

    private final ExecutorService imageExecutor = Executors.newFixedThreadPool(4, r -> {
        Thread t = new Thread(r, "image-loader");
        t.setDaemon(true);
        return t;
    });

    private final Map<String, ImageIcon> urlCache = new ConcurrentHashMap<>();

    public ImageService() {}

    // ── Public API ────────────────────────────────────────────────────────────

    /**
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
                    return icon;
                } else {
                    System.err.println("[ImageService] ImageIO.read returned null for: " + imageUrl);
                }
            } catch (Exception e) {
                System.err.println("[ImageService] Attempt " + attempt + " failed: " + e.getMessage());
                if (attempt < MAX_RETRIES) {
                    try { Thread.sleep(500L * attempt); } catch (InterruptedException ignored) {}
                }
            }
        }

        System.err.println("[ImageService] All retries failed for: " + imageUrl + " — using placeholder.");
        return createPlaceholderIcon(iconSize);
    }

    private BufferedImage downloadWithTimeouts(String urlStr) throws Exception {
        URL url = new URL(urlStr);

        // Proxy.NO_PROXY bypasses system proxy settings that cause "Failed to select a proxy"
        HttpURLConnection con = (HttpURLConnection) url.openConnection(Proxy.NO_PROXY);
        con.setConnectTimeout(8_000);
        con.setReadTimeout(12_000);
        con.setRequestMethod("GET");
        con.setInstanceFollowRedirects(true);
        con.setRequestProperty("User-Agent",
                "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36");
        con.setRequestProperty("Accept", "image/jpeg,image/png,image/*,*/*");

        int responseCode = con.getResponseCode();
        System.out.println("[ImageService] HTTP " + responseCode + " for " + urlStr);

        if (responseCode != HttpURLConnection.HTTP_OK) {
            throw new Exception("HTTP error: " + responseCode);
        }

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
            g2.setColor(Color.WHITE);
            g2.setFont(new Font(Font.SANS_SERIF, Font.BOLD, iconSize / 3));
            FontMetrics fm = g2.getFontMetrics();
            String q = "?";
            g2.drawString(q, (iconSize - fm.stringWidth(q)) / 2,
                    (iconSize - fm.getHeight()) / 2 + fm.getAscent());
        } finally {
            g2.dispose();
        }
        return new ImageIcon(img);
    }
}