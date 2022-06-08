package higtools.modules.world

import higtools.MeteorModule
import higtools.modules.HIGTools

class NoCaveCulling:MeteorModule(HIGTools.HIG, "NoCaveCulling", "No culling of caves.") {

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
