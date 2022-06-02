package higtools.utils;

import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.events.game.GameJoinedEvent;
import meteordevelopment.meteorclient.mixininterface.IRaycastContext;
import meteordevelopment.meteorclient.mixininterface.IVec3d;
import meteordevelopment.meteorclient.systems.friends.Friends;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.AnvilBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.EnderChestBlock;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.DamageUtil;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Items;
import net.minecraft.item.SwordItem;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.*;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.explosion.Explosion;

import java.util.Objects;

import static meteordevelopment.meteorclient.MeteorClient.mc;

public class HTDamageUtils {
    private static final Vec3d vec3d = new Vec3d(0, 0, 0);
    private static Explosion explosion;
    private static RaycastContext raycastContext;

    public static void init() {
        MeteorClient.EVENT_BUS.subscribe(HTDamageUtils.class);
    }

    @EventHandler
    private static void onGameJoined(GameJoinedEvent event) {
        explosion = new Explosion(mc.world, null, 0, 0, 0, 6, false, Explosion.DestructionType.DESTROY);
        raycastContext = new RaycastContext(null, null, RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.ANY, mc.player);
    }


    // Sword damage

    public static float getSwordDamage(PlayerEntity entity, boolean charged) {
        // Get sword damage
        float damage = 0;
        if (charged) {
            if (entity.getActiveItem().getItem() == Items.NETHERITE_SWORD) {
                damage += 8;
            } else if (entity.getActiveItem().getItem() == Items.DIAMOND_SWORD) {
                damage += 7;
            } else if (entity.getActiveItem().getItem() == Items.GOLDEN_SWORD) {
                damage += 4;
            } else if (entity.getActiveItem().getItem() == Items.IRON_SWORD) {
                damage += 6;
            } else if (entity.getActiveItem().getItem() == Items.STONE_SWORD) {
                damage += 5;
            } else if (entity.getActiveItem().getItem() == Items.WOODEN_SWORD) {
                damage += 4;
            }
            damage *= 1.5;
        }

        if (entity.getActiveItem().getEnchantments() != null) {
            if (EnchantmentHelper.get(entity.getActiveItem()).containsKey(Enchantments.SHARPNESS)) {
                int level = EnchantmentHelper.getLevel(Enchantments.SHARPNESS, entity.getActiveItem());
                damage += (0.5 * level) + 0.5;
            }
        }

        if (entity.getActiveStatusEffects().containsKey(StatusEffects.STRENGTH)) {
            int strength = Objects.requireNonNull(entity.getStatusEffect(StatusEffects.STRENGTH)).getAmplifier() + 1;
            damage += 3 * strength;
        }

        // Reduce by resistance
        damage = resistanceReduction(entity, damage);

        // Reduce by armour
        damage = DamageUtil.getDamageLeft(damage, (float) entity.getArmor(), (float) entity.getAttributeInstance(EntityAttributes.GENERIC_ARMOR_TOUGHNESS).getValue());

        // Reduce by enchants
        damage = normalProtReduction(entity, damage);

        return damage < 0 ? 0 : damage;
    }


    // Utils

    private static float getDamageForDifficulty(float damage) {
        return switch (mc.world.getDifficulty()) {
            case PEACEFUL -> 0;
            case EASY     -> Math.min(damage * 0.5f + 1, damage);
            case HARD     -> damage * 1.5f;
            default       -> damage;
        };
    }

    private static float normalProtReduction(Entity player, float damage) {
        int protLevel = EnchantmentHelper.getProtectionAmount(player.getArmorItems(), DamageSource.GENERIC);
        if (protLevel > 20) protLevel = 20;

        damage *= 1 - (protLevel * 0.04);
        return damage < 0 ? 0 : damage;
    }

    private static float blastProtReduction(Entity player, float damage, Explosion explosion) {
        int protLevel = EnchantmentHelper.getProtectionAmount(player.getArmorItems(), DamageSource.explosion(explosion));
        if (protLevel > 20) protLevel = 20;

        damage *= (1 - (protLevel * 0.04));
        return damage < 0 ? 0 : damage;
    }

    private static float resistanceReduction(LivingEntity player, float damage) {
        if (player.hasStatusEffect(StatusEffects.RESISTANCE)) {
            int lvl = (player.getStatusEffect(StatusEffects.RESISTANCE).getAmplifier() + 1);
            damage *= (1 - (lvl * 0.2));
        }

        return damage < 0 ? 0 : damage;
    }

