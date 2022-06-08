package higtools.modules.world

import higtools.MeteorModule
import higtools.backward
import higtools.forward
import higtools.getValue
import higtools.highway
import higtools.modules.HIGTools
import higtools.left
import meteordevelopment.meteorclient.events.world.TickEvent
import meteordevelopment.meteorclient.settings.BoolSetting
import meteordevelopment.meteorclient.settings.EnumSetting
import meteordevelopment.meteorclient.settings.IntSetting
import meteordevelopment.orbit.EventHandler
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket
import net.minecraft.util.Util
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.util.math.MathHelper
import higtools.right
import higtools.setValue
import kotlin.math.floor

// Current nuker bypass implementation, works on ecme @ 13 bps
class NetherrackDiggingMontageMaker:MeteorModule(HIGTools.HIG, "NetherBorer", "Bores the nether. 'Tis a boring job.") {

    private val general = settings.defaultGroup

    private var mode by general.add(EnumSetting.Builder()
                                        .name("Mode")
                                        .description("Mode to use")
                                        .defaultValue(Mode.HIGHWAY)
                                        .build())
    private var extForward by general.add(IntSetting.Builder()
                                              .name("ExtForward")
                                              .description("How many blocks to extend higtools.forward")
                                              .defaultValue(1)
                                              .min(1)
                                              .max(6)
                                              .build())
    private var extBackward by general.add(IntSetting.Builder()
                                               .name("ExtBackward")
                                               .description("How many blocks to extend backwards.")
                                               .defaultValue(2)
                                               .min(1)
                                               .max(6)
                                               .build())
    private var xOffset by general.add(IntSetting.Builder()
                                           .name("XOffset")
                                           .description("How many blocks to offset the x axis.")
                                           .defaultValue(2)
                                           .min(-2)
                                           .max(2)
                                           .build())
    private var zOffset by general.add(IntSetting.Builder()
                                           .name("ZOffset")
                                           .description("How many blocks to offset the z axis.")
                                           .defaultValue(2)
                                           .min(-2)
                                           .max(2)
                                           .build())
    private var keepY by general.add(IntSetting.Builder()
                                         .name("KeepY")
                                         .description("How many blocks to keep on the y axis.")
                                         .defaultValue(-1)
                                         .min(-1)
                                         .max(255)
                                         .build())
    private var disable by general.add(BoolSetting.Builder()
                                           .name("Disable")
                                           .description("Disable the feature.")
                                           .defaultValue(false)
                                           .build())
    private var jumping by general.add(BoolSetting.Builder()
                                           .name("Jumping")
                                           .description("Send more or less packs.")
                                           .defaultValue(false)
                                           .build())

    // preserve 2 block tall tunnel for speed bypass
    private var blacklist:MutableList<BlockPos> = ArrayList()

    // previous floored block position of player
    private var prevBlockPos:BlockPos = BlockPos.ORIGIN

    // floored block position of player
    private var playerPos:BlockPos = BlockPos.ORIGIN

    // last time packets were sent
    private var lastUpdateTime:Long = 0

    private var packets:Int = 0

    override fun onActivate() {
        super.onActivate()
        // reset blacklist
        blacklist.clear()
    }

    override fun onDeactivate() {
        super.onDeactivate()
        // reset blacklist
        blacklist.clear()
    }

    @EventHandler
    fun tick(event:TickEvent.Pre) {
        this.prevBlockPos = this.playerPos
        this.playerPos = BlockPos(floor(mc.player!!.x).toInt(),
                                  if (keepY != -1) keepY else floor(mc.player!!.y).toInt(),
                                  floor(mc.player!!.z).toInt())
        if (this.playerPos != this.prevBlockPos || Util.getMeasuringTimeMs() - this.lastUpdateTime > 800) {
            this.getBlacklistedBlockPoses()
            when (this.mode) {
                Mode.THIN -> {
                    this.do2x3(playerPos.add(xOffset, 0, zOffset))
                    if (jumping) {
                        this.do2x3(playerPos.add(xOffset * -1, 0, zOffset * -1))
                        this.do2x3(playerPos.add(xOffset * -3, 0, zOffset * -3))
                        this.do2x3(playerPos.add(xOffset * -7, 0, zOffset * -7))
                    } else {
                        this.do2x3(playerPos.add(xOffset * -3, 0, zOffset * -3))
                    }
                }
                Mode.HIGHWAY -> {
                    this.doHighway4(playerPos.add(xOffset, 0, zOffset))
                    if (jumping) {
                        this.doHighway4(playerPos.add(xOffset * -1, 0, zOffset * -1))
                        this.doHighway4(playerPos.add(xOffset * -3, 0, zOffset * -3))
                        this.doHighway4(playerPos.add(xOffset * -7, 0, zOffset * -7))
                    } else {
                        this.doHighway4(playerPos.add(xOffset * -3, 0, zOffset * -3))
                    }
                }
            }
            this.lastUpdateTime = Util.getMeasuringTimeMs()
        }
        packets = 0
    }

