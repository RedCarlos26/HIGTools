package me.redcarlos.higtools.modules.main;

import me.redcarlos.higtools.HIGTools;
import me.redcarlos.higtools.utils.ListMode;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.mixininterface.IVec3d;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.player.FindItemResult;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.item.BlockItem;
import net.minecraft.network.packet.c2s.play.PlayerInteractBlockC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

import java.util.List;

public class ScaffoldHIG extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<ListMode> listMode = sgGeneral.add(new EnumSetting.Builder<ListMode>()
        .name("block-list-mode")
        .description("Block list selection mode.")
        .defaultValue(ListMode.Whitelist)
        .build()
    );

    private final Setting<List<Block>> whitelist = sgGeneral.add(new BlockListSetting.Builder()
        .name("whitelist")
        .description("Blocks allowed to scaffold.")
        .defaultValue(Blocks.NETHERRACK)
        .visible(() -> listMode.get() == ListMode.Whitelist)
        .build()
    );

    private final Setting<List<Block>> blacklist = sgGeneral.add(new BlockListSetting.Builder()
        .name("blacklist")
        .description("Blocks denied to scaffold.")
        .defaultValue(Blocks.OBSIDIAN)
        .visible(() -> listMode.get() == ListMode.Blacklist)
        .build()
    );

    private final Setting<Integer> ext = sgGeneral.add(new IntSetting.Builder()
        .name("extend")
        .description("How far to place in front of you.")
        .defaultValue(1)
        .range(0, 5)
        .build()
    );

    private final Setting<Boolean> keepY = sgGeneral.add(new BoolSetting.Builder()
        .name("keep-y")
        .description("Places blocks only at a specific Y value.")
        .defaultValue(false)
        .build()
    );

    private final Setting<Integer> height = sgGeneral.add(new IntSetting.Builder()
        .name("height")
        .description("Y value to scaffold at.")
        .defaultValue(119)
        .range(-64, 320)
        .sliderRange(-64, 320)
        .visible(keepY::get)
        .build()
    );

    private boolean worked = false;

    public ScaffoldHIG() {
        super(HIGTools.MAIN, "scaffold-HIG", "Scaffolds blocks under you.");
    }

    @EventHandler
    public void onTick(TickEvent.Pre event) {
        if (mc.player == null || mc.world == null) return;

        float f = MathHelper.sin(mc.player.getYaw() * 0.017453292f);
        float g = MathHelper.cos(mc.player.getYaw() * 0.017453292f);

        for (int i = 0; i <= (mc.player.getVelocity().x == 0.0 && mc.player.getVelocity().z == 0.0 ? 0 : ext.get()); i++) {
            // Loop body
            Vec3d pos = mc.player.getPos().add(-f * i, -0.5, g * i);
            if (keepY.get()) ((IVec3d) pos).meteor$setY(height.get() - 1.0);

            BlockPos bPos = BlockPos.ofFloored(pos);

            if (!mc.world.getBlockState(bPos).isReplaceable()) {
                worked = false;
                continue;
            }
            worked = true;

            // Find slot with a block
            FindItemResult item;
            if (listMode.get() == ListMode.Whitelist) {
                item = InvUtils.findInHotbar(itemStack -> itemStack.getItem() instanceof BlockItem && whitelist.get().contains(Block.getBlockFromItem(itemStack.getItem())));
            } else {
                item = InvUtils.findInHotbar(itemStack -> itemStack.getItem() instanceof BlockItem && !blacklist.get().contains(Block.getBlockFromItem(itemStack.getItem())));
            }
            if (!item.found()) {
                return;
            } else {
                InvUtils.swap(item.slot(), true);
            }

            mc.player.networkHandler.sendPacket(new PlayerInteractBlockC2SPacket(Hand.MAIN_HAND, new BlockHitResult(pos, Direction.getFacing(pos).getOpposite(), bPos, true), 0));

            InvUtils.swapBack();
        }
    }

    public boolean hasWorked() {
        return worked;
    }
}
