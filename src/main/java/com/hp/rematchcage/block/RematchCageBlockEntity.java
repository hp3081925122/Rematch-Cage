package com.hp.rematchcage.block;

import com.hp.rematchcage.registry.ModBlockEntities;
import com.mojang.logging.LogUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

public class RematchCageBlockEntity extends BlockEntity {
    public static final String TAG_ENTITY_ID = "EntityId";
    public static final String TAG_CUSTOM_NAME = "CustomName";
    private static final Logger LOGGER = LogUtils.getLogger();

    @Nullable
    private ResourceLocation entityId;
    @Nullable
    private Component customName;

    @Nullable
    private Entity cachedDisplayEntity;
    @Nullable
    private ResourceLocation cachedDisplayId;
    @Nullable
    private Level cachedDisplayLevel;
    private boolean displayCreationFailed;

    public RematchCageBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.REMATCH_CAGE.get(), pos, state);
    }

    @Nullable
    public ResourceLocation getEntityId() {
        return this.entityId;
    }

    @Nullable
    public EntityType<?> getEntityType() {
        if (this.entityId == null || !BuiltInRegistries.ENTITY_TYPE.containsKey(this.entityId)) {
            return null;
        }
        return BuiltInRegistries.ENTITY_TYPE.get(this.entityId);
    }

    @Nullable
    public Component getCustomName() {
        return this.customName;
    }

    public void setStoredEntity(@Nullable ResourceLocation entityId, @Nullable Component customName) {
        this.entityId = entityId;
        this.customName = customName;
        this.invalidateDisplayCache();
        this.setChanged();
        if (this.level != null) {
            this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), Block.UPDATE_ALL);
        }
    }

    private void invalidateDisplayCache() {
        this.cachedDisplayEntity = null;
        this.cachedDisplayId = null;
        this.cachedDisplayLevel = null;
        this.displayCreationFailed = false;
    }

    @Nullable
    public Entity getOrCreateDisplayEntity(Level level) {
        if (this.entityId == null || level == null) {
            return null;
        }
        if (this.displayCreationFailed && this.entityId.equals(this.cachedDisplayId) && this.cachedDisplayLevel == level) {
            return null;
        }
        if (this.cachedDisplayEntity != null && this.entityId.equals(this.cachedDisplayId) && this.cachedDisplayLevel == level) {
            return this.cachedDisplayEntity;
        }
        this.cachedDisplayId = this.entityId;
        this.cachedDisplayLevel = level;
        EntityType<?> type = this.getEntityType();
        if (type == null) {
            this.displayCreationFailed = true;
            return null;
        }
        try {
            Entity created = type.create(level);
            if (created == null) {
                this.displayCreationFailed = true;
                return null;
            }
            created.noPhysics = true;
            this.cachedDisplayEntity = created;
            this.displayCreationFailed = false;
            return created;
        } catch (Throwable t) {
            this.displayCreationFailed = true;
            LOGGER.warn("[RematchCage] Failed to instantiate client display entity for {}", this.entityId, t);
            return null;
        }
    }

    public void markDisplayRenderFailed() {
        this.displayCreationFailed = true;
        this.cachedDisplayEntity = null;
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        if (this.entityId != null) {
            tag.putString(TAG_ENTITY_ID, this.entityId.toString());
        }
        if (this.customName != null) {
            tag.putString(TAG_CUSTOM_NAME, Component.Serializer.toJson(this.customName, registries));
        }
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        ResourceLocation loadedId = null;
        if (tag.contains(TAG_ENTITY_ID, Tag.TAG_STRING)) {
            loadedId = ResourceLocation.tryParse(tag.getString(TAG_ENTITY_ID));
        }
        Component loadedName = null;
        if (tag.contains(TAG_CUSTOM_NAME, Tag.TAG_STRING)) {
            try {
                loadedName = Component.Serializer.fromJson(tag.getString(TAG_CUSTOM_NAME), registries);
            } catch (Exception ignored) {
            }
        }
        this.entityId = loadedId;
        this.customName = loadedName;
        this.invalidateDisplayCache();
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return this.saveWithoutMetadata(registries);
    }

    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
}
