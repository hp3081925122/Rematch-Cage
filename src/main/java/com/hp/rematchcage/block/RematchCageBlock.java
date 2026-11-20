package com.hp.rematchcage.block;

import com.hp.rematchcage.config.RematchCageConfig;
import com.hp.rematchcage.config.RematchMappingManager;
import com.mojang.logging.LogUtils;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.neoforge.event.EventHooks;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

public class RematchCageBlock extends Block implements EntityBlock {
    private static final Logger LOGGER = LogUtils.getLogger();

    public RematchCageBlock(Properties properties) {
        super(properties);
    }

    @Override
    public boolean canEntityDestroy(BlockState state, BlockGetter level, BlockPos pos, Entity entity) {
        return false;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new RematchCageBlockEntity(pos, state);
    }

    @Override
    public ItemStack getCloneItemStack(LevelReader level, BlockPos pos, BlockState state, boolean includeData, Player player) {
        ItemStack stack = super.getCloneItemStack(level, pos, state, includeData, player);
        if (level.getBlockEntity(pos) instanceof RematchCageBlockEntity cage && cage.getEntityId() != null) {
            TagValueOutput output = TagValueOutput.createWithContext(ProblemReporter.DISCARDING, level.registryAccess());
            output.putString(RematchCageBlockEntity.TAG_ENTITY_ID, cage.getEntityId().toString());
            output.storeNullable(RematchCageBlockEntity.TAG_CUSTOM_NAME, ComponentSerialization.CODEC, cage.getCustomName());
            BlockItem.setBlockEntityData(stack, cage.getType(), output);
        }
        return stack;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (!(level.getBlockEntity(pos) instanceof RematchCageBlockEntity cage)) {
            return InteractionResult.PASS;
        }
        if (cage.getEntityId() == null) {
            if (!level.isClientSide()) {
                player.sendOverlayMessage(Component.translatable("message.rematchcage.empty_cage"));
            }
            return InteractionResult.SUCCESS;
        }
        EntityType<?> targetType = cage.getEntityType();
        if (targetType == null) {
            if (!level.isClientSide()) {
                player.sendOverlayMessage(Component.translatable("message.rematchcage.missing_entity"));
            }
            return InteractionResult.CONSUME;
        }
        if (!level.isClientSide()) {
            displayRequirementHint(player, targetType);
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    protected InteractionResult useItemOn(ItemStack heldStack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (heldStack.isEmpty()) {
            return InteractionResult.TRY_WITH_EMPTY_HAND;
        }
        if (!(level.getBlockEntity(pos) instanceof RematchCageBlockEntity cage)) {
            return InteractionResult.TRY_WITH_EMPTY_HAND;
        }

        if (cage.getEntityId() == null) {
            if (player.getAbilities().instabuild && heldStack.getItem() instanceof SpawnEggItem spawnEgg) {
                EntityType<?> eggType = spawnEgg.getType(heldStack);
                if (eggType != null) {
                    Identifier eggKey = BuiltInRegistries.ENTITY_TYPE.getKey(eggType);
                    if (eggKey != null) {
                        if (!level.isClientSide()) {
                            cage.setStoredEntity(eggKey, null);
                            level.playSound(null, pos, SoundEvents.RESPAWN_ANCHOR_SET_SPAWN, SoundSource.BLOCKS, 0.9F, 1.15F);
                            player.sendOverlayMessage(Component.translatable("message.rematchcage.bound_entity", eggType.getDescription()));
                        }
                        return InteractionResult.SUCCESS;
                    }
                }
            }
            if (hand == InteractionHand.MAIN_HAND && !level.isClientSide()) {
                player.sendOverlayMessage(Component.translatable("message.rematchcage.empty_cage"));
            }
            return InteractionResult.SUCCESS;
        }

        EntityType<?> targetType = cage.getEntityType();
        if (targetType == null) {
            if (hand == InteractionHand.MAIN_HAND && !level.isClientSide()) {
                player.sendOverlayMessage(Component.translatable("message.rematchcage.missing_entity"));
            }
            return InteractionResult.CONSUME;
        }

        if (!RematchMappingManager.matchesActivationItem(targetType, heldStack)) {
            if (hand == InteractionHand.OFF_HAND) {
                return InteractionResult.TRY_WITH_EMPTY_HAND;
            }
            if (RematchMappingManager.matchesActivationItem(targetType, player.getOffhandItem())) {
                return InteractionResult.PASS;
            }
            if (!level.isClientSide()) {
                displayRequirementHint(player, targetType);
            }
            return InteractionResult.SUCCESS;
        }

        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }

        if (!(level instanceof ServerLevel serverLevel)) {
            return InteractionResult.CONSUME;
        }

        Identifier backupId = cage.getEntityId();
        Component backupName = cage.getCustomName();
        serverLevel.removeBlock(pos, false);

        boolean summoned = summonTargetEntity(serverLevel, pos, player, targetType, backupName);
        if (!summoned) {
            serverLevel.setBlock(pos, state, Block.UPDATE_ALL);
            if (serverLevel.getBlockEntity(pos) instanceof RematchCageBlockEntity restored) {
                restored.setStoredEntity(backupId, backupName);
            }
            LOGGER.error("[RematchCage] Failed to summon entity {} at {}", backupId, pos);
            return InteractionResult.CONSUME;
        }

        if (!player.getAbilities().instabuild) {
            heldStack.shrink(1);
        }

        if (RematchCageConfig.COMMON.playEffects.get()) {
            serverLevel.playSound(null, pos, SoundEvents.RESPAWN_ANCHOR_CHARGE, SoundSource.BLOCKS, 1.0F, 0.9F);
            serverLevel.playSound(null, pos, SoundEvents.EVOKER_PREPARE_SUMMON, SoundSource.HOSTILE, 0.9F, 0.95F);
            serverLevel.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, pos.getX() + 0.5D, pos.getY() + 0.6D, pos.getZ() + 0.5D, 28, 0.35D, 0.45D, 0.35D, 0.04D);
            serverLevel.sendParticles(ParticleTypes.POOF, pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D, 16, 0.3D, 0.3D, 0.3D, 0.03D);
        }
        return InteractionResult.SUCCESS;
    }

