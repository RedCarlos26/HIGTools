package highwaytools.mixins;

import highwaytools.HighwayTools;
import meteordevelopment.meteorclient.mixininterface.IMinecraftClient;
import net.minecraft.client.MinecraftClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(
    value = {MinecraftClient.class},
    priority = 1001
)
public abstract class MinecraftClientMixin2 implements IMinecraftClient {

    @Inject(method = "getWindowTitle", at = @At("HEAD"), cancellable = true)
    public void getWindowTitle(CallbackInfoReturnable<String> ci){
        String title = "HighwayTools-Meteor " + HighwayTools.VERSION;
        ci.setReturnValue(title);
    }
}
