package higtools.modules.borers;

import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;

public class AxisBorer extends BorerModule {
    public AxisBorer() {
        super("AxisBorer", "Bore holes in the X and Z axis.", 4, 4, 0, 0);
    }

    @Override
    @EventHandler
    public void tick(TickEvent.Pre event) {
        // previous floored block position of player
        BlockPos prevBlockPos = playerPos;
        playerPos = new BlockPos(
            MathHelper.floor(mc.player.getX()),
            (int) (keepY.get() != -1 ? keepY.get() : Math.floor(mc.player.getY())),
            MathHelper.floor(mc.player.getZ()));

        if (playerPos != prevBlockPos || Util.getMeasuringTimeMs() - lastUpdateTime > 800) {
            switch (mode.get()) {
                case THIN -> {
                    do2x3(playerPos.add(xOffset.get(), 0, zOffset.get()));
                    do2x3(playerPos.add(xOffset.get() * -3, 0, zOffset.get() * -3));
                }
                case HIGHWAY -> {
                    doHighway4(playerPos.add(xOffset.get(), 0, zOffset.get()));
                    doHighway4(playerPos.add(xOffset.get() * -3, 0, zOffset.get() * -3));
                }
            }
            lastUpdateTime = Util.getMeasuringTimeMs();
        }
        packets = 0;
    }

    @Override
    protected void breakBlock(BlockPos blockPos) {
        if (packets >= 130 || mc.world.getBlockState(blockPos).getMaterial().isReplaceable()) return;

        mc.player.networkHandler.sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.START_DESTROY_BLOCK, blockPos, Direction.UP));
        mc.player.networkHandler.sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.STOP_DESTROY_BLOCK, blockPos, Direction.UP));
        packets += 2;
    }
}
