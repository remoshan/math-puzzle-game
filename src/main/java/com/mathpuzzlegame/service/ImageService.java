package com.mathpuzzlegame.service;

import com.mathpuzzlegame.net.BananaApi;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ConcurrentHashMap;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.imageio.ImageIO;

/**
 * Wraps the BananaApi and provides Swing-friendly image loading
 * for the game cards. Images are loaded asynchronously so the UI
 * thread stays responsive.
 */
public class ImageService {

    private final BananaApi bananaApi;
    private final ExecutorService imageExecutor = Executors.newFixedThreadPool(4, r -> {
        Thread t = new Thread(r, "image-loader");
        t.setDaemon(true);
        return t;
    });
    private final Map<String, ImageIcon> urlCache = new ConcurrentHashMap<>();
    private volatile boolean loggedPlaceholderNotice;

    public ImageService(BananaApi bananaApi) {
        this.bananaApi = bananaApi;
    }

    /**
     * Requests a set of images from BananaApi and returns a mapping
     * from logical card index to an ImageIcon future. Consumers can
     * attach callbacks to update card buttons when each image is ready.
     */
    public Map<Integer, CompletableFuture<ImageIcon>> loadCardImagesAsync(int pairCount, int iconSize) {
        Map<Integer, CompletableFuture<ImageIcon>> futures = new HashMap<>();
        // Fetch the IDs on a worker thread so we never block the Swing EDT.
        CompletableFuture<String[]> idsFuture =
                CompletableFuture.supplyAsync(() -> BananaApi.fetchImages(pairCount), imageExecutor);

        for (int i = 0; i < pairCount; i++) {
            final int index = i;
            futures.put(index, idsFuture.thenApplyAsync(ids -> {
                String raw = (ids != null && ids.length > index) ? ids[index] : ("card-" + index);
                return createIconFromId(normalizeId(raw), iconSize);
            }, imageExecutor));
        }
        return futures;
    }

    private String normalizeId(String raw) {
        if (raw == null) {
            return "";
        }
        String s = raw.trim();

        // If the BananaApi parsing produced fragments like {url:https://...}
        // or url:https:\/\/..., extract the URL portion.
        int httpIdx = s.indexOf("http://");
        if (httpIdx < 0) {
            httpIdx = s.indexOf("https://");
        }
        if (httpIdx >= 0) {
            s = s.substring(httpIdx).trim();
        }

        // Unescape common JSON escapes for URLs
        s = s.replace("\\/", "/");

        // Strip trailing braces/quotes/commas
        s = s.replaceAll("[\"\\}\\]]+$", "");
        s = s.replaceAll("^[\"\\{\\[]+", "");
        return s.trim();
    }

    private ImageIcon createIconFromId(String id, int iconSize) {
        // If Banana API returns a URL, attempt to load it directly
        if (id != null && (id.startsWith("http://") || id.startsWith("https://"))) {
            ImageIcon cached = urlCache.get(id);
            if (cached != null) {
                return cached;
            }
            try {
                BufferedImage img = downloadImageWithTimeouts(id);
                if (img != null) {
                    ImageIcon icon = new ImageIcon(scaleToSquare(img, iconSize));
                    urlCache.put(id, icon);
                    return icon;
                }
            } catch (Exception ignored) {
                // Fallback to generated icon below
            }
        }

        // Log once per app run when we are not using remote Banana image URLs
        if (!loggedPlaceholderNotice) {
            loggedPlaceholderNotice = true;
            System.out.println("Banana API did not provide usable image URLs. Using Banana-generated local card art based on Banana IDs.");
        }

        // Fallback: generate a vivid Apple-style "banana tile" based on the Banana ID.
        BufferedImage img = new BufferedImage(iconSize, iconSize, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = img.createGraphics();
        try {
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int hash = id != null ? id.hashCode() : 0;

            // Soft background
            Color bg = new Color(245, 245, 248);
            g2.setColor(bg);
            g2.fillRoundRect(0, 0, iconSize, iconSize, iconSize / 3, iconSize / 3);

            // Banana-colored inner shape with slight variation per ID
            int hueShift = (hash & 0xFF);
            float h = (45 + (hueShift % 60)) / 360f; // yellow/orange band
            float s = 0.7f;
            float b = 0.95f;
            Color banana = Color.getHSBColor(h, s, b);
            int inset = iconSize / 7;
            g2.setColor(banana);
            g2.fillRoundRect(inset, inset, iconSize - inset * 2, iconSize - inset * 2,
                    iconSize / 2, iconSize / 2);

            // Accent curve to suggest a banana silhouette with variation
            g2.setColor(new Color(190, 150, 40, 220));
            g2.setStroke(new BasicStroke(3f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            int cx = iconSize / 2;
            int cy = iconSize / 2;
            int r = iconSize / 3;
            int start = 10 + (hash % 40);
            int extent = 120 + (hash % 60);
            g2.drawArc(cx - r, cy - r / 2, r * 2, r, start, extent);

            // Subtle border
            g2.setColor(new Color(0, 0, 0, 40));
            g2.setStroke(new BasicStroke(1.2f));
            g2.drawRoundRect(0, 0, iconSize - 1, iconSize - 1, iconSize / 3, iconSize / 3);
        } finally {
            g2.dispose();
        }
        return new ImageIcon(img);
    }

    private BufferedImage downloadImageWithTimeouts(String urlStr) throws Exception {
        URL url = new URL(urlStr);
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setConnectTimeout(5000);
        con.setReadTimeout(8000);
        con.setRequestMethod("GET");
        con.setInstanceFollowRedirects(true);
        try (InputStream in = con.getInputStream()) {
            return ImageIO.read(in);
        }
    }

    private BufferedImage scaleToSquare(BufferedImage src, int size) {
        BufferedImage dst = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = dst.createGraphics();
        try {
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.drawImage(src, 0, 0, size, size, null);
        } finally {
            g2.dispose();
        }
        return dst;
    }
}

