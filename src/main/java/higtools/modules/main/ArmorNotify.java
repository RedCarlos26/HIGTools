package higtools.modules.main;

import higtools.HIGTools;
import higtools.utils.HIGUtils;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.DoubleSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.item.ItemStack;

public class ArmorNotify extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Double> threshold = sgGeneral.add(new DoubleSetting.Builder()
        .name("durability")
        .description("How low an armor piece needs to be to alert you (in %).")
        .defaultValue(20)
        .range(1, 100)
        .sliderRange(1, 100)
        .build()
    );

    public ArmorNotify() {
        super(HIGTools.MAIN, "armor-notify", "Notifies you when your armor pieces are low.");
    }

    private boolean alertedHelmet;
    private boolean alertedChestplate;
    private boolean alertedLeggings;
    private boolean alertedBoots;

    @Override
    public void onActivate() {
        alertedHelmet = false;
        alertedChestplate = false;
        alertedLeggings = false;
        alertedBoots = false;
    }

    @EventHandler
    private void onTick(TickEvent.Post event) {
        Iterable<ItemStack> armorPieces = mc.player.getArmorItems();
        for (ItemStack armorPiece : armorPieces) {

            if (HIGUtils.checkNotifyThreshold(armorPiece, threshold.get())) {
                if (HIGUtils.isHelmetArmor(armorPiece) && !alertedHelmet) {
                    warning("Your helmet durability is low.");
                    alertedHelmet = true;
                } else if (HIGUtils.isChestplateArmor(armorPiece) && !alertedChestplate) {
                    warning("Your chestplate durability is low.");
                    alertedChestplate = true;
                } else if (HIGUtils.isLeggingsArmor(armorPiece) && !alertedLeggings) {
                    warning("Your leggings durability is low.");
                    alertedLeggings = true;
                } else if (HIGUtils.isBootsArmor(armorPiece) && !alertedBoots) {
                    warning("Your boots durability is low.");
                    alertedBoots = true;
                }
            } else if (!HIGUtils.checkNotifyThreshold(armorPiece, threshold.get())) {
                if (HIGUtils.isHelmetArmor(armorPiece) && alertedHelmet) alertedHelmet = false;
                else if (HIGUtils.isChestplateArmor(armorPiece) && alertedChestplate) alertedChestplate = false;
                else if (HIGUtils.isLeggingsArmor(armorPiece) && alertedLeggings) alertedLeggings = false;
                else if (HIGUtils.isBootsArmor(armorPiece) && alertedBoots) alertedBoots = false;
            }
        }
    }
}
