package higtools.modules.main;

import higtools.HIGTools;
import higtools.modules.borers.*;
import meteordevelopment.meteorclient.settings.EnumSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.misc.AutoLog;
import meteordevelopment.meteorclient.systems.modules.movement.SafeWalk;
import meteordevelopment.meteorclient.systems.modules.player.Rotation;
import meteordevelopment.meteorclient.systems.modules.render.FreeLook;
import meteordevelopment.meteorclient.systems.modules.world.LiquidFiller;

import java.util.List;

public class HighwayTools extends Module {
    public enum Mode {
        HighwayBuilding,
        AxisDigging,
        NegNegDigging,
        NegPosDigging,
        PosNegDigging,
        PosPosDigging,
        RingRoadDigging
    }

    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Mode> mode = sgGeneral.add(new EnumSetting.Builder<Mode>()
        .name("Profile")
        .description("Which highway profile to use.")
        .defaultValue(Mode.HighwayBuilding)
        .build()
    );

    private final List<Class<? extends Module>> commonClasses = List.of(
        AutoEatPlus.class,
        AutoLog.class,
        FreeLook.class,
        HandManager.class,
        InvManager.class,
        LiquidFiller.class,
        Rotation.class,
        SafeWalk.class,
        ScaffoldPlus.class
    );

    public HighwayTools() {
        super(HIGTools.MAIN, "highway-tools", "Digs, builds and repairs highways automatically.");
    }

    @Override
    public void onActivate() {
        Modules modules = Modules.get();

        switch (mode.get()) {
            case HighwayBuilding -> {
                modules.get(HighwayBuilderPlus.class).toggle();
                commonClasses.forEach(moduleClass -> modules.get(moduleClass).toggle());
            }
            case AxisDigging -> {
                modules.get(AxisBorer.class).toggle();
                commonClasses.forEach(moduleClass -> modules.get(moduleClass).toggle());
            }
            case NegNegDigging -> {
                modules.get(NegNegBorer.class).toggle();
                modules.get(AutoCenter.class).toggle();
                commonClasses.forEach(moduleClass -> modules.get(moduleClass).toggle());
            }
            case NegPosDigging -> {
                modules.get(NegPosBorer.class).toggle();
                modules.get(AutoCenter.class).toggle();
                commonClasses.forEach(moduleClass -> modules.get(moduleClass).toggle());
            }
            case PosNegDigging -> {
                modules.get(PosNegBorer.class).toggle();
                modules.get(AutoCenter.class).toggle();
                commonClasses.forEach(moduleClass -> modules.get(moduleClass).toggle());
            }
            case PosPosDigging -> {
                modules.get(PosPosBorer.class).toggle();
                modules.get(AutoCenter.class).toggle();
                commonClasses.forEach(moduleClass -> modules.get(moduleClass).toggle());
            }
            case RingRoadDigging -> {
                modules.get(RingRoadBorer.class).toggle();
                commonClasses.forEach(moduleClass -> modules.get(moduleClass).toggle());
            }
        }
    }

    @Override
    public void onDeactivate() {
        Modules modules = Modules.get();

        switch (mode.get()) {
            case HighwayBuilding -> {
                if (modules.get(HighwayBuilderPlus.class).isActive()) modules.get(HighwayBuilderPlus.class).toggle();
                commonClasses.stream()
                    .filter(moduleClass -> modules.get(moduleClass).isActive())
                    .forEach(moduleClass -> modules.get(moduleClass).toggle());
            }
            case AxisDigging -> {
                if (modules.get(AxisBorer.class).isActive()) modules.get(AxisBorer.class).toggle();
                commonClasses.stream()
                    .filter(moduleClass -> modules.get(moduleClass).isActive())
                    .forEach(moduleClass -> modules.get(moduleClass).toggle());
            }
            case NegNegDigging -> {
                if (modules.get(NegNegBorer.class).isActive()) modules.get(NegNegBorer.class).toggle();
                if (modules.get(AutoCenter.class).isActive()) modules.get(AutoCenter.class).toggle();
                commonClasses.stream()
                    .filter(moduleClass -> modules.get(moduleClass).isActive())
                    .forEach(moduleClass -> modules.get(moduleClass).toggle());
            }
            case NegPosDigging -> {
                if (modules.get(NegPosBorer.class).isActive()) modules.get(NegPosBorer.class).toggle();
                if (modules.get(AutoCenter.class).isActive()) modules.get(AutoCenter.class).toggle();
                commonClasses.stream()
                    .filter(moduleClass -> modules.get(moduleClass).isActive())
                    .forEach(moduleClass -> modules.get(moduleClass).toggle());
            }
            case PosNegDigging -> {
                if (modules.get(PosNegBorer.class).isActive()) modules.get(PosNegBorer.class).toggle();
                if (modules.get(AutoCenter.class).isActive()) modules.get(AutoCenter.class).toggle();
                commonClasses.stream()
                    .filter(moduleClass -> modules.get(moduleClass).isActive())
                    .forEach(moduleClass -> modules.get(moduleClass).toggle());
            }
            case PosPosDigging -> {
                if (modules.get(PosPosBorer.class).isActive()) modules.get(PosPosBorer.class).toggle();
                if (modules.get(AutoCenter.class).isActive()) modules.get(AutoCenter.class).toggle();
                commonClasses.stream()
                    .filter(moduleClass -> modules.get(moduleClass).isActive())
                    .forEach(moduleClass -> modules.get(moduleClass).toggle());
            }
            case RingRoadDigging -> {
                if (modules.get(RingRoadBorer.class).isActive()) modules.get(RingRoadBorer.class).toggle();
                commonClasses.stream()
                    .filter(moduleClass -> modules.get(moduleClass).isActive())
                    .forEach(moduleClass -> modules.get(moduleClass).toggle());
            }
        }
    }
}
