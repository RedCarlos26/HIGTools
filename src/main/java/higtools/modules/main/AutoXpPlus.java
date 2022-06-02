package higtools.modules.main;

import higtools.modules.HIGTools;
import higtools.utils.HTEntityUtils;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.misc.Keybind;
import meteordevelopment.meteorclient.utils.player.FindItemResult;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.meteorclient.utils.player.PlayerUtils;
import meteordevelopment.meteorclient.utils.player.Rotations;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;

public class AutoXpPlus extends Module {

    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final SettingGroup sgSafety = settings.createGroup("Safety");


    // General
    private final Setting<Boolean> replenish = sgGeneral.add(new BoolSetting.Builder()
            .name("replenish")
            .description("Automatically move XP to a selected hotbar slot.")
            .defaultValue(true)
            .build()
    );

    private final Setting<Integer> slot = sgGeneral.add(new IntSetting.Builder()
            .name("exp-slot")
            .description("The slot to replenish XP into.")
            .visible(replenish::get)
            .defaultValue(5)
            .range(1,9)
            .sliderRange(1,9)
            .build()
    );

    private final Setting<Integer> maxThreshold = sgGeneral.add(new IntSetting.Builder()
            .name("threshold")
            .description("The maximum durability to repair items to.")
            .defaultValue(80)
            .range(1,100)
            .sliderRange(1,100)
            .build()
    );

    private final Setting<Boolean> lookDown = sgGeneral.add(new BoolSetting.Builder()
            .name("rotate")
            .description("Forces you to rotate downwards when throwing XP.")
            .defaultValue(true)
            .build()
    );

    private final Setting<Boolean> onlyOnKey = sgGeneral.add(new BoolSetting.Builder()
            .name("only-on-key")
            .description("Only allows Auto XP to throw xp when a key is held.")
            .defaultValue(true)
            .build()
    );

    private final Setting<Keybind> keybind = sgGeneral.add(new KeybindSetting.Builder()
            .name("force-keybind")
            .description("What key to press in order to throw Auto XP.")
            .defaultValue(Keybind.none())
            .visible(onlyOnKey::get)
            .build()
    );

    private final Setting<Boolean> autoToggle = sgGeneral.add(new BoolSetting.Builder()
            .name("auto-toggle")
            .description("Toggles off when your armor is repaired / No XP is found.")
            .defaultValue(false)
            .build()
    );


    // Safety
    private final Setting<Double> minHealth = sgSafety.add(new DoubleSetting.Builder()
            .name("min-health")
            .description("Minimum health for Auto XP.")
            .defaultValue(10)
            .range(0,20)
            .sliderRange(0,20)
            .build()
    );

    private final Setting<Boolean> onlyOnGround = sgSafety.add(new BoolSetting.Builder()
            .name("only-on-ground")
            .description("Only allows when you are on the ground.")
            .defaultValue(true)
            .build()
    );

    private final Setting<Boolean> onlyInHole = sgSafety.add(new BoolSetting.Builder()
            .name("only-in-hole")
            .description("Only allows when you are in a hole.")
            .defaultValue(false)
            .build()
    );


    public AutoXpPlus() {
        super(HIGTools.HIG, "auto-xp+", "Automatically repairs your armor and tools in pvp.");
    }


    public boolean isOnStandby;
    private boolean isKey, isHealth, isGround, isHole;
    public boolean isRepairing;


    @Override
    public void onDeactivate() {
        isRepairing = false;
        isOnStandby = false;
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        isKey = (onlyOnKey.get() && keybind.get().isPressed()) || !onlyOnKey.get();
        isHealth = PlayerUtils.getTotalHealth() >= minHealth.get();
        isGround = (onlyOnGround.get() && mc.player.isOnGround()) || !onlyOnGround.get();
        isHole = (onlyInHole.get() && HTEntityUtils.isInHole(mc.player, true, HTEntityUtils.BlastResistantType.Any)) || !onlyInHole.get();

        isOnStandby = isKey && isHealth && isGround && isHole;

        if (!isOnStandby) return;

        if (repaired()) {
            if (autoToggle.get()) {
                warning("Finished repairing... disabling.");
                toggle();
            }
        } else {
            FindItemResult exp = InvUtils.find(Items.EXPERIENCE_BOTTLE);
            FindItemResult hotbarExp = InvUtils.findInHotbar(Items.EXPERIENCE_BOTTLE);

            if (exp.found()) {
                if (!hotbarExp.found()) {
                    if (!replenish.get()) return;
                    InvUtils.move().from(exp.slot()).toHotbar(slot.get() - 1);
                }

                if (lookDown.get()) Rotations.rotate(mc.player.getYaw(), 90, () -> throwExp(hotbarExp));
                else throwExp(hotbarExp);
            } else {
                if (autoToggle.get()) toggle();
            }
        }
    }

    private void throwExp(FindItemResult hotbarExp) {
        if (hotbarExp.isOffhand()) {
            mc.interactionManager.interactItem(mc.player, mc.world, Hand.OFF_HAND);
        } else {
            InvUtils.swap(hotbarExp.slot(), true);
            mc.interactionManager.interactItem(mc.player, mc.world, Hand.MAIN_HAND);
            InvUtils.swapBack();
        }
    }

    private boolean repaired() {
        ItemStack helmet = mc.player.getInventory().getArmorStack(3);
        ItemStack chestplate = mc.player.getInventory().getArmorStack(2);
        ItemStack leggings = mc.player.getInventory().getArmorStack(1);
        ItemStack boots = mc.player.getInventory().getArmorStack(0);

        boolean helmetRepaired;
        boolean chestplateRepaired;
        boolean leggingsRepaired;
        boolean bootsRepaired;

        if (EnchantmentHelper.getLevel(Enchantments.MENDING, helmet) > 0) {
            helmetRepaired = (float) (helmet.getMaxDamage() - helmet.getDamage()) / helmet.getMaxDamage() * 100 >= maxThreshold.get();
        } else helmetRepaired = true;

        if (EnchantmentHelper.getLevel(Enchantments.MENDING, chestplate) > 0) {
            chestplateRepaired = (float) (chestplate.getMaxDamage() - chestplate.getDamage()) / chestplate.getMaxDamage() * 100 >= maxThreshold.get();
        } else chestplateRepaired = true;

        if (EnchantmentHelper.getLevel(Enchantments.MENDING, leggings) > 0) {
            leggingsRepaired = (float) (leggings.getMaxDamage() - leggings.getDamage()) / leggings.getMaxDamage() * 100 >= maxThreshold.get();
        } else leggingsRepaired = true;

        if (EnchantmentHelper.getLevel(Enchantments.MENDING, boots) > 0) {
            bootsRepaired = (float) (boots.getMaxDamage() - boots.getDamage()) / boots.getMaxDamage() * 100 >= maxThreshold.get();
        } else bootsRepaired = true;

        return helmetRepaired && chestplateRepaired && leggingsRepaired && bootsRepaired;
    }
}
