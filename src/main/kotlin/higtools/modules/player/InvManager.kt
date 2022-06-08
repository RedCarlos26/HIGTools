package higtools.modules.player

import higtools.*
import higtools.modules.HIGTools
import meteordevelopment.meteorclient.events.world.TickEvent
import meteordevelopment.orbit.EventHandler
import net.minecraft.screen.slot.SlotActionType
import net.minecraft.text.Text
import net.minecraft.util.Formatting
import net.minecraft.util.Identifier
import net.minecraft.util.registry.Registry

class InvManager:MeteorModule(HIGTools.HIG, "InvManager", "Automatically organize your inventory") {

    private val slotsGroup = settings.createGroup("Slots", false)

    private var slot0 by slotsGroup.add(SValue("Slot0",
                                               "",
                                               "",
                                               { false },
                                               { cur -> itemIds[0] = Identifier(cur).also { println("<Llama/InvManager> Loaded $it into slot0") } }))
    private var slot1 by slotsGroup.add(SValue("Slot1",
                                               "",
                                               "",
                                               { false },
                                               { cur -> itemIds[1] = Identifier(cur).also { println("<Llama/InvManager> Loaded $it into slot1") } }))
    private var slot2 by slotsGroup.add(SValue("Slot2",
                                               "",
                                               "",
                                               { false },
                                               { cur -> itemIds[2] = Identifier(cur).also { println("<Llama/InvManager> Loaded $it into slot2") } }))
    private var slot3 by slotsGroup.add(SValue("Slot3",
                                               "",
                                               "",
                                               { false },
                                               { cur -> itemIds[3] = Identifier(cur).also { println("<Llama/InvManager> Loaded $it into slot3") } }))
    private var slot4 by slotsGroup.add(SValue("Slot4",
                                               "",
                                               "",
                                               { false },
                                               { cur -> itemIds[4] = Identifier(cur).also { println("<Llama/InvManager> Loaded $it into slot4") } }))
    private var slot5 by slotsGroup.add(SValue("Slot5",
                                               "",
                                               "",
                                               { false },
                                               { cur -> itemIds[5] = Identifier(cur).also { println("<Llama/InvManager> Loaded $it into slot5") } }))
    private var slot6 by slotsGroup.add(SValue("Slot6",
                                               "",
                                               "",
                                               { false },
                                               { cur -> itemIds[6] = Identifier(cur).also { println("<Llama/InvManager> Loaded $it into slot6") } }))
    private var slot7 by slotsGroup.add(SValue("Slot7",
                                               "",
                                               "",
                                               { false },
                                               { cur -> itemIds[7] = Identifier(cur).also { println("<Llama/InvManager> Loaded $it into slot7") } }))
    private var slot8 by slotsGroup.add(SValue("Slot8",
                                               "",
                                               "",
                                               { false },
                                               { cur -> itemIds[8] = Identifier(cur).also { println("<Llama/InvManager> Loaded $it into slot8") } }))

    private var delay by mainGroup.add(IValue("Delay", 15, "Delay to use", 1..35, 1))
    private var reset by mainGroup.add(BValue("Reset", false, "Toggle this to reset, too lazy to make proper thing", { true }, { reset() }))

    private var itemIds:Array<Identifier> = Array(9) { Identifier("") }

    override fun onActivate() {
        super.onActivate()
        println("<HIGTools/InvManager> ${itemIds.contentToString()}")
    }

    @EventHandler
    fun tick(event:TickEvent.Pre) {
        if (mc.player!!.age % delay == 0) {
            for (i in 0..8) {
                if (itemIds[i].toString().replace("minecraft:", "") == "") continue
                if (Registry.ITEM.getId(mc.player!!.inventory.getStack(i).item) != itemIds[i]) {
                    for (j in 9..35) {
                        if (Registry.ITEM.getId(mc.player!!.inventory.getStack(j).item) == itemIds[i]) {
                            mc.interactionManager!!.clickSlot(mc.player!!.currentScreenHandler.syncId,
                                                              csToPs(j),
                                                              0,
                                                              SlotActionType.PICKUP,
                                                              mc.player)
                            mc.interactionManager!!.clickSlot(mc.player!!.currentScreenHandler.syncId,
                                                              csToPs(i),
                                                              0,
                                                              SlotActionType.PICKUP,
                                                              mc.player)
                            mc.interactionManager!!.clickSlot(mc.player!!.currentScreenHandler.syncId,
                                                              csToPs(j),
                                                              0,
                                                              SlotActionType.PICKUP,
                                                              mc.player)
                            mc.interactionManager!!.tick()
                            break
                        }
                    }
                }
            }
        }
    }

    private fun reset() {
        slot0 = ""
        slot1 = ""
        slot2 = ""
        slot3 = ""
        slot4 = ""
        slot5 = ""
        slot6 = ""
        slot7 = ""
        slot8 = ""
    }

    private fun save() {
        slot0 = Registry.ITEM.getId(mc.player!!.inventory.getStack(0).item).toString()
        slot1 = Registry.ITEM.getId(mc.player!!.inventory.getStack(1).item).toString()
        slot2 = Registry.ITEM.getId(mc.player!!.inventory.getStack(2).item).toString()
        slot3 = Registry.ITEM.getId(mc.player!!.inventory.getStack(3).item).toString()
        slot4 = Registry.ITEM.getId(mc.player!!.inventory.getStack(4).item).toString()
        slot5 = Registry.ITEM.getId(mc.player!!.inventory.getStack(5).item).toString()
        slot6 = Registry.ITEM.getId(mc.player!!.inventory.getStack(6).item).toString()
        slot7 = Registry.ITEM.getId(mc.player!!.inventory.getStack(7).item).toString()
        slot8 = Registry.ITEM.getId(mc.player!!.inventory.getStack(8).item).toString()
        mc.inGameHud.chatHud.addMessage(Text.of("<${Formatting.LIGHT_PURPLE}Llama/InvManager${Formatting.RESET}> Saved inventory -> ${
            itemIds.contentToString().replace("minecraft:", "")
        }"))
    }

}
