package highwaytools.modules.kmain

import highwaytools.MeteorModule
import highwaytools.HighwayTools

class NoCaveCulling:MeteorModule(HighwayTools.MAIN, "NoCaveCulling", "Disables Minecraft's cave culling algorithm.") {

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
