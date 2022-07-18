package higtools.modules.main;

import higtools.modules.HIGTools;
import higtools.utils.HTServerUtils;
import higtools.utils.HTPlayerUtils;
import higtools.utils.HTWorldUtils;
import meteordevelopment.meteorclient.events.entity.player.PlayerMoveEvent;
import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.mixininterface.IVec3d;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.movement.Anchor;
import meteordevelopment.meteorclient.systems.modules.movement.Flight;
import meteordevelopment.meteorclient.systems.modules.movement.LongJump;
import meteordevelopment.meteorclient.systems.modules.movement.elytrafly.ElytraFly;
import meteordevelopment.meteorclient.systems.modules.world.Timer;
import meteordevelopment.meteorclient.utils.misc.Vec2;
import meteordevelopment.meteorclient.utils.player.PlayerUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.Blocks;
import net.minecraft.entity.MovementType;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.network.packet.s2c.play.PlayerPositionLookS2CPacket;
import net.minecraft.util.math.Vec3d;

public class Strafe extends Module {
    public enum Mode {
        Vanilla,
        NCP,
        Smart
    }

    public enum HopMode {
        Auto,
        Custom
    }

    public enum WebbedPause {
        Always,
        OnAir,
        None
    }

    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final SettingGroup sgVanilla = settings.createGroup("Vanilla");
    private final SettingGroup sgNCP = settings.createGroup("NCP");
    private final SettingGroup sgPotion = settings.createGroup("Potions");
    private final SettingGroup sgPause = settings.createGroup("Pause");
    private final SettingGroup sgAC = settings.createGroup("Anti Cheat");

    private final Setting<Mode> mode = sgGeneral.add(new EnumSetting.Builder<Mode>()
            .name("mode")
            .description("Behaviour of your movements")
            .defaultValue(Mode.NCP)
            .build()
    );

    private final Setting<Double> groundTimer = sgGeneral.add(new DoubleSetting.Builder()
            .name("ground-timer")
            .description("Ground timer override.")
            .defaultValue(1)
            .sliderRange(0.001,10)
            .build()
    );

    private final Setting<Double> airTimer = sgGeneral.add(new DoubleSetting.Builder()
            .name("air-timer")
            .description("Air timer override.")
            .defaultValue(1.088)
            .sliderRange(0.001,10)
            .build()
    );

    private final Setting<Boolean> autoSprint = sgGeneral.add(new BoolSetting.Builder()
            .name("auto-sprint")
            .description("Makes you sprint if you are moving forward")
            .defaultValue(false)
            .build()
    );

    private final Setting<Boolean> TPSSync = sgGeneral.add(new BoolSetting.Builder()
            .name("TPS-sync")
            .description("Tries to sync movement with the server's TPS.")
            .defaultValue(false)
            .build()
    );

    // Vanilla
    private final Setting<Double> vanillaSneakSpeed = sgVanilla.add(new DoubleSetting.Builder()
            .name("vanilla-sneak-speed")
            .description("The speed in blocks per second (on ground and sneaking).")
            .defaultValue(2.6)
            .min(0)
            .sliderMax(20)
            .visible(() -> mode.get() == Mode.Vanilla)
            .build()
    );

    private final Setting<Double> vanillaGroundSpeed = sgVanilla.add(new DoubleSetting.Builder()
            .name("vanilla-ground-speed")
            .description("The speed in blocks per second (on ground).")
            .defaultValue(5.6)
            .min(0)
            .sliderMax(20)
            .visible(() -> mode.get() == Mode.Vanilla)
            .build()
    );

    private final Setting<Double> vanillaAirSpeed = sgVanilla.add(new DoubleSetting.Builder()
            .name("vanilla-air-speed")
            .description("The speed in blocks per second (on air).")
            .defaultValue(6)
            .min(0)
            .sliderMax(20)
            .visible(() -> mode.get() == Mode.Vanilla)
            .build()
    );

