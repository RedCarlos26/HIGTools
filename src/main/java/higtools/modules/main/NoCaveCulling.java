package higtools.modules.main;

import higtools.HIGTools;
import meteordevelopment.meteorclient.systems.modules.Module;

public class NoCaveCulling extends Module {
    public NoCaveCulling() {
        super(HIGTools.MAIN, "NoCaveCulling", "Disables Minecraft's cave culling algorithm.");
    }

    @Override
    public void onActivate() {
        super.onActivate();
        mc.chunkCullingEnabled = false;
        mc.worldRenderer.reload();
    }

    @Override
    public void onDeactivate() {
        super.onDeactivate();
        mc.chunkCullingEnabled = true;
        mc.worldRenderer.reload();
    }
}
