/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 * Enhanced by RedCarlos26
 */

package me.redcarlos.higtools.modules.main;

import me.redcarlos.higtools.HIGTools;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.misc.input.Input;
import meteordevelopment.meteorclient.utils.player.FindItemResult;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.meteorclient.utils.world.TickRate;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.item.Items;

public class AutoWalkHig extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Integer> resumeTPS = sgGeneral.add(new IntSetting.Builder()
        .name("resume-tps")
        .description("TPS at which to resume walking.")
        .defaultValue(18)
        .range(1, 20)
        .sliderRange(1, 20)
        .build()
    );

    private final Setting<Boolean> pickToggle = sgGeneral.add(new BoolSetting.Builder()
        .name("pickaxe-toggle")
        .description("Automatically disables AutoWalk-HIG when you run out of pickaxes.")
        .defaultValue(true)
        .build()
    );

    private boolean sentMessage;

    public AutoWalkHig() {
        super(HIGTools.Main, "auto-walk-hig", "Automatically walks forward (optimized for highway digging).");
    }

    @Override
    public void onActivate() {
        sentMessage = false;
    }

    @Override
    public void onDeactivate() {
        unpress();
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        float timeSinceLastTick = TickRate.INSTANCE.getTimeSinceLastTick();

        if (timeSinceLastTick >= 1.4f) {
            if (!sentMessage) error("Server is lagging, pausing.");
            sentMessage = true;
            unpress();
            return;
        }

        float TPS = (TickRate.INSTANCE.getTickRate());
        float i = (resumeTPS.get());

        if (TPS > i) {
            setPressed(mc.options.forwardKey, true);
        } else return;
        sentMessage = false;

        if (pickToggle.get()) {
            FindItemResult pickaxe = InvUtils.find(itemStack -> itemStack.getItem() == Items.DIAMOND_PICKAXE || itemStack.getItem() == Items.NETHERITE_PICKAXE);
            if (!pickaxe.found()) {
                error("No pickaxe found, disabling.");
                toggle();
            }
        }
    }

    private void unpress() {
        setPressed(mc.options.forwardKey, false);
    }

    private void setPressed(KeyBinding key, boolean pressed) {
        key.setPressed(pressed);
        Input.setKeyState(key, pressed);
    }
}