    private fun doHighway4(plyerPos:BlockPos = playerPos) {
        for (i in -extBackward..extForward) {
            this.breakBlock(plyerPos.forward(i))
            this.breakBlock(plyerPos.forward(i).up())
            this.breakBlock(plyerPos.forward(i).up(2))
            this.breakBlock(plyerPos.forward(i).up(3))
            this.breakBlock(plyerPos.forward(i).right(1))
            this.breakBlock(plyerPos.forward(i).right(1).up())
            this.breakBlock(plyerPos.forward(i).right(1).up(2))
            this.breakBlock(plyerPos.forward(i).right(1).up(3))
            this.breakBlock(plyerPos.forward(i).right(2).up())
            this.breakBlock(plyerPos.forward(i).right(2).up(2))
            this.breakBlock(plyerPos.forward(i).right(2).up(3))
            this.breakBlock(plyerPos.forward(i).left(1))
            this.breakBlock(plyerPos.forward(i).left(1).up())
            this.breakBlock(plyerPos.forward(i).left(1).up(2))
            this.breakBlock(plyerPos.forward(i).left(1).up(3))
            this.breakBlock(plyerPos.forward(i).left(2))
            this.breakBlock(plyerPos.forward(i).left(2).up())
            this.breakBlock(plyerPos.forward(i).left(2).up(2))
            this.breakBlock(plyerPos.forward(i).left(2).up(3))
            this.breakBlock(plyerPos.forward(i).left(3).up())
            this.breakBlock(plyerPos.forward(i).left(3).up(2))
            this.breakBlock(plyerPos.forward(i).left(3).up(3))
        }
    }

    private fun do2x3(plyerPos:BlockPos = playerPos) {
        for (i in -extBackward..extForward) {
            this.breakBlock(plyerPos.forward(i))
            this.breakBlock(plyerPos.forward(i).up())
            this.breakBlock(plyerPos.forward(i).up(2))
            this.breakBlock(plyerPos.forward(i).left(1))
            this.breakBlock(plyerPos.forward(i).left(1).up())
            this.breakBlock(plyerPos.forward(i).left(1).up(2))
        }
    }

    private fun getBlacklistedBlockPoses() {
        this.blacklist.clear()
        if (highway in 1..4) {
            this.blacklist.add(this.playerPos.up(2))
            this.blacklist.add(this.playerPos.up(2).backward(1))
            this.blacklist.add(this.playerPos.up(2).backward(2))
            this.blacklist.add(this.playerPos.forward(1).up(2))
            this.blacklist.add(this.playerPos.forward(2).up(2))
            this.blacklist.add(this.playerPos.forward(3).up(2))
            this.blacklist.add(this.playerPos.forward(4).up(2))
            this.blacklist.add(this.playerPos.forward(5).up(2))
        } else {
            val f = MathHelper.sin(mc.player!!.yaw * 0.017453292f)
            val g = MathHelper.cos(mc.player!!.yaw * 0.017453292f)
            for (i in -2..5) {
                val pos = mc.player!!.pos.add(-f * i.toDouble(), 2.0, g * i.toDouble())
                this.blacklist.add(BlockPos(pos))
                this.blacklist.add(BlockPos(pos).left(1))
                this.blacklist.add(BlockPos(pos).left(2))
                this.blacklist.add(BlockPos(pos).right(1))
            }
        }
    }

    private fun breakBlock(blockPos:BlockPos) {
        if (packets >= 130) return
        if (mc.world!!.getBlockState(blockPos).material.isReplaceable || (this.blacklist.contains(blockPos) && disable)) return
        mc.networkHandler!!.sendPacket(PlayerActionC2SPacket(PlayerActionC2SPacket.Action.START_DESTROY_BLOCK,
                                                             blockPos,
                                                             Direction.UP))
        mc.networkHandler!!.sendPacket(PlayerActionC2SPacket(PlayerActionC2SPacket.Action.STOP_DESTROY_BLOCK,
                                                             blockPos,
                                                             Direction.UP))
        packets += 2
    }

    enum class Mode {
        THIN, HIGHWAY
    }

}
