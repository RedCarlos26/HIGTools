package higtools.modules.main;

import higtools.modules.HIGTools;
import meteordevelopment.meteorclient.events.entity.player.FinishUsingItemEvent;
import meteordevelopment.meteorclient.events.entity.player.StoppedUsingItemEvent;
import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.ItemListSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.world.BlockUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.mob.SkeletonHorseEntity;
import net.minecraft.entity.mob.ZombieHorseEntity;
import net.minecraft.entity.passive.*;
import net.minecraft.entity.vehicle.BoatEntity;
import net.minecraft.entity.vehicle.MinecartEntity;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.item.PotionItem;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;

import java.util.List;

public class OneClickEat extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<List<Item>> foodList = sgGeneral.add(new ItemListSetting.Builder()
            .name("whitelist")
            .description("Which items you can one click eat.")
            .filter(Item::isFood)
            .build()
    );

    private final Setting<Boolean> usePotions = sgGeneral.add(new BoolSetting.Builder()
            .name("use-potions")
            .description("Allows you to also use potions.")
            .defaultValue(false)
            .build()
    );

    private final Setting<Boolean> onlyGround = sgGeneral.add(new BoolSetting.Builder()
            .name("only-on-ground")
            .description("Only allows you to one click eat on ground.")
            .defaultValue(false)
            .build()
    );

    public OneClickEat() {
        super(HIGTools.HIG, "one-click-eat", "Allows you to eat a consumable with one click");
    }

    private boolean isUsing;
    private boolean pressed;

    private boolean isBerry() {
        return mc.player.getMainHandStack().getItem() == Items.SWEET_BERRIES || mc.player.getOffHandStack().getItem() == Items.SWEET_BERRIES;
    }

    private boolean isPotato() {
        return mc.player.getMainHandStack().getItem() == Items.POTATO || mc.player.getOffHandStack().getItem() == Items.POTATO;
    }

    private boolean isCarrot() {
        return mc.player.getMainHandStack().getItem() == Items.CARROT || mc.player.getOffHandStack().getItem() == Items.CARROT;
    }

    private boolean canEatFull() {
        return mc.player.getMainHandStack().getItem() == Items.ENCHANTED_GOLDEN_APPLE || mc.player.getOffHandStack().getItem() == Items.ENCHANTED_GOLDEN_APPLE
                || mc.player.getMainHandStack().getItem() == Items.GOLDEN_APPLE || mc.player.getOffHandStack().getItem() == Items.GOLDEN_APPLE
                || mc.player.getMainHandStack().getItem() == Items.CHORUS_FRUIT || mc.player.getOffHandStack().getItem() == Items.CHORUS_FRUIT
                || mc.player.getMainHandStack().getItem() == Items.HONEY_BOTTLE || mc.player.getOffHandStack().getItem() == Items.HONEY_BOTTLE
                || mc.player.getMainHandStack().getItem() == Items.MILK_BUCKET || mc.player.getOffHandStack().getItem() == Items.MILK_BUCKET
                || mc.player.getMainHandStack().getItem() == Items.SUSPICIOUS_STEW || mc.player.getOffHandStack().getItem() == Items.SUSPICIOUS_STEW;

    }

    private boolean canPlantBerry(BlockPos pos){
        return mc.world.getBlockState(pos).isOf(Blocks.GRASS_BLOCK)
                || mc.world.getBlockState(pos).isOf(Blocks.DIRT)
                || mc.world.getBlockState(pos).isOf(Blocks.PODZOL)
                || mc.world.getBlockState(pos).isOf(Blocks.COARSE_DIRT)
                || mc.world.getBlockState(pos).isOf(Blocks.FARMLAND);
    }

    private boolean isFarmland(BlockPos pos) {
        return mc.world.getBlockState(pos).isOf(Blocks.FARMLAND);
    }

    private boolean isRideable(Entity hit) {
        return hit instanceof MinecartEntity
                || hit instanceof BoatEntity
                || hit instanceof HorseEntity
                || hit instanceof DonkeyEntity
                || hit instanceof MuleEntity
                || hit instanceof SkeletonHorseEntity
                || hit instanceof ZombieHorseEntity
                || hit instanceof PigEntity
                || hit instanceof StriderEntity;
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        assert mc.player != null;
        assert mc.world != null;

        if (onlyGround.get() && !mc.player.isOnGround()) return;

        if ((foodList.get().contains(mc.player.getMainHandStack().getItem()) || foodList.get().contains(mc.player.getOffHandStack().getItem()))
                || (usePotions.get() && (mc.player.getMainHandStack().getItem() instanceof PotionItem || mc.player.getOffHandStack().getItem() instanceof PotionItem))) {

            if(!mc.options.useKey.isPressed()) {
                pressed = false;
            }

            if (mc.options.useKey.isPressed() && !isUsing && !pressed) {
                pressed = true;

                if (mc.crosshairTarget.getType() == HitResult.Type.BLOCK) {
                    if (BlockUtils.isClickable(mc.world.getBlockState(((BlockHitResult) mc.crosshairTarget).getBlockPos()).getBlock())) return;
                    if (isBerry() && canPlantBerry(((BlockHitResult) mc.crosshairTarget).getBlockPos())) return;
                    if ((isPotato() || isCarrot()) && isFarmland(((BlockHitResult) mc.crosshairTarget).getBlockPos())) return;
                }

                if (!mc.player.getHungerManager().isNotFull() && !canEatFull()) return;

                mc.options.useKey.setPressed(true);
                isUsing = true;
            }

            if (isUsing) mc.options.useKey.setPressed(true);
        }
    }


    @Override
    public void onDeactivate() {
        stopIfUsing();
    }

    @EventHandler
    private void onPacketSend(PacketEvent.Send event) {
        if (event.packet instanceof UpdateSelectedSlotC2SPacket) {
            stopIfUsing();
        }
    }

    @EventHandler
    private void onFinishUsingItem(FinishUsingItemEvent event) {
        stopIfUsing();
    }

    @EventHandler
    private void onStoppedUsingItem(StoppedUsingItemEvent event) {
        stopIfUsing();
    }

    private void stopIfUsing() {
        if (isUsing) {
            mc.options.useKey.setPressed(false);
            isUsing = false;
        }
    }
}
