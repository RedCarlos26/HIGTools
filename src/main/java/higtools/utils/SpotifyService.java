package higtools.utils;

import higtools.utils.OSUtils;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Stream;

public class SpotifyService {

    public static boolean isSpotifyRunning;
    public static String currentTrack;
    public static String lastTrack;
    public static String currentArtist;

    public static void init() {
        ThreadLoader.schedueled.scheduleAtFixedRate(SpotifyService::updateCurrentTrack, 10000, 100, TimeUnit.MILLISECONDS); // wait a bit before starting to not interfere with client startup

    }

    public static void reset() {
        currentTrack = null;
        currentArtist = null;
        lastTrack = null;
    }

    public static void updateCurrentTrack() {
        isSpotifyRunning = isSpotifyActive();
        if (!isSpotifyRunning) {
            reset();
            return;
        }
        String[] metadata = getCurrentTrack();
        if (metadata == null) {
            reset();
            return;
        }
        String artist = metadata[0];
        String track = metadata[1];
        if (artist == null || track == null) {
            reset();
            return;
        }
        currentArtist = artist.trim();
        currentTrack = track.trim();
    }

    public static boolean isSpotifyActive() {
        AtomicBoolean isRunning = new AtomicBoolean(false);
        Stream<ProcessHandle> liveProcesses = ProcessHandle.allProcesses();
        liveProcesses.filter(ProcessHandle::isAlive).forEach(ph -> {
            if (ph.info().command().toString().contains("Spotify") || ph.info().command().toString().contains("spotify")) isRunning.set(true);
        });
        return isRunning.get();
    }

    public static boolean hasMedia() {
        return isSpotifyRunning && currentTrack != null && currentArtist != null;
    }

    public static String getMedia() {
        if (!hasMedia()) return "No song is playing.";
        return currentArtist + " - " + currentTrack;
    }

    public static String getMediaReverse() {
        if (!hasMedia()) return "No song is playing.";
        return currentTrack + " - " + currentArtist;
    }

    public static String getMediaFull() {
        if (!hasMedia()) return "No song is playing.";
        return "Playing " + currentTrack + " by " + currentArtist;
    }

    public static String[] getCurrentTrack() {
        if (!OSUtils.isWindows) return null;
        ArrayList<String> results = new ArrayList<>();
        try {
            ProcessBuilder builder = new ProcessBuilder("cmd", "/c", "for /f \"tokens=* skip=9 delims= \" %g in ('tasklist /v /fo list /fi \"imagename eq spotify*\"') do @echo %g"); // lists all process titles
            builder.redirectErrorStream(true); // redirect the error stream
            Process p = builder.start();
            BufferedReader r = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String line;
            while (true) {
                line = r.readLine();
                if (line == null) { break; }
                if (line.contains("Window Title:")) results.add(line); // store the titles (spotify can have multiple titles)
            }
        } catch (IOException e) {return null;}
        if (results.isEmpty()) return null;
        String songData = "";
        for (String line: results) { // find the correct title (spotify puts a dash between the title and artist)
            if (line.contains("-")) {
                songData = line;
                break;
            }
        }
        if (songData.equals("") || songData.isBlank()) return null;
        songData = songData.replace("Window Title: ", "");
        return songData.split("-", 0); // split the title into title and artist
    }


}
