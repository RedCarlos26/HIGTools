package higtools.modules.kmain

import higtools.*
import higtools.HIGTools
import meteordevelopment.meteorclient.events.world.TickEvent
import meteordevelopment.meteorclient.mixininterface.IVec3d
import meteordevelopment.orbit.EventHandler
import net.minecraft.item.BlockItem
import net.minecraft.network.packet.c2s.play.PlayerInteractBlockC2SPacket
import net.minecraft.util.Hand
import net.minecraft.util.hit.BlockHitResult
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.util.math.MathHelper
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket

object ScaffoldPlus:MeteorModule(HIGTools.MAIN, "Scaffold+", "Scaffolds blocks under you.") {

    private var slot = -1

    private var ext by mainGroup.add(IValue("Extend", 1, "How much to place in front of you", 0..5, 1))
    private var tower by mainGroup.add(BValue("Tower", false, "Makes towering easier"))
    private var towerMult by mainGroup.add(DValue("Multi", 0.7454, "Makes tower potentially bypass stricter anti-cheats.", 0.0..2.0, 0.0001))
    private var keepY by mainGroup.add(IValue("KeepY", -1, "Keeps the Y value of the block", -1..255, 1))

    var worked:Boolean = false

    @EventHandler
    fun tick(event:TickEvent.Pre) {
        val f = MathHelper.sin(mc.player!!.yaw * 0.017453292f)
        val g = MathHelper.cos(mc.player!!.yaw * 0.017453292f)
        var runs = 0
        val prevSlot = mc.player!!.inventory.selectedSlot
        for (i in 0..if (mc.player!!.velocity.x == 0.0 && mc.player!!.velocity.z == 0.0) 0 else ext) {
            val pos = mc.player!!.pos.add(-f * i.toDouble(), -1.0, g * i.toDouble())
            if (keepY != -1) {
                (pos as IVec3d).setY(keepY.toDouble() - 1.0)
            }
            val bpos = BlockPos(pos)
            if (!mc.world!!.getBlockState(bpos).material.isReplaceable) {
                worked = false
                continue
            }
            worked = true
            val offHand = mc.player!!.offHandStack.item is BlockItem
            if (!offHand) {
                if (mc.player!!.mainHandStack.item !is BlockItem) {
                    findBlock@
                    for (j in 0..8) {
                        if (mc.player!!.inventory.getStack(j).item is BlockItem) {
                            slot = j
                            break@findBlock
                        }
                    }
                    if (slot == -1) return
                    mc.player!!.inventory.selectedSlot = slot
                    mc.networkHandler!!.sendPacket(UpdateSelectedSlotC2SPacket(slot))
                }
            }
            if (tower && mc.options.jumpKey.isPressed && mc.player!!.velocity.x == 0.0 && mc.player!!.velocity.z == 0.0) {
                if (mc.world!!.getBlockState(mc.player!!.blockPos.down()).material.isReplaceable && !mc.world!!.getBlockState(
                        mc.player!!.blockPos.down(2)).material.isReplaceable && mc.player!!.velocity.y > 0) {
                    mc.player!!.setVelocity(mc.player!!.velocity.x, -0.6, mc.player!!.velocity.z)
                    mc.player!!.jump()
                    mc.player!!.setVelocity(mc.player!!.velocity.x,
                                            mc.player!!.velocity.y * towerMult,
                                            mc.player!!.velocity.z)
                }
            }
            mc.networkHandler!!.sendPacket(
                PlayerInteractBlockC2SPacket(
                    if (offHand) Hand.OFF_HAND else Hand.MAIN_HAND,
                    BlockHitResult(pos, Direction.DOWN, bpos, false), 0))
            runs++
            slot = -1
        }
        if (mc.player!!.inventory.selectedSlot != prevSlot) {
            mc.player!!.inventory.selectedSlot = prevSlot
            mc.networkHandler!!.sendPacket(UpdateSelectedSlotC2SPacket(prevSlot))
        }
    }

}
