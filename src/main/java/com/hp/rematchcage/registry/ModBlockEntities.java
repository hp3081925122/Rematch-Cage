package com.hp.rematchcage.registry;

import com.hp.rematchcage.Rematchcage;
import com.hp.rematchcage.block.RematchCageBlockEntity;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
        DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, Rematchcage.MOD_ID);

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<RematchCageBlockEntity>> REMATCH_CAGE =
        BLOCK_ENTITIES.register(
            "rematch_cage",
            () -> BlockEntityType.Builder.of(RematchCageBlockEntity::new, ModBlocks.REMATCH_CAGE.get()).build(null)
        );

    private ModBlockEntities() {
    }
}
