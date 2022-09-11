package higtools.mixins;

import higtools.modules.HIGTools;
import meteordevelopment.meteorclient.mixininterface.IMinecraftClient;
import net.minecraft.client.MinecraftClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import net.minecraft.client.util.Window;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.io.IOException;
import java.io.InputStream;

@Mixin(
    value = {MinecraftClient.class},
    priority = 1001
)
public abstract class MinecraftClientMixin2 implements IMinecraftClient {

    @Inject(method = "getWindowTitle", at = @At("HEAD"), cancellable = true)
    public void getWindowTitle(CallbackInfoReturnable<String> ci){
        String title = "HIG Tools " + HIGTools.VERSION;
        ci.setReturnValue(title);
    }
}
