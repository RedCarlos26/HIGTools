package higtools.mixins;

import net.minecraft.client.render.item.HeldItemRenderer;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(HeldItemRenderer.class)
public interface HeldItemRendererAccessor {
    @Accessor(value = "equipProgressMainHand")
    void setEquipProgressMainHand(float equipProgressMainHand);

    @Accessor(value = "equipProgressMainHand")
    float getEquipProgressMainHand();

    @Accessor(value = "prevEquipProgressMainHand")
    void setPrevEquipProgressMainHand(float prevEquipProgressMainHand);

    @Accessor(value = "prevEquipProgressMainHand")
    float getPrevEquipProgressMainHand();

    @Accessor(value = "equipProgressOffHand")
    void setEquipProgressOffHand(float equipProgressOffHand);

    @Accessor(value = "equipProgressOffHand")
    float getEquipProgressOffHand();

    @Accessor(value = "prevEquipProgressOffHand")
    void setPrevEquipProgressOffHand(float prevEquipProgressOffHand);

    @Accessor(value = "prevEquipProgressOffHand")
    float getPrevEquipProgressOffHand();

    @Accessor(value = "mainHand")
    void setMainHand(ItemStack itemStackMainHand);

    @Accessor(value = "mainHand")
    ItemStack getMainHand();

    @Accessor(value = "offHand")
    void setOffHand(ItemStack itemStackOffHand);

    @Accessor(value = "offHand")
    ItemStack getOffHand();
}
