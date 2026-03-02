package com.mathpuzzlegame.net;

import java.net.URI;
import java.util.List;

public class MusicApi {

    public URI getBackgroundMusicUri() {
        return URI.create("https://www2.cs.uic.edu/~i101/SoundFiles/StarWars60.wav");
    }

    public List<URI> getCandidateBackgroundMusicUris() {
        return List.of(
                // Short public WAV samples; if any of these domains are blocked
                // on the local network, MusicService will quietly fall back.
                URI.create("https://samplelib.com/lib/preview/wav/sample-3s.wav"),
                URI.create("https://samplelib.com/lib/preview/wav/sample-6s.wav")
        );
    }
}