package higtools.modules.main;

import higtools.modules.HIGTools;
import higtools.utils.HIGUtils;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.world.Timer;
import meteordevelopment.orbit.EventHandler;

public class TPSSync extends Module {

    public TPSSync() {
        super(HIGTools.MAIN, "tps-sync", "Adds a general TPS Sync module (breaks timer, working on a fix).");
    }

    @EventHandler
    private void onTick(TickEvent.Post event) {
        if (!isActive()) return;

        Modules.get().get(Timer.class).setOverride(HIGUtils.getServerTPSMatch(true));
    }
}
