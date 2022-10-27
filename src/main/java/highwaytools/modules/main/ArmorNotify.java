package highwaytools.modules.main;

import highwaytools.HighwayTools;
import highwaytools.utils.HTUtils;
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
            .range(1,100)
            .sliderRange(1,100)
            .build()
    );

    public ArmorNotify() {
        super(HighwayTools.MAIN, "armor-notify", "Notifies you when your armor pieces are low.");
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

            if (HTUtils.checkNotifyThreshold(armorPiece, threshold.get())) {
                if (HTUtils.isHelmetArmor(armorPiece) && !alertedHelmet) {
                    warning("Your helmet durability is low.");
                    alertedHelmet = true;
                } else if (HTUtils.isChestplateArmor(armorPiece) && !alertedChestplate) {
                    warning("Your chestplate durability is low.");
                    alertedChestplate = true;
                } else if (HTUtils.isLeggingsArmor(armorPiece) && !alertedLeggings) {
                    warning("Your leggings durability is low.");
                    alertedLeggings = true;
                } else if (HTUtils.isBootsArmor(armorPiece) && !alertedBoots) {
                    warning("Your boots durability is low.");
                    alertedBoots = true;
                }
            } else if (!HTUtils.checkNotifyThreshold(armorPiece, threshold.get())) {
                if (HTUtils.isHelmetArmor(armorPiece) && alertedHelmet) alertedHelmet = false;
                else if (HTUtils.isChestplateArmor(armorPiece) && alertedChestplate) alertedChestplate = false;
                else if (HTUtils.isLeggingsArmor(armorPiece) && alertedLeggings) alertedLeggings = false;
                else if (HTUtils.isBootsArmor(armorPiece) && alertedBoots) alertedBoots = false;
            }
        }
    }
}
