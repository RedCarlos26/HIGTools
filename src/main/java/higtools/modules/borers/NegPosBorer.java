package higtools.modules.borers;

import higtools.HIGTools;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.stream.IntStream;

import static higtools.utils.HIGUtils.*;

public class NegPosBorer extends Module {
    // preserve 2 block tall tunnel for speed bypass
    private final ArrayList<BlockPos> blackList = new ArrayList<>();

    /**
     * last time packets were sent
     */
    private long lastUpdateTime = 0;
    /**
     * floored block position of player
     */
    private BlockPos playerPos = BlockPos.ORIGIN;
    private int packets = 0;
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Mode> mode = sgGeneral.add(new EnumSetting.Builder<Mode>()
        .name("Shape")
        .description("The shape to dig.")
        .defaultValue(Mode.HIGHWAY)
        .build()
    );

    private final Setting<Integer> extForward = sgGeneral.add(new IntSetting.Builder()
        .name("ExtForward")
        .description("How many blocks to dig forwards.")
        .defaultValue(1)
        .range(1, 6)
        .build()
    );

    private final Setting<Integer> extBackward = sgGeneral.add(new IntSetting.Builder()
        .name("ExtBackward")
        .description("How many blocks to dig backwards.")
        .defaultValue(2)
        .range(1, 6)
        .build()
    );

    private final Setting<Integer> xOffset = sgGeneral.add(new IntSetting.Builder()
        .name("XOffset")
        .description("How many blocks to dig on the x axis.")
        .defaultValue(-2)
        .range(-2, 2)
        .sliderRange(-2, 2)
        .build()
    );

    private final Setting<Integer> zOffset = sgGeneral.add(new IntSetting.Builder()
        .name("ZOffset")
        .description("How many blocks to dig on the z axis.")
        .defaultValue(2)
        .range(-2, 2)
        .sliderRange(-2, 2)
        .build()
    );

    private final Setting<Integer> keepY = sgGeneral.add(new IntSetting.Builder()
        .name("KeepY")
        .description("Keeps a specific Y level when digging.")
        .defaultValue(119)
        .range(-1, 255)
        .sliderRange(-1, 255)
        .build()
    );

    private final Setting<Boolean> disable = sgGeneral.add(new BoolSetting.Builder()
        .name("Disable")
        .description("Disable the jumping block feature")
        .defaultValue(false)
        .build()
    );

    private final Setting<Boolean> jumping = sgGeneral.add(new BoolSetting.Builder()
        .name("Jumping")
        .description("Send more or less packs")
        .defaultValue(false)
        .build()
    );

    public NegPosBorer() {
        super(HIGTools.BORERS, "NegPosBorer", "Automatically digs -X +Z highway.");
    }

    @Override
    public void onActivate() {
        super.onActivate();
        blackList.clear();
    }

    @Override
    public void onDeactivate() {
        super.onDeactivate();
        blackList.clear();
    }

    @EventHandler
    public void tick(TickEvent.Pre event) {
        // previous floored block position of player
        BlockPos prevBlockPos = playerPos;
        playerPos = new BlockPos(
            Math.floor(mc.player.getX()),
            (int) (keepY.get() != -1 ? keepY.get() : Math.floor(mc.player.getY())),
            Math.floor(mc.player.getZ()));

        if (this.playerPos != prevBlockPos || Util.getMeasuringTimeMs() - this.lastUpdateTime > 800) {
            getBlacklistedBlockPoses();
            switch (mode.get()) {
                case THIN -> {
                    do2x3(playerPos.add(xOffset.get(), 0, zOffset.get()));
                    if (jumping.get()) {
                        do2x3(playerPos.add(xOffset.get() * -1, 0, zOffset.get() * -1));
                        do2x3(playerPos.add(xOffset.get() * -3, 0, zOffset.get() * -3));
                        do2x3(playerPos.add(xOffset.get() * -7, 0, zOffset.get() * -7));
                    } else do2x3(playerPos.add(xOffset.get() * -3, 0, zOffset.get() * -3));
                }
                case HIGHWAY -> {
                    doHighway4(playerPos.add(xOffset.get(), 0, zOffset.get()));
                    if (jumping.get()) {
                        this.doHighway4(playerPos.add(xOffset.get() * -1, 0, zOffset.get() * -1));
                        this.doHighway4(playerPos.add(xOffset.get() * -3, 0, zOffset.get() * -3));
                        this.doHighway4(playerPos.add(xOffset.get() * -7, 0, zOffset.get() * -7));
                    } else doHighway4(playerPos.add(xOffset.get() * -3, 0, zOffset.get() * -3));
                }
            }
            lastUpdateTime = Util.getMeasuringTimeMs();
        }
        packets = 0;
    }

