package com.hp.rematchcage.event;

import com.hp.rematchcage.block.RematchCageBlockEntity;
import com.hp.rematchcage.config.RematchCageConfig;
import com.hp.rematchcage.config.RematchMappingManager;
import com.hp.rematchcage.registry.ModBlocks;
import com.mojang.logging.LogUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

public final class RematchCageEvents {
    private static final Logger LOGGER = LogUtils.getLogger();

    private RematchCageEvents() {
    }

    public static void onConfigLoad(ModConfigEvent.Loading event) {
        if (event.getConfig().getSpec() == RematchCageConfig.COMMON_SPEC) {
            RematchMappingManager.reload();
        }
    }

    public static void onConfigReload(ModConfigEvent.Reloading event) {
        if (event.getConfig().getSpec() == RematchCageConfig.COMMON_SPEC) {
            RematchMappingManager.reload();
        }
    }

    public static void onLivingDeath(LivingDeathEvent event) {
        if (event.isCanceled()) {
            return;
        }
        LivingEntity entity = event.getEntity();
        if (entity instanceof Player || !(entity.level() instanceof ServerLevel serverLevel)) {
            return;
        }
        EntityType<?> entityType = entity.getType();
        if (!RematchMappingManager.isMappedEntity(entityType)) {
            return;
        }
        Identifier entityId = BuiltInRegistries.ENTITY_TYPE.getKey(entityType);
        if (entityId == null) {
            return;
        }

        BlockPos origin = clampToWorld(serverLevel, entity.blockPosition());
        if (RematchCageConfig.COMMON.snapToGround.get()) {
            origin = snapDownToGround(serverLevel, origin, RematchCageConfig.COMMON.maxGroundSnapDistance.get());
        }

        BlockPos placePos = findPlaceablePos(serverLevel, origin, RematchCageConfig.COMMON.placementSearchRadius.get());
        if (placePos == null) {
            LOGGER.warn("[RematchCage] Could not find a valid replaceable block near {} for dead entity {}", origin, entityId);
            return;
        }

        BlockState cageState = ModBlocks.REMATCH_CAGE.get().defaultBlockState();
        if (!serverLevel.setBlock(placePos, cageState, Block.UPDATE_ALL)) {
            return;
        }

        Component storedName = (RematchCageConfig.COMMON.preserveCustomName.get() && entity.hasCustomName())
            ? entity.getCustomName()
            : null;

        if (serverLevel.getBlockEntity(placePos) instanceof RematchCageBlockEntity cage) {
            cage.setStoredEntity(entityId, storedName);
        }

        if (RematchCageConfig.COMMON.playEffects.get()) {
            serverLevel.playSound(null, placePos, SoundEvents.RESPAWN_ANCHOR_SET_SPAWN, SoundSource.BLOCKS, 0.95F, 0.9F);
            serverLevel.sendParticles(
                ParticleTypes.SOUL_FIRE_FLAME,
                placePos.getX() + 0.5D,
                placePos.getY() + 0.55D,
                placePos.getZ() + 0.5D,
                22,
                0.3D,
                0.35D,
                0.3D,
                0.025D
            );
        }
    }

    private static BlockPos clampToWorld(ServerLevel level, BlockPos pos) {
        int minY = level.getMinY() + 1;
        int maxY = level.getMaxY() - 2;
        return new BlockPos(pos.getX(), Mth.clamp(pos.getY(), minY, maxY), pos.getZ());
    }

    private static BlockPos snapDownToGround(ServerLevel level, BlockPos start, int maxDrop) {
        int minY = level.getMinY() + 1;
        BlockPos.MutableBlockPos cursor = start.mutable();
        for (int i = 0; i < maxDrop && cursor.getY() > minY; i++) {
            BlockState below = level.getBlockState(cursor.below());
            if (!below.canBeReplaced() && (!below.getCollisionShape(level, cursor.below()).isEmpty() || below.isFaceSturdy(level, cursor.below(), Direction.UP))) {
                return cursor.immutable();
            }
            cursor.move(Direction.DOWN);
        }
        return start;
    }

    @Nullable
    private static BlockPos findPlaceablePos(ServerLevel level, BlockPos center, int radius) {
        if (canPlaceCageAt(level, center)) {
            return center;
        }

        BlockPos fallbackAir = null;
        int minY = level.getMinY() + 1;
        int maxY = level.getMaxY() - 2;

        for (int r = 1; r <= radius; r++) {
            for (int dy = -r; dy <= r; dy++) {
                int y = center.getY() + dy;
                if (y < minY || y > maxY) {
                    continue;
                }
                for (int dx = -r; dx <= r; dx++) {
                    for (int dz = -r; dz <= r; dz++) {
                        if (Math.max(Math.max(Math.abs(dx), Math.abs(dy)), Math.abs(dz)) != r) {
                            continue;
                        }
                        BlockPos candidate = new BlockPos(center.getX() + dx, y, center.getZ() + dz);
                        if (!canPlaceCageAt(level, candidate)) {
                            continue;
                        }
                        BlockState below = level.getBlockState(candidate.below());
                        if (!below.canBeReplaced() && !below.getCollisionShape(level, candidate.below()).isEmpty()) {
                            return candidate;
                        }
                        if (fallbackAir == null) {
                            fallbackAir = candidate;
                        }
                    }
                }
            }
        }
        return fallbackAir;
    }

    private static boolean canPlaceCageAt(ServerLevel level, BlockPos pos) {
        if (!level.getWorldBorder().isWithinBounds(pos)) {
            return false;
        }
        BlockState existing = level.getBlockState(pos);
        if (existing.is(ModBlocks.REMATCH_CAGE.get()) || existing.hasBlockEntity()) {
            return false;
        }
        return existing.isAir() || existing.canBeReplaced();
    }

    public static EventPriority deathPriority() {
        return EventPriority.LOWEST;
    }
}