    private final Setting<Boolean> rubberbandPause = sgVanilla.add(new BoolSetting.Builder()
            .name("pause-on-rubberband")
            .description("Will pause Vanilla mode when you rubberband.")
            .defaultValue(false)
            .visible(() -> mode.get() == Mode.Vanilla)
            .build()
    );

    private final Setting<Integer> rubberbandTime = sgVanilla.add(new IntSetting.Builder()
            .name("pause-time")
            .description("Pauses vanilla mode for x ticks when a rubberband is detected.")
            .defaultValue(30)
            .min(0)
            .sliderMax(100)
            .visible(() -> mode.get() == Mode.Vanilla && rubberbandPause.get())
            .build()
    );

    // NCP
    private final Setting<Double> ncpSpeed = sgNCP.add(new DoubleSetting.Builder()
            .name("NCP-speed")
            .description("The speed.")
            .defaultValue(2)
            .min(0)
            .sliderMax(3)
            .visible(() -> mode.get() != Mode.Vanilla)
            .build()
    );

    private final Setting<Boolean> ncpSpeedLimit = sgNCP.add(new BoolSetting.Builder()
            .name("speed-limit")
            .description("Limits your speed on servers with very strict anticheats.")
            .defaultValue(false)
            .visible(() -> mode.get() != Mode.Vanilla)
            .build()
    );

    private final Setting<Double> startingSpeed = sgNCP.add(new DoubleSetting.Builder()
            .name("starting-speed")
            .description("Initial speed when starting (recommended 1.18 on NCP, 1.080 on Smart).")
            .defaultValue(1.08)
            .min(0)
            .sliderMax(2)
            .visible(() -> mode.get() != Mode.Vanilla)
            .build()
    );

    private final Setting<HopMode> hopMode = sgNCP.add(new EnumSetting.Builder<HopMode>()
            .name("hop-mode")
            .description("Mode to use for the hop height.")
            .defaultValue(HopMode.Auto)
            .visible(() -> mode.get() != Mode.Vanilla)
            .build()
    );

    private final Setting<Double> hopHeight = sgNCP.add(new DoubleSetting.Builder()
            .name("hop-height")
            .description("The hop intensity.")
            .defaultValue(0.401)
            .min(0)
            .sliderMax(1)
            .visible(() -> hopMode.get() == HopMode.Custom && mode.get() != Mode.Vanilla)
            .build()
    );

    private final Setting<Integer> jumpTime = sgNCP.add(new IntSetting.Builder()
            .name("jump-time")
            .description("How many ticks to recognise that you have jumped for smart mode.")
            .defaultValue(20)
            .min(0)
            .sliderMax(30)
            .visible(() -> mode.get() == Mode.Smart)
            .build()
    );

    private final Setting<Double> jumpedSlowDown = sgNCP.add(new DoubleSetting.Builder()
            .name("jumped-slow-down")
            .description("How much to slow down by after jumping.")
            .defaultValue(0.76)
            .min(0)
            .sliderMax(1)
            .visible(() -> mode.get() != Mode.Vanilla)
            .build()
    );

    private final Setting<Double> resetDivisor = sgNCP.add(new DoubleSetting.Builder()
            .name("reset-divisor")
            .description("Speed value get divided by this amount on rubberband or collision.")
            .defaultValue(159)
            .min(0)
            .sliderMax(200)
            .visible(() -> mode.get() != Mode.Vanilla)
            .build()
    );

    // Potions
    private final Setting<Boolean> applyJumpBoost = sgPotion.add(new BoolSetting.Builder()
            .name("jump-boost")
            .description("Apply jump boost effect if the player has it.")
            .defaultValue(true)
            .visible(() -> mode.get() != Mode.Vanilla)
            .build()
    );

    private final Setting<Boolean> applySpeed = sgPotion.add(new BoolSetting.Builder()
            .name("speed-effect")
            .description("Apply speed effect if the player has it.")
            .defaultValue(true)
            .build()
    );

    private final Setting<Boolean> applySlowness = sgPotion.add(new BoolSetting.Builder()
            .name("slowness-effect")
            .description("Apply slowness effect if the player has it.")
            .defaultValue(true)
            .build()
    );

