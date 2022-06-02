package higtools.utils.server;

import higtools.utils.TimerUtils;
import meteordevelopment.meteorclient.utils.world.TickRate;

import static meteordevelopment.meteorclient.MeteorClient.mc;

public class HTServerUtils {
    public static double getTPSMatch(boolean TPSSync) {
        return TPSSync ? (TickRate.INSTANCE.getTickRate() / 20) : 1;
    }

    public enum PingSync {
        Auto,
        Custom,
        None
    }

    public static int getPingMatch(boolean PingSync, boolean auto, int latency) {
        return PingSync ? (auto ? mc.getNetworkHandler().getPlayerListEntry(mc.player.getUuid()).getLatency() : latency) : 0;
    }

    private static TimerUtils pingTimer = new TimerUtils();

    public static boolean passedPingMatch(boolean PingSync, boolean auto, int latency) {
        return pingTimer.passedMillis(getPingMatch(PingSync, auto, latency));
    }


}
