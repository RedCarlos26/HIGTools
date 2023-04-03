package higtools.modules.borers;

import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;

import java.util.stream.IntStream;

import static higtools.utils.HIGUtils.*;

public class RingRoadBorer extends BorerModule {
    public RingRoadBorer() {
        super("RingRoadBorer", "Automatically digs ring road highways.", 4, 4, 0, 0);
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

    @Override
    @EventHandler
    public void tick(TickEvent.Pre event) {
        // previous floored block position of player
        BlockPos prevBlockPos = playerPos;
        playerPos = new BlockPos(
            MathHelper.floor(mc.player.getX()),
            keepY.get() != -1 ? keepY.get() : MathHelper.floor(mc.player.getY()),
            MathHelper.floor(mc.player.getZ()));

        if (playerPos != prevBlockPos || Util.getMeasuringTimeMs() - this.lastUpdateTime > 800) {
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
                    doHighway3(playerPos.add(xOffset.get(), 0, zOffset.get()));
                    if (jumping.get()) {
                        doHighway3(playerPos.add(xOffset.get() * -1, 0, zOffset.get() * -1));
                        doHighway3(playerPos.add(xOffset.get() * -3, 0, zOffset.get() * -3));
                        doHighway3(playerPos.add(xOffset.get() * -7, 0, zOffset.get() * -7));
                    } else doHighway3(playerPos.add(xOffset.get() * -3, 0, zOffset.get() * -3));
                }
            }
            lastUpdateTime = Util.getMeasuringTimeMs();
        }
        packets = 0;
    }

    private void doHighway3(BlockPos playerPos) {
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

            breakBlock(left(forward(playerPos, i), 2).up());
            breakBlock(left(forward(playerPos, i), 2).up(2));
            breakBlock(left(forward(playerPos, i), 2).up(3));
        });
    }
}