    private static void displayRequirementHint(Player player, EntityType<?> targetType) {
        Optional<ItemStack> representative = RematchMappingManager.findRepresentativeItem(targetType);
        if (representative.isPresent()) {
            player.sendOverlayMessage(
                Component.translatable("message.rematchcage.need_item", representative.get().getHoverName(), targetType.getDescription())
            );
        } else {
            player.sendOverlayMessage(
                Component.translatable("message.rematchcage.unmapped_entity", targetType.getDescription())
            );
        }
    }

    private static boolean summonTargetEntity(ServerLevel level, BlockPos pos, Player summoner, EntityType<?> targetType, @Nullable Component storedCustomName) {
        Entity entity;
        try {
            entity = targetType.create(level, EntitySpawnReason.SPAWNER);
        } catch (Throwable t) {
            LOGGER.error("[RematchCage] Exception creating entity {}", BuiltInRegistries.ENTITY_TYPE.getKey(targetType), t);
            return false;
        }
        if (entity == null) {
            return false;
        }

        double spawnX = pos.getX() + 0.5D;
        double spawnY = pos.getY();
        double spawnZ = pos.getZ() + 0.5D;
        float yaw = summoner.getYRot() + 180.0F;
        entity.snapTo(spawnX, spawnY, spawnZ, yaw, 0.0F);

        if (!level.noCollision(entity)) {
            for (int dy = 1; dy <= 2; dy++) {
                entity.setPos(spawnX, spawnY + dy, spawnZ);
                if (level.noCollision(entity)) {
                    break;
                }
            }
            if (!level.noCollision(entity)) {
                entity.setPos(spawnX, spawnY, spawnZ);
            }
        }

        if (entity instanceof Mob mob) {
            mob.yHeadRot = yaw;
            mob.yBodyRot = yaw;
            try {
                EventHooks.finalizeMobSpawn(mob, level, level.getCurrentDifficultyAt(pos), EntitySpawnReason.SPAWNER, null);
            } catch (Throwable t) {
                LOGGER.warn("[RematchCage] Exception in finalizeMobSpawn for {}", BuiltInRegistries.ENTITY_TYPE.getKey(targetType), t);
            }
            mob.setPersistenceRequired();
        }

        if (RematchCageConfig.COMMON.preserveCustomName.get() && storedCustomName != null) {
            entity.setCustomName(storedCustomName);
        }

        return level.tryAddFreshEntityWithPassengers(entity);
    }
}
