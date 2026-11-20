package com.hp.rematchcage;

import com.hp.rematchcage.config.RematchCageConfig;
import com.hp.rematchcage.event.RematchCageEvents;
import com.hp.rematchcage.registry.ModBlockEntities;
import com.hp.rematchcage.registry.ModBlocks;
import com.hp.rematchcage.registry.ModItems;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(Rematchcage.MOD_ID)
public final class Rematchcage {
    public static final String MOD_ID = "rematchcage";

    public Rematchcage(FMLJavaModLoadingContext context) {
        IEventBus modEventBus = context.getModEventBus();
        RematchCageConfig.register(context);

        ModBlocks.BLOCKS.register(modEventBus);
        ModBlockEntities.BLOCK_ENTITIES.register(modEventBus);
        ModItems.ITEMS.register(modEventBus);

        modEventBus.addListener(ModItems::onBuildTabContents);
        modEventBus.addListener(RematchCageEvents::onConfigLoad);
        modEventBus.addListener(RematchCageEvents::onConfigReload);

        MinecraftForge.EVENT_BUS.addListener(RematchCageEvents.deathPriority(), false, RematchCageEvents::onLivingDeath);
    }

    public static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
    }
}
