package com.hp.rematchcage.client;

import com.hp.rematchcage.Rematchcage;
import com.hp.rematchcage.registry.ModBlockEntities;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

@EventBusSubscriber(modid = Rematchcage.MOD_ID, value = Dist.CLIENT)
public final class RematchCageClient {
    private RematchCageClient() {
    }

    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(ModBlockEntities.REMATCH_CAGE.get(), RematchCageRenderer::new);
    }
}
