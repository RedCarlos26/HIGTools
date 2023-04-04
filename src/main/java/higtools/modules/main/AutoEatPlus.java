package higtools.modules.main;

import higtools.HIGTools;
import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractBlockC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Direction;

import java.util.stream.IntStream;

public class AutoEatPlus extends Module {
    private boolean eating = false;
    private int slot = 0;
    private int prevSlot = 0;
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Integer> hunger = sgGeneral.add(new IntSetting.Builder()
        .name("hunger")
        .description("Hunger to eat at.")
        .defaultValue(16)
        .range(1, 19)
        .sliderRange(1, 19)
        .build()
    );

    private final Setting<Boolean> autoGap = sgGeneral.add(new BoolSetting.Builder()
        .name("auto-gap")
        .description("Gap when no fire resistance effect.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Boolean> offhand = sgGeneral.add(new BoolSetting.Builder()
        .name("offhand")
        .description("Eat if food is in offhand.")
        .defaultValue(true)
        .build()
    );

    public AutoEatPlus() {
        super(HIGTools.MAIN, "auto-eat+", "Automatically eats the chosen food.");
    }

    @EventHandler
    public void tick(TickEvent.Pre event) {
        if (Modules.get().get(ScaffoldPlus.class).isWorked()) return;

        if (offhand.get() && mc.player.getOffHandStack().getItem().isFood()) {
            if (eating) {
                if (shouldEat()) {
                    doEat(true);
                } else {
                    stopEating();
                }
                return;
            } else if (shouldEat()) {
                startEating(true);
                return;
            }
        }

        if (eating) {
            if (shouldEat()) {
                if (!mc.player.getInventory().getStack(slot).isFood()) {
                    int slot = findSlot();
                    if (slot == -1) {
                        stopEating();
                        return;
                    } else changeSlot(slot);
                }
                changeSlot(slot);
                doEat(false);
            } else {
                changeSlot(prevSlot);
                stopEating();
            }
        } else if (shouldEat()) {
            slot = findSlot();
            if (slot != -1) startEating(false);
        }
    }

    @EventHandler
    public void sendPacket(PacketEvent.Receive event) {
        if (event.packet instanceof PlayerInteractBlockC2SPacket) stopEating();
    }

    private void startEating(boolean offhand) {
        prevSlot = mc.player.getInventory().selectedSlot;
        if (!offhand) changeSlot(slot);
        doEat(offhand);
    }

    private void stopEating() {
        mc.options.useKey.setPressed(false);
        mc.player.stopUsingItem();
        mc.player.networkHandler.sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.RELEASE_USE_ITEM, mc.player.getBlockPos(), Direction.DOWN));
        eating = false;
    }

    private void changeSlot(int slot) {
        mc.player.getInventory().selectedSlot = slot;
        this.slot = slot;
    }

    private void doEat(boolean offhand) {
        mc.options.useKey.setPressed(true);
        if (mc.player.isUsingItem()) return;
        mc.interactionManager.interactItem(mc.player, offhand ? Hand.OFF_HAND : Hand.MAIN_HAND);
        eating = true;
    }

    private boolean shouldEat() {
        return mc.player.getHungerManager().getFoodLevel() <= hunger.get() && !Modules.get().get(ScaffoldPlus.class).isWorked();
    }

    private int findSlot() {
        return IntStream.rangeClosed(0, 8).filter(i -> {
                Item item = mc.player.getInventory().getStack(i).getItem();
                return item.isFood() && (item != Items.ENCHANTED_GOLDEN_APPLE
                    || mc.player.getStatusEffect(StatusEffects.FIRE_RESISTANCE) != null
                    || !autoGap.get()
                    || !InvUtils.findInHotbar(Items.ENCHANTED_GOLDEN_APPLE).found()
                );
            }
        ).findFirst().orElse(-1);
    }

    public boolean isEating() {
        return eating;
    }
}
