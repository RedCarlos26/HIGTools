package higtools.modules.main;

import higtools.modules.HIGTools;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.movement.AutoWalk;
import meteordevelopment.meteorclient.utils.player.FindItemResult;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.item.Items;

public class AutoDisable extends Module {

    public AutoDisable() { super(HIGTools.MAIN, "auto-disable", "Automatically disable AutoWalk when running out of pickaxes"); }

    @EventHandler
    private void onTick(TickEvent.Pre event) {

        Modules modules = Modules.get();
        FindItemResult pickaxe = InvUtils.find(itemStack -> itemStack.getItem() == Items.DIAMOND_PICKAXE || itemStack.getItem() == Items.NETHERITE_PICKAXE);

        if (!pickaxe.found()) {
            error("No pickaxe found... disabling auto walk.");
            modules.get(AutoWalk.class).toggle();
            modules.get(AutoDisable.class).toggle();
            // IK this is retarded but IDK how to do it with an actual method lmao
        }
    }
}
