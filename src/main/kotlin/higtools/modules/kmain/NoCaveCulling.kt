package higtools.modules.kmain

import higtools.MeteorModule
import higtools.HIGTools

class NoCaveCulling:MeteorModule(HIGTools.MAIN, "NoCaveCulling", "Disables Minecraft's cave culling algorithm.") {

    override fun onActivate() {
        super.onActivate()
        mc.chunkCullingEnabled = false
        mc.worldRenderer.reload()
    }

    override fun onDeactivate() {
        super.onDeactivate()
        mc.chunkCullingEnabled = true
        mc.worldRenderer.reload()
    }

}
