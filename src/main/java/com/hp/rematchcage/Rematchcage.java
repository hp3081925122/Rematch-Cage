package com.hp.rematchcage;

import com.hp.rematchcage.config.RematchCageConfig;
import com.hp.rematchcage.event.RematchCageEvents;
import com.hp.rematchcage.registry.ModBlockEntities;
import com.hp.rematchcage.registry.ModBlocks;
import com.hp.rematchcage.registry.ModItems;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;

@Mod(Rematchcage.MOD_ID)
public final class Rematchcage {
    public static final String MOD_ID = "rematchcage";

    public Rematchcage(IEventBus modEventBus, ModContainer modContainer) {
        RematchCageConfig.register(modContainer);

        ModBlocks.BLOCKS.register(modEventBus);
        ModBlockEntities.BLOCK_ENTITIES.register(modEventBus);
        ModItems.ITEMS.register(modEventBus);

        modEventBus.addListener(ModItems::onBuildTabContents);
        modEventBus.addListener(RematchCageEvents::onConfigLoad);
        modEventBus.addListener(RematchCageEvents::onConfigReload);

        NeoForge.EVENT_BUS.addListener(RematchCageEvents.deathPriority(), false, RematchCageEvents::onLivingDeath);
    }

    public static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
    }
}
