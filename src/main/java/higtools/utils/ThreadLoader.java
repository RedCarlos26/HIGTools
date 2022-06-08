package higtools.utils;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public class ThreadLoader {
    public static ExecutorService auth = Executors.newCachedThreadPool();
    public static ExecutorService cached = Executors.newCachedThreadPool();
    public static ScheduledExecutorService schedueled = Executors.newScheduledThreadPool(10);
    public static ExecutorService modules = Executors.newFixedThreadPool(10);

    public static void shutdown() {
        auth.shutdown();
        cached.shutdown();
        schedueled.shutdown();
        modules.shutdown();
    }


}
