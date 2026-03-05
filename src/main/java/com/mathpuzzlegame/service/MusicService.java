package com.mathpuzzlegame.service;

import com.mathpuzzlegame.net.MusicApi;

import javax.sound.sampled.*;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Handles background music playback on a worker thread so that
 * the Swing event dispatch thread remains responsive.
 */
public class MusicService {

    private final MusicApi musicApi;
    private final ExecutorService executor = Executors.newSingleThreadExecutor(r -> {
        Thread t = new Thread(r, "music-player");
        t.setDaemon(true);
        return t;
    });

    private Clip clip;
    private final AtomicBoolean muted = new AtomicBoolean(false);
    private final AtomicBoolean requested = new AtomicBoolean(false);
    private final AtomicBoolean workerRunning = new AtomicBoolean(false);
    private volatile float volume = 0.8f; // 0.0 – 1.0
    private volatile String lastError;

    public MusicService(MusicApi musicApi) {
        this.musicApi = musicApi;
    }

    public boolean isMuted() {
        return muted.get();
    }

    public boolean isPlaying() {
        Clip c = clip;
        return requested.get() && !muted.get() && c != null && c.isOpen();
    }

    public float getVolume() {
        return volume;
    }

    public String getLastError() {
        return lastError;
    }

    /**
     * Sets output volume on a linear scale between 0.0 and 1.0.
     * If a clip is currently open the gain is adjusted immediately.
     */
    public void setVolume(float volume) {
        float clamped = Math.max(0.0f, Math.min(1.0f, volume));
        this.volume = clamped;
        applyVolumeToClip();
    }

    /**
     * Starts background music playback. If audio is muted this
     * will be a no-op until unmuted.
     */
    public void startBackgroundMusic() {
        requested.set(true);
        if (muted.get()) {
            return;
        }
        ensureWorkerRunning();
    }

    /**
     * Stops playback and releases any underlying audio resources.
     */
    public void stopBackgroundMusic() {
        requested.set(false);
        stopAndCloseClip();
    }

    /**
     * Sets mute state. When muted, any current playback is stopped.
     * When unmuted, callers should invoke {@link #startBackgroundMusic()}
     * at the appropriate time (for example when a new game begins).
     */
    public void setMuted(boolean mute) {
        boolean previous = muted.getAndSet(mute);
        if (mute && !previous) {
            stopAndCloseClip();
            return;
        }
        if (!mute && requested.get()) {
            ensureWorkerRunning();
        }
    }

    private void ensureWorkerRunning() {
        if (!workerRunning.compareAndSet(false, true)) {
            return;
        }
        executor.submit(this::workerLoop);
    }

    private void workerLoop() {
        try {
            while (requested.get() && !muted.get()) {
                boolean played = tryStartClipFromCandidates();
                if (!played) {
                    // Avoid tight spin if every source fails
                    try {
                        Thread.sleep(1500L);
                    } catch (InterruptedException ignored) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                    // If still requested, retry later (network may come back)
                    continue;
                }

                // Wait while clip is playing (or until stop/mute)
                while (requested.get() && !muted.get()) {
                    Clip c = clip;
                    if (c == null || !c.isOpen()) {
                        break;
                    }
                    try {
                        Thread.sleep(500L);
                    } catch (InterruptedException ignored) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }

                stopAndCloseClip();
            }
        } finally {
            stopAndCloseClip();
            workerRunning.set(false);
            // If we were asked to play while exiting, restart worker.
            if (requested.get() && !muted.get()) {
                ensureWorkerRunning();
            }
        }
    }

    private boolean tryStartClipFromCandidates() {
        stopAndCloseClip();
        lastError = null;

        List<java.net.URI> candidates = musicApi.getCandidateBackgroundMusicUris();
        for (java.net.URI uri : candidates) {
            if (!requested.get() || muted.get()) {
                return false;
            }
            try {
                URL url = uri.toURL();
                try (InputStream in = new BufferedInputStream(url.openStream());
                     AudioInputStream audioIn = AudioSystem.getAudioInputStream(in)) {

                    Clip localClip = AudioSystem.getClip();
                    localClip.open(audioIn);
                    clip = localClip;
                    applyVolumeToClip();
                    localClip.loop(Clip.LOOP_CONTINUOUSLY);
                    return true;
                }
            } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
                lastError = e.getClass().getSimpleName() + ": " + e.getMessage();
                // Avoid spamming stack traces if the host is unreachable on this network.
                if (!(e instanceof UnknownHostException)) {
                    e.printStackTrace();
                }
            }
        }
        return false;
    }

    private synchronized void stopAndCloseClip() {
        if (clip != null) {
            try {
                clip.stop();
                clip.close();
            } catch (Exception ignored) {
            } finally {
                clip = null;
            }
        }
    }

    private synchronized void applyVolumeToClip() {
        if (clip == null || !clip.isOpen()) {
            return;
        }
        try {
            FloatControl gainControl = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
            float min = gainControl.getMinimum();
            float max = gainControl.getMaximum();
            float range = max - min;
            float gain = min + (range * volume);
            gainControl.setValue(gain);
        } catch (IllegalArgumentException ignored) {
            // MASTER_GAIN not supported; ignore volume changes.
        }
    }
}
