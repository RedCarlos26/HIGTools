package higtools.utils;

import com.sun.jna.Platform;

public class OSUtils {

    public static boolean isWindows = false;

    public static void init() {
        if (getOS().equals(OSType.Windows)) isWindows = true;
    }

    public static OSType getOS() {
        if (Platform.isWindows()) return OSType.Windows;
        if (Platform.isLinux()) return OSType.Linux;
        if (Platform.isMac()) return OSType.Mac;
        return OSType.Unsupported;

    }

    public enum OSType {
        Windows,
        Linux,
        Mac,
        Unsupported
    }

}
