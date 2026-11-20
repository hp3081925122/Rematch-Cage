package com.hp.rematchcage.registry;

import com.hp.rematchcage.Rematchcage;
import com.hp.rematchcage.block.RematchCageBlock;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModBlocks {
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(Registries.BLOCK, Rematchcage.MOD_ID);

    public static final DeferredHolder<Block, RematchCageBlock> REMATCH_CAGE = BLOCKS.register(
        "rematch_cage",
        () -> new RematchCageBlock(
            BlockBehaviour.Properties.of()
                .setId(ResourceKey.create(Registries.BLOCK, Rematchcage.id("rematch_cage")))
                .mapColor(MapColor.STONE)
                .strength(5.0F, 3600000.0F)
                .requiresCorrectToolForDrops()
                .sound(SoundType.METAL)
                .lightLevel(state -> 7)
                .noOcclusion()
                .pushReaction(PushReaction.BLOCK)
        )
    );

    private ModBlocks() {
    }
}
