package higtools.utils;

import java.awt.*;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

public class ServiceLoader {

    public static void load() {
    OSUtils.init(); // setup current os for stuff like spotify
    SpotifyService.init();
    Runtime.getRuntime().addShutdownHook(new Thread(ThreadLoader::shutdown));
    }
}

