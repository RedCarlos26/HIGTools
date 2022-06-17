package higtools.modules.toggles;

import higtools.modules.HIGTools;
import higtools.modules.borers.AxisBorer;
import higtools.modules.kmain.AutoEat;
import higtools.modules.kmain.InvManager;
import higtools.modules.kmain.ScaffoldPlus;
import higtools.modules.main.HandManager;
import higtools.modules.main.TPSSync;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.misc.AutoLog;
import meteordevelopment.meteorclient.systems.modules.movement.SafeWalk;
import meteordevelopment.meteorclient.systems.modules.player.Rotation;
import meteordevelopment.meteorclient.systems.modules.render.FreeLook;
import meteordevelopment.meteorclient.systems.modules.world.LiquidFiller;
import meteordevelopment.orbit.EventHandler;

public class AxisDigging extends Module {

    public AxisDigging() { super(HIGTools.TOGGLES, "axis-digging", "Turns on the necessary modules to dig axis highways."); }

    @EventHandler
    private void onTick(TickEvent.Pre event) {

        Modules modules = Modules.get();

        modules.get(AxisBorer.class).toggle();

        modules.get(AutoEat.class).toggle();
        modules.get(AutoLog.class).toggle();
        modules.get(FreeLook.class).toggle();
        modules.get(HandManager.class).toggle();
        modules.get(InvManager.class).toggle();
        modules.get(LiquidFiller.class).toggle();
        modules.get(Rotation.class).toggle();
        modules.get(SafeWalk.class).toggle();
        modules.get(ScaffoldPlus.class).toggle();
        modules.get(TPSSync.class).toggle();

    }
}