    // Pauses
    private final Setting<Boolean> longJumpPause = sgPause.add(new BoolSetting.Builder()
            .name("pause-on-long-jump")
            .description("Pauses the module if long jump is active.")
            .defaultValue(false)
            .build()
    );

    private final Setting<Boolean> flightPause = sgPause.add(new BoolSetting.Builder()
            .name("pause-on-flight")
            .description("Pauses the module if flight is active.")
            .defaultValue(false)
            .build()
    );

    private final Setting<Boolean> eFlyPause = sgPause.add(new BoolSetting.Builder()
            .name("pause-on-elytra-fly")
            .description("Pauses the module if elytra fly is active.")
            .defaultValue(false)
            .build()
    );

    // Anti Cheat
    private final Setting<Boolean> inWater = sgAC.add(new BoolSetting.Builder()
            .name("in-water")
            .description("Uses speed when in water.")
            .defaultValue(false)
            .build()
    );

    private final Setting<Boolean> inLava = sgAC.add(new BoolSetting.Builder()
            .name("in-lava")
            .description("Uses speed when in lava.")
            .defaultValue(false)
            .build()
    );

    private final Setting<Boolean> whenSneaking = sgAC.add(new BoolSetting.Builder()
            .name("when-sneaking")
            .description("Uses speed when sneaking.")
            .defaultValue(false)
            .build()
    );

    private final Setting<Boolean> hungerCheck = sgAC.add(new BoolSetting.Builder()
            .name("hunger-check")
            .description("Pauses when hunger reaches 3 or less drumsticks")
            .defaultValue(true)
            .build()
    );

    private final Setting<WebbedPause> webbedPause = sgAC.add(new EnumSetting.Builder<WebbedPause>()
            .name("pause-on-webbed")
            .description("Pauses when you are webbed.")
            .defaultValue(WebbedPause.OnAir)
            .build()
    );

    public Strafe() {
        super(HIGTools.MAIN, "strafe", "Increase speed and control.");
    }

    // Fields
    private int stage;
    private double distance, speed;
    private long timer = 0L;
    private int rubberbandTicks;
    private boolean rubberbanded, sentMessage;
    private int jumpTicks;
    private boolean jumped;

    // Modules
    Modules modules = Modules.get();
    Timer timerClass = modules.get(Timer.class);
    Anchor anchor = modules.get(Anchor.class);
    LongJump longJump = modules.get(LongJump.class);
    Flight flight = modules.get(Flight.class);
    ElytraFly efly = modules.get(ElytraFly.class);

    @Override
    public void onDeactivate() {
       timerClass.setOverride(Timer.OFF);
    }