    private static float getExposure(Vec3d source, Entity entity, boolean predictMovement, RaycastContext raycastContext, boolean ignoreTerrain, boolean fullBlocks) {
        Box box = entity.getBoundingBox();
        if (predictMovement) {
            Vec3d v = entity.getVelocity();
            box.offset(v.x, v.y, v.z);
        }

        double d = 1 / ((box.maxX - box.minX) * 2 + 1);
        double e = 1 / ((box.maxY - box.minY) * 2 + 1);
        double f = 1 / ((box.maxZ - box.minZ) * 2 + 1);
        double g = (1 - Math.floor(1 / d) * d) * 0.5;
        double h = (1 - Math.floor(1 / f) * f) * 0.5;

        if (!(d < 0) && !(e < 0) && !(f < 0)) {
            int i = 0;
            int j = 0;

            for (float k = 0; k <= 1; k += d) {
                for (float l = 0; l <= 1; l += e) {
                    for (float m = 0; m <= 1; m += f) {
                        double n = MathHelper.lerp(k, box.minX, box.maxX);
                        double o = MathHelper.lerp(l, box.minY, box.maxY);
                        double p = MathHelper.lerp(m, box.minZ, box.maxZ);

                        ((IVec3d) vec3d).set(n + g, o, p + h);
                        ((IRaycastContext) raycastContext).set(vec3d, source, RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE, entity);

                        if (raycast(raycastContext, ignoreTerrain, fullBlocks).getType() == HitResult.Type.MISS) i++;

                        j++;
                    }
                }
            }

            return (float) i / j;
        }

        return 0;
    }

    private static BlockHitResult raycast(RaycastContext context, boolean ignoreTerrain, boolean fullBlocks) {
        return BlockView.raycast(context.getStart(), context.getEnd(), context, (raycastContext, blockPos) -> {
            BlockState blockState;

            blockState = mc.world.getBlockState(blockPos);
            if (blockState.getBlock() instanceof AnvilBlock && fullBlocks) blockState = Blocks.OBSIDIAN.getDefaultState();
            else if (blockState.getBlock() instanceof EnderChestBlock && fullBlocks) blockState = Blocks.OBSIDIAN.getDefaultState();
            else if (blockState.getBlock().getBlastResistance() < 600 && ignoreTerrain) blockState = Blocks.AIR.getDefaultState();

            Vec3d vec3d = raycastContext.getStart();
            Vec3d vec3d2 = raycastContext.getEnd();

            VoxelShape voxelShape = raycastContext.getBlockShape(blockState, mc.world, blockPos);
            BlockHitResult blockHitResult = mc.world.raycastBlock(vec3d, vec3d2, blockPos, voxelShape, blockState);
            VoxelShape voxelShape2 = VoxelShapes.empty();
            BlockHitResult blockHitResult2 = voxelShape2.raycast(vec3d, vec3d2, blockPos);

            double d = blockHitResult == null ? Double.MAX_VALUE : raycastContext.getStart().squaredDistanceTo(blockHitResult.getPos());
            double e = blockHitResult2 == null ? Double.MAX_VALUE : raycastContext.getStart().squaredDistanceTo(blockHitResult2.getPos());

            return d <= e ? blockHitResult : blockHitResult2;
        }, (raycastContext) -> {
            Vec3d vec3d = raycastContext.getStart().subtract(raycastContext.getEnd());
            return BlockHitResult.createMissed(raycastContext.getEnd(), Direction.getFacing(vec3d.x, vec3d.y, vec3d.z), new BlockPos(raycastContext.getEnd()));
        });
    }

    public static float possibleHealthReductions(boolean crystals, double explosionRadius , boolean swords, float enemyDistance) {
        float damageTaken = 0;

        for (Entity entity : mc.world.getEntities()) {
            if (swords) {
                // Check for players holding swords
                if (entity instanceof PlayerEntity && damageTaken < getSwordDamage((PlayerEntity) entity, true)) {
                    if (!Friends.get().isFriend((PlayerEntity) entity) && mc.player.getPos().distanceTo(entity.getPos()) < enemyDistance) {
                        if (((PlayerEntity) entity).getActiveItem().getItem() instanceof SwordItem) {
                            damageTaken = getSwordDamage((PlayerEntity) entity, true);
                        }
                    }
                }
            }
        }
        return damageTaken;
    }
}

