package higtools.modules.player

import higtools.*
import higtools.modules.HIGTools
import meteordevelopment.meteorclient.events.world.TickEvent
import meteordevelopment.orbit.EventHandler
import kotlin.math.abs

class AutoCenter:MeteorModule(HIGTools.HIG, "AutoCenter", "Automatically centers the player so that NetherBorer mines on axis.") {

    // x+ x- z+ z- x+z+ x-z+ x+z- x-z-
    private var highway = -1
    private var comp by mainGroup.add(DValue("Compensation",
                                             1.0,
                                             "How much to compensate for the player's position",
                                             -2.5..2.5,
                                             0.1))

    override fun onActivate() {
        super.onActivate()
        if (mc.player == null) return
        this.highway = higtools.highway
    }

    @EventHandler
    fun motion(event:TickEvent.Pre) {
        check()
    }

    @EventHandler
    fun motion(event:TickEvent.Post) {
        check()
    }

    private fun check() {
        if (highway == -1) toggle()
        when (highway) {
            5 -> {
                val addZ = mc.player!!.z - mc.player!!.x
                mc.player!!.addVelocity(0.0, 0.0, (comp - addZ).coerceIn(-0.1..0.1))
            }
            6 -> {
                val addX = abs(mc.player!!.x) - mc.player!!.z
                mc.player!!.addVelocity((addX - comp).coerceIn(-0.1..0.1), 0.0, 0.0)
            }
            7 -> {
                val addX = mc.player!!.x - abs(mc.player!!.z)
                val wantedAddX = comp
                mc.player!!.addVelocity((wantedAddX - addX).coerceIn(-0.1..0.1), 0.0, 0.0)
            }
            8 -> {
                val addZ = abs(mc.player!!.z) - abs(mc.player!!.x)
                mc.player!!.addVelocity(0.0, 0.0, (addZ - comp).coerceIn(-0.1..0.1))
            }
        }
    }
}