    @EventHandler
    private void onPlayerMove(PlayerMoveEvent event) {
        if (event.type != MovementType.SELF || mc.player.isFallFlying() || mc.player.isClimbing() || mc.player.getVehicle() != null) return;
        if (!whenSneaking.get() && mc.player.isSneaking()) return;
        if (!inWater.get() && mc.player.isTouchingWater()) return;
        if (!inLava.get() && mc.player.isInLava()) return;
        if (hungerCheck.get() && (mc.player.getHungerManager().getFoodLevel() <= 6)) return;

        if (longJumpPause.get() && longJump.isActive()) return;
        if (flightPause.get() && flight.isActive()) return;
        if (eFlyPause.get() && efly.isActive()) return;

        if (HTPlayerUtils.isWebbed(mc.player) && webbedPause.get() == WebbedPause.Always) return;
        if (HTPlayerUtils.isWebbed(mc.player) && !mc.player.isOnGround() && webbedPause.get() == WebbedPause.OnAir) return;

        if (mc.player.isOnGround()) {
           timerClass.setOverride(PlayerUtils.isMoving() ? (groundTimer.get() * HTServerUtils.getTPSMatch(TPSSync.get())) : Timer.OFF);
        } else
           timerClass.setOverride(PlayerUtils.isMoving() ? (airTimer.get() * HTServerUtils.getTPSMatch(TPSSync.get())) : Timer.OFF);

        // Vanilla
        if (mode.get() == Mode.Vanilla && !rubberbanded) {
            if (mc.player.isOnGround()) {
                if (mc.player.isSneaking()) {
                    Vec3d vel = PlayerUtils.getHorizontalVelocity(vanillaSneakSpeed.get());
                    double velX = vel.getX();
                    double velZ = vel.getZ();

                    if (mc.player.hasStatusEffect(StatusEffects.SPEED) && applySpeed.get()) {
                        double value = (mc.player.getStatusEffect(StatusEffects.SPEED).getAmplifier() + 1) * 0.205;
                        velX += velX * value;
                        velZ += velZ * value;
                    }

                    if (mc.player.hasStatusEffect(StatusEffects.SLOWNESS) && applySlowness.get()) {
                        double value = (mc.player.getStatusEffect(StatusEffects.SLOWNESS).getAmplifier() + 1) * 0.205;
                        velX -= velX * value;
                        velZ -= velZ * value;
                    }

                    if (anchor.isActive() && anchor.controlMovement) {
                        velX = anchor.deltaX;
                        velZ = anchor.deltaZ;
                    }

                    ((IVec3d) event.movement).set(velX, event.movement.y, velZ);
                } else {
                    Vec3d vel = PlayerUtils.getHorizontalVelocity(vanillaGroundSpeed.get());
                    double velX = vel.getX();
                    double velZ = vel.getZ();

                    if (mc.player.hasStatusEffect(StatusEffects.SPEED) && applySpeed.get()) {
                        double value = (mc.player.getStatusEffect(StatusEffects.SPEED).getAmplifier() + 1) * 0.205;
                        velX += velX * value;
                        velZ += velZ * value;
                    }

                    if (mc.player.hasStatusEffect(StatusEffects.SLOWNESS) && applySlowness.get()) {
                        double value = (mc.player.getStatusEffect(StatusEffects.SLOWNESS).getAmplifier() + 1) * 0.205;
                        velX -= velX * value;
                        velZ -= velZ * value;
                    }

                    Anchor anchor = Modules.get().get(Anchor.class);
                    if (anchor.isActive() && anchor.controlMovement) {
                        velX = anchor.deltaX;
                        velZ = anchor.deltaZ;
                    }

                    ((IVec3d) event.movement).set(velX, event.movement.y, velZ);
                }
            } else {
                Vec3d vel = PlayerUtils.getHorizontalVelocity(vanillaAirSpeed.get());
                double velX = vel.getX();
                double velZ = vel.getZ();

                if (mc.player.hasStatusEffect(StatusEffects.SPEED) && applySpeed.get()) {
                    double value = (mc.player.getStatusEffect(StatusEffects.SPEED).getAmplifier() + 1) * 0.205;
                    velX += velX * value;
                    velZ += velZ * value;
                }

                if (mc.player.hasStatusEffect(StatusEffects.SLOWNESS) && applySlowness.get()) {
                    double value = (mc.player.getStatusEffect(StatusEffects.SLOWNESS).getAmplifier() + 1) * 0.205;
                    velX -= velX * value;
                    velZ -= velZ * value;
                }

                if (anchor.isActive() && anchor.controlMovement) {
                    velX = anchor.deltaX;
                    velZ = anchor.deltaZ;
                }

                ((IVec3d) event.movement).set(velX, event.movement.y, velZ);
            }
        }

        // NCP
        if (mode.get() == Mode.NCP) {
            switch (stage) {
                case 0: //Reset
                    if (PlayerUtils.isMoving()) {
                        stage++;
                        speed = startingSpeed.get() * getDefaultSpeed() - 0.01;
                    }
                case 1: //Jump
                    if (!PlayerUtils.isMoving() || !mc.player.isOnGround()) break;

                    if (hopMode.get() == HopMode.Auto) ((IVec3d) event.movement).setY(getHop(0.40123128));
                    else ((IVec3d) event.movement).setY(getHop(hopHeight.get()));
                    speed *= ncpSpeed.get();
                    stage++;
                    break;
                case 2:
                    speed = distance - jumpedSlowDown.get() * (distance - getDefaultSpeed());
                    stage++;
                    break; //Slowdown after jump
                case 3: //Reset on collision or predict and update speed
                    if (!mc.world.isSpaceEmpty(mc.player.getBoundingBox().offset(0.0, mc.player.getVelocity().y, 0.0)) || mc.player.verticalCollision && stage > 0) {
                        stage = 0;
                    }
                    speed = distance - (distance / resetDivisor.get());
                    break;
            }

            speed = Math.max(speed, getDefaultSpeed());

            if (ncpSpeedLimit.get()) {
                if (System.currentTimeMillis() - timer > 2500L) {
                    timer = System.currentTimeMillis();
                }

                speed = Math.min(speed, System.currentTimeMillis() - timer > 1250L ? 0.44D : 0.43D);
            }

            Vec2 change = transformStrafe(speed);

            double velX = change.x;
            double velZ = change.y;

            if (anchor.isActive() && anchor.controlMovement) {
                velX = anchor.deltaX;
                velZ = anchor.deltaZ;
            }

            ((IVec3d) event.movement).setXZ(velX, velZ);
        }

        if (mode.get() == Mode.Smart) {
            switch (stage) {
            case 0: //Reset
                if (PlayerUtils.isMoving()) {
                    stage++;
                    speed = startingSpeed.get() * getDefaultSpeed() - 0.01;
                }
            case 1: //Jump
                if (!PlayerUtils.isMoving() || !mc.player.isOnGround()) break;

                if (jumped) {
                    if (hopMode.get() == HopMode.Auto) ((IVec3d) event.movement).setY(getHop(0.40123128));
                    else ((IVec3d) event.movement).setY(getHop(hopHeight.get()));
                    speed *= ncpSpeed.get();
                    stage++;
                }
                break;
            case 2:
                speed = distance - jumpedSlowDown.get() * (distance - getDefaultSpeed());
                stage++;
                break; // Slowdown after jump
            case 3: // Reset on collision or predict and update speed
                if (!mc.world.isSpaceEmpty(mc.player.getBoundingBox().offset(0.0, mc.player.getVelocity().y, 0.0)) || mc.player.verticalCollision && stage > 0) {
                    stage = 0;
                }
                speed = distance - (distance / resetDivisor.get());
                break;
        }

        speed = Math.max(speed, getDefaultSpeed());

        if (ncpSpeedLimit.get()) {
            if (System.currentTimeMillis() - timer > 2500L) {
                timer = System.currentTimeMillis();
            }

            speed = Math.min(speed, System.currentTimeMillis() - timer > 1250L ? 0.44D : 0.43D);
        }

        Vec2 change = transformStrafe(speed);

        double velX = change.x;
        double velZ = change.y;

        if (anchor.isActive() && anchor.controlMovement) {
            velX = anchor.deltaX;
            velZ = anchor.deltaZ;
        }

        ((IVec3d) event.movement).setXZ(velX, velZ);
        }
    }

