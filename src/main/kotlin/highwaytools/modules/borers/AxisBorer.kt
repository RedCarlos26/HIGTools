package highwaytools.modules.borers

import highwaytools.*
import highwaytools.HighwayTools
import meteordevelopment.meteorclient.events.world.TickEvent
import meteordevelopment.orbit.EventHandler
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket
import net.minecraft.util.Util
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import kotlin.math.floor

class AxisBorer:MeteorModule(HighwayTools.BORERS, "AxisBorer", "Automatically digs axis highways.") {

    private val general = settings.defaultGroup

    private var mode by general.add(EValue("Shape", Mode.HIGHWAY, "The shape to dig."))
    private var extForward by general.add(IValue("ExtForwards", 4, "How many blocks to dig forwards.", 1..6, 1))
    private var extBackward by general.add(IValue("ExtBackwards", 4, "How many blocks to dig backwards.", 1..6, 1))
    private var xOffset by general.add(IValue("XOffset", 0, "How many blocks to offset on the x axis.", -2..2, 1))
    private var zOffset by general.add(IValue("ZOffset", 0, "How many blocks to offset on the z axis.", -2..2, 1))
    private var keepY by general.add(IValue("KeepY", 119, "Keeps a specific Y level when digging.", -1..255, 1))

    // previous floored block position of player
    private var prevBlockPos:BlockPos = BlockPos.ORIGIN

    // floored block position of player
    private var playerPos:BlockPos = BlockPos.ORIGIN

    // last time packets were sent
    private var lastUpdateTime:Long = 0

    private var packets:Int = 0

    @EventHandler
    fun tick(event:TickEvent.Pre) {
        this.prevBlockPos = this.playerPos
        this.playerPos = BlockPos(floor(mc.player!!.x).toInt(),
                                  if (keepY != -1) keepY else floor(mc.player!!.y).toInt(),
                                  floor(mc.player!!.z).toInt())
        if (this.playerPos != this.prevBlockPos || Util.getMeasuringTimeMs() - this.lastUpdateTime > 800) {
            when (this.mode) {
                Mode.THIN -> {
                    this.do2x3(playerPos.add(xOffset, 0, zOffset))
                    this.do2x3(playerPos.add(xOffset * -3, 0, zOffset * -3))
                }
                Mode.HIGHWAY -> {
                    this.doHighway4(playerPos.add(xOffset, 0, zOffset))
                    this.doHighway4(playerPos.add(xOffset * -3, 0, zOffset * -3))
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

    private fun breakBlock(blockPos:BlockPos) {
        if (packets >= 130) return
        if (mc.world!!.getBlockState(blockPos).material.isReplaceable) return
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
