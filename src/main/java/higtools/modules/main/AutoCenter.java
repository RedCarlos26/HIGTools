package higtools.modules.main;

import higtools.HIGTools;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.DoubleSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;

import static higtools.utils.HIGUtils.getHighway;

public class AutoCenter extends Module {
    // x+ x- z+ z- x+z+ x-z+ x+z- x-z-
    private int highway = -1;
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Double> comp = sgGeneral.add(new DoubleSetting.Builder()
        .name("offset")
        .defaultValue(1.0)
        .description("How much to offset the player's position.")
        .range(-2.5, 2.5)
        .sliderRange(-2.5, 2.5)
        .build()
    );

    private final Setting<Double> maxSpeed = sgGeneral.add(new DoubleSetting.Builder()
        .name("max-speed")
        .defaultValue(0.1)
        .description("How fast to try to get you on axis.")
        .range(0.01, 0.5)
        .build()
    );

    public AutoCenter() {
        super(HIGTools.MAIN, "auto-center", "Automatically centers the player so that NetherBorer mines on axis.");
    }

    @Override
    public void onActivate() {
        super.onActivate();
        if (mc.player == null) return;
        highway = getHighway();
    }

    @EventHandler
    public void motions(TickEvent.Pre event) {
        check();
    }

    @EventHandler
    public void motions(TickEvent.Post event) {
        check();
    }

    private void check() {
        if (highway == -1) toggle();
        if (highway == 5) {
            double addZ = mc.player.getZ() - mc.player.getX();
            mc.player.addVelocity(0.0, 0.0, Math.max(-maxSpeed.get(), Math.min(maxSpeed.get(), comp.get() - addZ)));
        } else if (highway == 6) {
            double addX = Math.abs(mc.player.getX()) - mc.player.getZ();
            mc.player.addVelocity(Math.max(-maxSpeed.get(), Math.min(maxSpeed.get(), addX - comp.get())), 0.0, 0.0);
        } else if (highway == 7) {
            double addX = mc.player.getX() - Math.abs(mc.player.getZ());
            mc.player.addVelocity(Math.max(-maxSpeed.get(), Math.min(maxSpeed.get(), comp.get() - addX)), 0.0, 0.0);
        } else if (highway == 8) {
            double addZ = Math.abs(mc.player.getZ()) - Math.abs(mc.player.getX());
            mc.player.addVelocity(0.0, 0.0, Math.max(-maxSpeed.get(), Math.min(maxSpeed.get(), addZ - comp.get())));
        }
    }
}
