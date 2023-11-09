/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 * Enhanced by RedCarlos26
 */

package me.redcarlos.higtools.modules.main;

import me.redcarlos.higtools.HIGTools;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.misc.input.Input;
import meteordevelopment.meteorclient.utils.player.FindItemResult;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.meteorclient.utils.world.TickRate;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.item.Items;

public class HIGAutoWalk extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Direction> direction = sgGeneral.add(new EnumSetting.Builder<Direction>()
        .name("direction")
        .description("The direction to walk in.")
        .defaultValue(Direction.Forwards)
        .onChanged(direction1 -> {
            if (isActive()) unpress();
        })
        .build()
    );

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
        .description("Automatically disables AutoWalk+ when you run out of pickaxes.")
        .defaultValue(true)
        .build()
    );

    private boolean sentMessage;

    public HIGAutoWalk() {
        super(HIGTools.Main, "HIG-auto-walk", "Automatically walks forward (optimized for highway digging).");
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
            switch (direction.get()) {
                case Forwards -> {
                    setPressed(mc.options.forwardKey, true);
                }
                case Backwards -> {
                    setPressed(mc.options.backKey, true);
                }
                case Left -> {
                    setPressed(mc.options.leftKey, true);
                }
                case Right -> {
                    setPressed(mc.options.rightKey, true);
                }
            }
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
        setPressed(mc.options.backKey, false);
        setPressed(mc.options.leftKey, false);
        setPressed(mc.options.rightKey, false);
    }

    private void setPressed(KeyBinding key, boolean pressed) {
        key.setPressed(pressed);
        Input.setKeyState(key, pressed);
    }

    public enum Direction {
        Forwards,
        Backwards,
        Left,
        Right
    }
}
