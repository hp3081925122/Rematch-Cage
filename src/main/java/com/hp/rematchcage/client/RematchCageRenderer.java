package com.hp.rematchcage.client;

import com.hp.rematchcage.block.RematchCageBlockEntity;
import com.hp.rematchcage.config.RematchCageConfig;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.logging.LogUtils;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import org.slf4j.Logger;

public class RematchCageRenderer implements BlockEntityRenderer<RematchCageBlockEntity> {
    private static final Logger LOGGER = LogUtils.getLogger();

    public RematchCageRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(RematchCageBlockEntity blockEntity, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        if (!RematchCageConfig.CLIENT.renderEntityModel.get()) {
            return;
        }
        Level level = blockEntity.getLevel();
        if (level == null) {
            return;
        }
        Entity displayEntity = blockEntity.getOrCreateDisplayEntity(level);
        if (displayEntity == null) {
            return;
        }

        float time = (level.getGameTime() + partialTick);
        float spinSpeed = RematchCageConfig.CLIENT.spinDegreesPerTick.get().floatValue();
        float bobAmp = RematchCageConfig.CLIENT.bobAmplitude.get().floatValue();
        float yaw = (time * spinSpeed) % 360.0F;
        double bobOffset = Mth.sin(time * 0.1F) * bobAmp;

        float bbWidth = Math.max(0.25F, displayEntity.getBbWidth());
        float bbHeight = Math.max(0.25F, displayEntity.getBbHeight());
        float baseScale = RematchCageConfig.CLIENT.baseEntityScale.get().floatValue();
        float maxDim = Math.max(bbWidth, bbHeight);
        float scale = maxDim > 1.0F ? (baseScale / maxDim) : baseScale;
        scale = Math.min(scale, Math.min(0.50F / bbWidth, 0.48F / bbHeight));

        int boostedLight = LightTexture.pack(
            Math.max(LightTexture.block(packedLight), 12),
            LightTexture.sky(packedLight)
        );

        EntityRenderDispatcher dispatcher = Minecraft.getInstance().getEntityRenderDispatcher();
        int stackDepthBefore = 0;
        poseStack.pushPose();
        stackDepthBefore++;
        dispatcher.setRenderShadow(false);
        try {
            poseStack.translate(0.5D, 0.30D + bobOffset, 0.5D);
            poseStack.mulPose(Axis.YP.rotationDegrees(yaw));
            poseStack.scale(scale, scale, scale);
            dispatcher.render(displayEntity, 0.0D, 0.0D, 0.0D, 0.0F, 0.0F, poseStack, bufferSource, boostedLight);
        } catch (Throwable t) {
            blockEntity.markDisplayRenderFailed();
            LOGGER.warn("[RematchCage] Disabled miniature rendering for {} due to entity render error", blockEntity.getEntityId(), t);
        } finally {
            dispatcher.setRenderShadow(true);
            while (stackDepthBefore > 0) {
                poseStack.popPose();
                stackDepthBefore--;
            }
        }
    }
}