    @EventHandler
    private void onPreTick(TickEvent.Pre event) {
        if (mc.player.isFallFlying() || mc.player.isClimbing() || mc.player.getVehicle() != null) return;
        if (!whenSneaking.get() && mc.player.isSneaking()) return;
        if (!inWater.get() && mc.player.isTouchingWater()) return;
        if (!inLava.get() && mc.player.isInLava()) return;
        if (hungerCheck.get() && (mc.player.getHungerManager().getFoodLevel() <= 6)) return;

        if (longJumpPause.get() && longJump.isActive()) return;
        if (flightPause.get() && flight.isActive()) return;
        if (eFlyPause.get() && efly.isActive()) return;

        if (HTWorldUtils.doesBoxTouchBlock(mc.player.getBoundingBox(), Blocks.COBWEB) && webbedPause.get() == WebbedPause.Always) return;
        if (HTWorldUtils.doesBoxTouchBlock(mc.player.getBoundingBox(), Blocks.COBWEB) && !mc.player.isOnGround() && webbedPause.get() == WebbedPause.OnAir) return;

        if (mc.player.forwardSpeed > 0 && autoSprint.get()) mc.player.setSprinting(true);

        if (rubberbandPause.get() && mode.get() == Mode.Vanilla ) {
            if (rubberbandTicks > 0) {
                rubberbandTicks--;
                rubberbanded = true;
                info("Rubberband detected... pausing.");
                sentMessage = false;
            } else {
                rubberbanded = false;
                if (!sentMessage) info("Resuming...");
                sentMessage = true;
            }
        }

        if (mode.get() == Mode.Smart) {
            if (mc.options.jumpKey.isPressed() && mc.player.isOnGround()) jumpTicks = jumpTime.get();
            if (jumpTicks > 0) {
                jumped = true;
                jumpTicks--;
            } else jumped = false;

            if (mc.player.isOnGround()) jumpTicks = 0;
        }

        if (mode.get() != Mode.Vanilla) {
            distance = Math.sqrt((mc.player.getX() - mc.player.prevX) * (mc.player.getX() - mc.player.prevX) + (mc.player.getZ() - mc.player.prevZ) * (mc.player.getZ() - mc.player.prevZ));
        }
    }