    private void getBlacklistedBlockPoses() {
        blackList.clear();
        if (getHighway() >= 1 && getHighway() <= 4) {
            blackList.add(playerPos.up(2));
            blackList.add(backward(playerPos.up(2), 1));
            blackList.add(backward(playerPos.up(2), 2));
            blackList.add(forward(playerPos, 1).up(2));
            blackList.add(forward(playerPos, 2).up(2));
            blackList.add(forward(playerPos, 3).up(2));
            blackList.add(forward(playerPos, 4).up(2));
            blackList.add(forward(playerPos, 5).up(2));
        } else {
            float f = MathHelper.sin(mc.player.getYaw() * 0.017453292f);
            float g = MathHelper.cos(mc.player.getYaw() * 0.017453292f);
            IntStream.rangeClosed(-2, 5).forEach(i -> {
                Vec3d pos = mc.player.getPos().add(-f * i, 20., g * i);
                blackList.add(new BlockPos(pos));
                blackList.add(left(new BlockPos(pos), 1));
                blackList.add(left(new BlockPos(pos), 2));
                blackList.add(right(new BlockPos(pos), 1));
            });
        }
    }

    private void do2x3(BlockPos playerPos) {
        IntStream.rangeClosed(-extBackward.get(), extForward.get()).forEach(i -> {
                breakBlock(forward(playerPos, i));
                breakBlock(forward(playerPos, i).up());
                breakBlock(forward(playerPos, i).up(2));
                breakBlock(left(forward(playerPos, i), 1));
                breakBlock(left(forward(playerPos, i), 1).up());
                breakBlock(left(forward(playerPos, i), 1).up(2));
            }
        );
    }

    private void doHighway4(BlockPos playerPos) {
        IntStream.rangeClosed(-extBackward.get(), extForward.get()).forEach(i -> {
            breakBlock(forward(playerPos, i));
            breakBlock(forward(playerPos, i).up());
            breakBlock(forward(playerPos, i).up(2));
            breakBlock(forward(playerPos, i).up(3));
            breakBlock(right(forward(playerPos, i), 1));
            breakBlock(right(forward(playerPos, i), 1).up());
            breakBlock(right(forward(playerPos, i), 1).up(2));
            breakBlock(right(forward(playerPos, i), 1).up(3));
            breakBlock(right(forward(playerPos, i), 2).up());
            breakBlock(right(forward(playerPos, i), 2).up(2));
            breakBlock(right(forward(playerPos, i), 2).up(3));
            breakBlock(left(forward(playerPos, i), 1));
            breakBlock(left(forward(playerPos, i), 1).up());
            breakBlock(left(forward(playerPos, i), 1).up(2));
            breakBlock(left(forward(playerPos, i), 1).up(3));
            breakBlock(left(forward(playerPos, i), 2));
            breakBlock(left(forward(playerPos, i), 2).up());
            breakBlock(left(forward(playerPos, i), 2).up(2));
            breakBlock(left(forward(playerPos, i), 2).up(3));
            breakBlock(left(forward(playerPos, i), 3).up());
            breakBlock(left(forward(playerPos, i), 3).up(2));
            breakBlock(left(forward(playerPos, i), 3).up(3));
        });
    }

    private void breakBlock(BlockPos blockPos) {
        if (packets >= 130 || mc.world.getBlockState(blockPos).getMaterial().isReplaceable() || (blackList.contains(blockPos) && disable.get())) return;

        mc.getNetworkHandler().sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.START_DESTROY_BLOCK, blockPos, Direction.UP));
        mc.getNetworkHandler().sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.STOP_DESTROY_BLOCK, blockPos, Direction.UP));
        packets += 2;
    }

    public enum Mode {
        THIN,
        HIGHWAY;
    }
}
