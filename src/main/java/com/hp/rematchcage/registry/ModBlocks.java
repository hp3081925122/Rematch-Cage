package com.hp.rematchcage.registry;

import com.hp.rematchcage.Rematchcage;
import com.hp.rematchcage.block.RematchCageBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModBlocks {
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, Rematchcage.MOD_ID);

    public static final RegistryObject<RematchCageBlock> REMATCH_CAGE = BLOCKS.register(
        "rematch_cage",
        () -> new RematchCageBlock(
            BlockBehaviour.Properties.of()
                .mapColor(MapColor.DEEPSLATE)
                .strength(5.0F, 3600000.0F)
                .requiresCorrectToolForDrops()
                .sound(SoundType.DEEPSLATE_TILES)
                .lightLevel(state -> 7)
                .noOcclusion()
                .pushReaction(PushReaction.BLOCK)
        )
    );

    private ModBlocks() {
    }
}
