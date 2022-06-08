package higtools.modules.main;

import higtools.modules.HIGTools;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.misc.AutoReconnect;
import meteordevelopment.meteorclient.utils.player.PlayerUtils;
import meteordevelopment.meteorclient.utils.world.Dimension;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.network.packet.s2c.play.DisconnectS2CPacket;
import net.minecraft.text.LiteralText;

public class AfkLogout extends Module {

    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Dimension> dimension = sgGeneral.add(new EnumSetting.Builder<Dimension>()
            .name("dimension")
            .description("Dimension for the coords.")
            .defaultValue(Dimension.Nether)
            .build()
    );

    private final Setting<Integer> xCoords = sgGeneral.add(new IntSetting.Builder()
            .name("x-coords")
            .description("The X coords it should log you out.")
            .defaultValue(1000)
            .range(-2147483648, 2147483647)
            .sliderRange(-2147483648, 2147483647)
            .build()
    );

    private final Setting<Integer> zCoords = sgGeneral.add(new IntSetting.Builder()
            .name("z-coords")
            .description("The z coords it should log you out.")
            .defaultValue(1000)
            .range(-2147483648, 2147483647)
            .sliderRange(-2147483648, 2147483647)
            .build()
    );

    private final Setting<Integer> radius = sgGeneral.add(new IntSetting.Builder()
            .name("radius")
            .description("The radius of coords from the exact coords it will log you out.")
            .defaultValue(64)
            .min(0)
            .sliderRange(0, 256)
            .build()
    );

    private final Setting<Boolean> toggleAutoReconnect = sgGeneral.add(new BoolSetting.Builder()
            .name("toggle-auto-reconnect")
            .description("Turns off auto reconnect when disconnecting.")
            .defaultValue(true)
            .build()
    );

    private final Setting<Boolean> autoToggle = sgGeneral.add(new BoolSetting.Builder()
            .name("auto-toggle")
            .description("Turns itself off when disconnecting.")
            .defaultValue(true)
            .build()
    );

    public AfkLogout() {
        super(HIGTools.HIG, "afk-logout", "Logs out when you are at a certain coords for afk travelling.");
    }

    @EventHandler
    private void onTick(TickEvent.Post event) {
        if (xCoordsMatch() && zCoordsMatch() && PlayerUtils.getDimension() == dimension.get()) {
            if (toggleAutoReconnect.get() && Modules.get().isActive(AutoReconnect.class)) Modules.get().get(AutoReconnect.class).toggle();
            if (autoToggle.get()) toggle();

            mc.player.networkHandler.onDisconnect(new DisconnectS2CPacket(new LiteralText("[AfkLog] Arrived at destination.")));
        }

    }

    private boolean xCoordsMatch() {
        return (mc.player.getX() <= xCoords.get() + radius.get() && mc.player.getX() >= xCoords.get() - radius.get());
    }

    private boolean zCoordsMatch() {
        return (mc.player.getZ() <= zCoords.get() + radius.get() && mc.player.getZ() >= zCoords.get() - radius.get());
    }
}