    @EventHandler
    private void onPacketReceive(PacketEvent.Receive event) {
       if (event.packet instanceof PlayerPositionLookS2CPacket) {
           rubberbandTicks = rubberbandTime.get();
           reset();
       }
    }

    // NCP
    private double getDefaultSpeed() {
        double defaultSpeed = 0.2873;
        if (mc.player.hasStatusEffect(StatusEffects.SPEED) && applySpeed.get()) {
            int amplifier = mc.player.getStatusEffect(StatusEffects.SPEED).getAmplifier();
            defaultSpeed *= 1.0 + 0.2 * (amplifier + 1);
        }
        if (mc.player.hasStatusEffect(StatusEffects.SLOWNESS) && applySlowness.get()) {
            int amplifier = mc.player.getStatusEffect(StatusEffects.SLOWNESS).getAmplifier();
            defaultSpeed /= 1.0 + 0.2 * (amplifier + 1);
        }
        return defaultSpeed;
    }

    private void reset() {
        stage = 0;
        distance = 0;
        speed = 0.2873;
    }

    private double getHop(double height) {
        StatusEffectInstance jumpBoost = mc.player.hasStatusEffect(StatusEffects.JUMP_BOOST) ? mc.player.getStatusEffect(StatusEffects.JUMP_BOOST) : null;
        if (jumpBoost != null && applyJumpBoost.get()) height += (jumpBoost.getAmplifier() + 1) * 0.1f;
        return height;
    }

    private Vec2 transformStrafe(double speed) {
        float forward = mc.player.input.movementForward;
        float side = mc.player.input.movementSideways;
        float yaw = mc.player.prevYaw + (mc.player.getYaw() - mc.player.prevYaw) * mc.getTickDelta();

        double velX, velZ;

        if (forward == 0.0f && side == 0.0f) return new Vec2(0, 0);

        else if (forward != 0.0f) {
            if (side >= 1.0f) {
                yaw += (float) (forward > 0.0f ? -45 : 45);
                side = 0.0f;
            } else if (side <= -1.0f) {
                yaw += (float) (forward > 0.0f ? 45 : -45);
                side = 0.0f;
            }

            if (forward > 0.0f)
                forward = 1.0f;

            else if (forward < 0.0f)
                forward = -1.0f;
        }

        double mx = Math.cos(Math.toRadians(yaw + 90.0f));
        double mz = Math.sin(Math.toRadians(yaw + 90.0f));

        velX = (double) forward * speed * mx + (double) side * speed * mz;
        velZ = (double) forward * speed * mz - (double) side * speed * mx;

        return new Vec2(velX, velZ);
    }
}
