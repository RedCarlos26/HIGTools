package higtools.modules.player

import higtools.*
import higtools.modules.HIGTools
import higtools.modules.world.ScaffoldPlus
import meteordevelopment.meteorclient.events.packets.PacketEvent
import meteordevelopment.meteorclient.events.world.TickEvent
import meteordevelopment.meteorclient.utils.player.InvUtils
import meteordevelopment.orbit.EventHandler
import net.minecraft.entity.effect.StatusEffects
import net.minecraft.item.Items
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket
import net.minecraft.network.packet.c2s.play.PlayerInteractBlockC2SPacket
import net.minecraft.util.Hand
import net.minecraft.util.math.Direction.DOWN

object AutoEat:MeteorModule(HIGTools.HIG, "AutoEat", "Automatically eat.") {

    private var hunger by mainGroup.add(IValue("Hunger", 16, "Hunger to eat at.", 1..19, 1))
    private val autoGap by mainGroup.add(BValue("AutoGap", true, "Gap when no fire res."))
    private val offhand by mainGroup.add(BValue("Offhand", true, "Eat if food is in offhand."))
    var eating = false
    private var slot = 0
    private var prevSlot = 0

    @EventHandler
    fun tick(event:TickEvent.Pre) {
        if (ScaffoldPlus.worked) {
            return
        }
        if (offhand && mc.player!!.offHandStack.item.isFood) {
            if (eating) {
                if (shouldEat()) {
                    doEat(true)
                    return
                } else {
                    stopEating()
                    return
                }
            } else {
                if (shouldEat()) {
                    startEating(true)
                    return
                }
            }
        }
        if (eating) {
            if (shouldEat()) {
                if (!mc.player!!.inventory.getStack(slot).isFood) {
                    val slot = findSlot()
                    if (slot == -1) {
                        stopEating()
                        return
                    } else {
                        changeSlot(slot)
                    }
                }
                changeSlot(slot)
                doEat(false)
            } else {
                changeSlot(prevSlot)
                stopEating()
            }
        } else {
            if (shouldEat()) {
                slot = findSlot()
                if (slot != -1) startEating(false)
            }
        }
    }

    @EventHandler
    fun sendPacket(event:PacketEvent.Receive) {
        if (event.packet is PlayerInteractBlockC2SPacket) {
            stopEating()
        }
    }

    private fun startEating(offhand:Boolean) {
        prevSlot = mc.player!!.inventory.selectedSlot
        if (!offhand) {
            changeSlot(slot)
        }
        doEat(offhand)
    }

    private fun stopEating() {
        mc.options.useKey.isPressed = false
        mc.player!!.stopUsingItem()
        mc.networkHandler!!.sendPacket(PlayerActionC2SPacket(PlayerActionC2SPacket.Action.RELEASE_USE_ITEM, mc.player!!.blockPos, DOWN))
        eating = false
    }

    private fun changeSlot(slot:Int) {
        mc.player!!.inventory.selectedSlot = slot
        this.slot = slot
    }

    private fun doEat(offhand:Boolean) {
        mc.options.useKey.isPressed = true
        if (!mc.player!!.isUsingItem) mc.interactionManager!!.interactItem(mc.player,
                                                                           mc.world,
                                                                           if (offhand) Hand.OFF_HAND else Hand.MAIN_HAND)
        eating = true
    }

    private fun shouldEat():Boolean {
        return mc.player!!.hungerManager.foodLevel <= hunger && !ScaffoldPlus.worked
    }

    private fun findSlot():Int {
        var slot = -1
        for (i in 0..8) {
            val item = mc.player!!.inventory.getStack(i).item
            if (!item.isFood) continue
            if (item != Items.ENCHANTED_GOLDEN_APPLE && mc.player!!.getStatusEffect(StatusEffects.FIRE_RESISTANCE) == null && InvUtils.findInHotbar(Items.ENCHANTED_GOLDEN_APPLE)
                    .found() && autoGap) continue
            slot = i
        }
        return slot
    }

}
