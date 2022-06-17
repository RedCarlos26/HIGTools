package higtools.modules.main;

import higtools.modules.HIGTools;
import higtools.modules.borers.*;
import higtools.modules.kmain.AutoCenter;
import higtools.modules.kmain.AutoEat;
import higtools.modules.kmain.InvManager;
import higtools.modules.kmain.ScaffoldPlus;
import meteordevelopment.meteorclient.events.world.TickEvent;
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
import meteordevelopment.orbit.EventHandler;

public class Toggles extends Module {

    public enum Mode {
        HighwayBuilding,
        AxisDigging,
        NegNegDigging,
        NegPosDigging,
        PosNegDigging,
        PosPosDigging
    }

    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Mode> mode = sgGeneral.add(new EnumSetting.Builder<Mode>()
        .name("profile")
        .description("Which highway profile to use.")
        .defaultValue(Mode.HighwayBuilding)
        .build()
    );

    public Toggles() { super(HIGTools.MAIN, "toggles", "Automatically toggles the necessary modules for digging / building highways."); }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        Modules modules = Modules.get();

        if (mode.get() == Mode.HighwayBuilding) {
            modules.get(HighwayBuilderPlus.class).toggle();

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

            modules.get(Toggles.class).toggle();
        }


        if (mode.get() == Mode.AxisDigging) {
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

            modules.get(Toggles.class).toggle();
        }


        if (mode.get() == Mode.NegNegDigging) {
            modules.get(NegNegBorer.class).toggle();

            modules.get(AutoEat.class).toggle();
            modules.get(AutoCenter.class).toggle();
            modules.get(AutoLog.class).toggle();
            modules.get(FreeLook.class).toggle();
            modules.get(HandManager.class).toggle();
            modules.get(InvManager.class).toggle();
            modules.get(LiquidFiller.class).toggle();
            modules.get(Rotation.class).toggle();
            modules.get(SafeWalk.class).toggle();
            modules.get(ScaffoldPlus.class).toggle();
            modules.get(TPSSync.class).toggle();

            modules.get(Toggles.class).toggle();
        }


        if (mode.get() == Mode.NegPosDigging) {
            modules.get(NegPosBorer.class).toggle();

            modules.get(AutoEat.class).toggle();
            modules.get(AutoCenter.class).toggle();
            modules.get(AutoLog.class).toggle();
            modules.get(FreeLook.class).toggle();
            modules.get(HandManager.class).toggle();
            modules.get(InvManager.class).toggle();
            modules.get(LiquidFiller.class).toggle();
            modules.get(Rotation.class).toggle();
            modules.get(SafeWalk.class).toggle();
            modules.get(ScaffoldPlus.class).toggle();
            modules.get(TPSSync.class).toggle();

            modules.get(Toggles.class).toggle();
        }


        if (mode.get() == Mode.PosNegDigging) {
            modules.get(PosNegBorer.class).toggle();

            modules.get(AutoEat.class).toggle();
            modules.get(AutoCenter.class).toggle();
            modules.get(AutoLog.class).toggle();
            modules.get(FreeLook.class).toggle();
            modules.get(HandManager.class).toggle();
            modules.get(InvManager.class).toggle();
            modules.get(LiquidFiller.class).toggle();
            modules.get(Rotation.class).toggle();
            modules.get(SafeWalk.class).toggle();
            modules.get(ScaffoldPlus.class).toggle();
            modules.get(TPSSync.class).toggle();

            modules.get(Toggles.class).toggle();
        }


        if (mode.get() == Mode.PosPosDigging) {
            modules.get(PosPosBorer.class).toggle();

            modules.get(AutoEat.class).toggle();
            modules.get(AutoCenter.class).toggle();
            modules.get(AutoLog.class).toggle();
            modules.get(FreeLook.class).toggle();
            modules.get(HandManager.class).toggle();
            modules.get(InvManager.class).toggle();
            modules.get(LiquidFiller.class).toggle();
            modules.get(Rotation.class).toggle();
            modules.get(SafeWalk.class).toggle();
            modules.get(ScaffoldPlus.class).toggle();
            modules.get(TPSSync.class).toggle();

            modules.get(Toggles.class).toggle();
        }
    }
}
