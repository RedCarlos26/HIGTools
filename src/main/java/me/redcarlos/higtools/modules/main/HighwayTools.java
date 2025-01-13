package me.redcarlos.higtools.modules.main;

import me.redcarlos.higtools.HIGTools;
import me.redcarlos.higtools.modules.highwayborers.*;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.misc.AutoLog;
import meteordevelopment.meteorclient.systems.modules.movement.SafeWalk;
import meteordevelopment.meteorclient.systems.modules.player.InstantRebreak;
import meteordevelopment.meteorclient.systems.modules.render.FreeLook;
import meteordevelopment.meteorclient.utils.player.FindItemResult;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.item.Items;

import java.util.List;

public class HighwayTools extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Mode> mode = sgGeneral.add(new EnumSetting.Builder<Mode>()
        .name("profile")
        .description("Which highway profile to use.")
        .defaultValue(Mode.HighwayBuilding)
        .build()
    );

    /**
     * private final Setting<Boolean> toggleModules = sgGeneral.add(new BoolSetting.Builder()
     * .name("toggle-modules")
     * .description("Turn on these modules when HighwayTools is activated.")
     * .defaultValue(true)
     * .build()
     * );
     * <p>
     * private final Setting<Boolean> toggleBack = sgGeneral.add(new BoolSetting.Builder()
     * .name("toggle-back-off")
     * .description("Turn the other modules back on when HighwayTools is deactivated.")
     * .defaultValue(true)
     * .visible(toggleModules::get)
     * .build()
     * );
     * <p>
     * private final Setting<List<Module>> moduleList = sgGeneral.add(new ModuleListSetting.Builder()
     * .name("modules")
     * .description("Which modules to enable.")
     * .visible(toggleModules::get)
     * .build()
     * );
     */

    private final Setting<Boolean> axisToggle = sgGeneral.add(new BoolSetting.Builder()
        .name("axis-toggle")
        .description("Toggles itself when you reach an axis. Useful when digging ring roads.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Double> radius = sgGeneral.add(new DoubleSetting.Builder()
        .name("axis-radius")
        .description("The radius from the axis to toggle HighwayTools at.")
        .defaultValue(3)
        .min(0)
        .sliderRange(0, 15)
        .visible(axisToggle::get)
        .build()
    );

    private final Setting<Boolean> pickToggle = sgGeneral.add(new BoolSetting.Builder()
        .name("pickaxe-toggle")
        .description("Toggles itself when you run out of pickaxes.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Boolean> keepY = sgGeneral.add(new BoolSetting.Builder()
        .name("y-value-toggle")
        .description("Toggles itself if you fall below your original height.")
        .defaultValue(true)
        .build()
    );

    private final List<Class<? extends Module>> commonClasses = List.of(
        AutoLog.class,
        FreeLook.class,
        OffhandManager.class
    );

    private final List<Class<? extends Module>> diggingClasses = List.of(
        HotbarManager.class,
        LiquidFillerHIG.class,
        SafeWalk.class,
        ScaffoldHIG.class
    );

    private double originX;
    private double originY;
    private double originZ;
    private double originXZRatio;

    public HighwayTools() {
        super(HIGTools.MAIN, "highway-tools", "Digs, builds and repairs highways automatically.");
    }

    @Override
    public void onActivate() {
        if (mc.player == null || mc.world == null) return;

        // Get coords of the player from an axis (for axis toggle)
        originX = Math.abs(mc.player.getX());
        originY = Math.abs(mc.player.getY());
        originZ = Math.abs(mc.player.getZ());
        originXZRatio = Math.abs(originX - originZ); // Is the player on a diagonal axis from the start

        Modules modules = Modules.get();

        if (modules.get(InstantRebreak.class).isActive()) modules.get(InstantRebreak.class).toggle(); // Prevents issues when mining

        switch (mode.get()) {
            case HighwayBuilding -> {
                modules.get(HighwayBuilderHIG.class).toggle();
                commonClasses.forEach(moduleClass -> modules.get(moduleClass).toggle());
            }
            case AxisDigging, RingRoadDigging -> {
                modules.get(AxisBorer.class).toggle();
                commonClasses.forEach(moduleClass -> modules.get(moduleClass).toggle());
                diggingClasses.forEach(moduleClass -> modules.get(moduleClass).toggle());
            }
            case NegNegDigging -> {
                modules.get(NegNegBorer.class).toggle();
                modules.get(AutoCenter.class).toggle();
                commonClasses.forEach(moduleClass -> modules.get(moduleClass).toggle());
                diggingClasses.forEach(moduleClass -> modules.get(moduleClass).toggle());
            }
            case NegPosDigging -> {
                modules.get(NegPosBorer.class).toggle();
                modules.get(AutoCenter.class).toggle();
                commonClasses.forEach(moduleClass -> modules.get(moduleClass).toggle());
                diggingClasses.forEach(moduleClass -> modules.get(moduleClass).toggle());
            }
            case PosNegDigging -> {
                modules.get(PosNegBorer.class).toggle();
                modules.get(AutoCenter.class).toggle();
                commonClasses.forEach(moduleClass -> modules.get(moduleClass).toggle());
                diggingClasses.forEach(moduleClass -> modules.get(moduleClass).toggle());
            }
            case PosPosDigging -> {
                modules.get(PosPosBorer.class).toggle();
                modules.get(AutoCenter.class).toggle();
                commonClasses.forEach(moduleClass -> modules.get(moduleClass).toggle());
                diggingClasses.forEach(moduleClass -> modules.get(moduleClass).toggle());
            }
        }
    }

    @Override
    public void onDeactivate() {
        Modules modules = Modules.get();

        switch (mode.get()) {
            case HighwayBuilding -> {
                if (modules.get(HighwayBuilderHIG.class).isActive()) modules.get(HighwayBuilderHIG.class).toggle();
                commonClasses.stream().filter(moduleClass -> modules.get(moduleClass).isActive()).forEach(moduleClass -> modules.get(moduleClass).toggle());
            }
            case AxisDigging, RingRoadDigging -> {
                if (modules.get(AxisBorer.class).isActive()) modules.get(AxisBorer.class).toggle();
                commonClasses.stream().filter(moduleClass -> modules.get(moduleClass).isActive()).forEach(moduleClass -> modules.get(moduleClass).toggle());
                diggingClasses.stream().filter(moduleClass -> modules.get(moduleClass).isActive()).forEach(moduleClass -> modules.get(moduleClass).toggle());
            }
            case NegNegDigging -> {
                if (modules.get(NegNegBorer.class).isActive()) modules.get(NegNegBorer.class).toggle();
                if (modules.get(AutoCenter.class).isActive()) modules.get(AutoCenter.class).toggle();
                commonClasses.stream().filter(moduleClass -> modules.get(moduleClass).isActive()).forEach(moduleClass -> modules.get(moduleClass).toggle());
                diggingClasses.stream().filter(moduleClass -> modules.get(moduleClass).isActive()).forEach(moduleClass -> modules.get(moduleClass).toggle());
            }
            case NegPosDigging -> {
                if (modules.get(NegPosBorer.class).isActive()) modules.get(NegPosBorer.class).toggle();
                if (modules.get(AutoCenter.class).isActive()) modules.get(AutoCenter.class).toggle();
                commonClasses.stream().filter(moduleClass -> modules.get(moduleClass).isActive()).forEach(moduleClass -> modules.get(moduleClass).toggle());
                diggingClasses.stream().filter(moduleClass -> modules.get(moduleClass).isActive()).forEach(moduleClass -> modules.get(moduleClass).toggle());
            }
            case PosNegDigging -> {
                if (modules.get(PosNegBorer.class).isActive()) modules.get(PosNegBorer.class).toggle();
                if (modules.get(AutoCenter.class).isActive()) modules.get(AutoCenter.class).toggle();
                commonClasses.stream().filter(moduleClass -> modules.get(moduleClass).isActive()).forEach(moduleClass -> modules.get(moduleClass).toggle());
                diggingClasses.stream().filter(moduleClass -> modules.get(moduleClass).isActive()).forEach(moduleClass -> modules.get(moduleClass).toggle());
            }
            case PosPosDigging -> {
                if (modules.get(PosPosBorer.class).isActive()) modules.get(PosPosBorer.class).toggle();
                if (modules.get(AutoCenter.class).isActive()) modules.get(AutoCenter.class).toggle();
                commonClasses.stream().filter(moduleClass -> modules.get(moduleClass).isActive()).forEach(moduleClass -> modules.get(moduleClass).toggle());
                diggingClasses.stream().filter(moduleClass -> modules.get(moduleClass).isActive()).forEach(moduleClass -> modules.get(moduleClass).toggle());
            }
        }
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        if (mc.player == null || mc.world == null) return;

        // If player isn't on axis from the start, check if player reached axis
        if (axisToggle.get()) {
            if (originX >= radius.get() && originZ >= radius.get() && originXZRatio >= 4) {
                // Only run expensive checks if requirements above are met
                if (Math.abs(mc.player.getX()) <= radius.get() || Math.abs(mc.player.getZ()) <= radius.get()
                    || Math.abs(Math.abs(mc.player.getX()) - Math.abs(mc.player.getZ())) <= radius.get() + 4) {

                    info("Reached axis, disabling.");
                    toggle();
                }
            }
        }

        if (pickToggle.get()) {
            FindItemResult pickaxe = InvUtils.find(itemStack -> itemStack.getItem() == Items.DIAMOND_PICKAXE || itemStack.getItem() == Items.NETHERITE_PICKAXE);

            if (!pickaxe.found()) {
                error("No pickaxe found, disabling.");
                toggle();
            }
        }

        if (keepY.get()) {
            // -0.125 so players can still walk on soul sand and similar blocks while digging
            if (mc.player.getY() < originY - 0.125) {
                info("Fell below original height, disabling.");
                toggle();
            }
        }

        // TODO :
        // Liquid filler + nuker for mushrooms and stuff (soon)
        // Grim mode
    }

    public enum Mode {
        HighwayBuilding,
        AxisDigging,
        NegNegDigging,
        NegPosDigging,
        PosNegDigging,
        PosPosDigging,
        RingRoadDigging
    }
}
