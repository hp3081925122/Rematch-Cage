package com.hp.rematchcage.client;

import com.hp.rematchcage.block.RematchCageBlockEntity;
import com.hp.rematchcage.config.RematchCageConfig;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.logging.LogUtils;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

public class RematchCageRenderer implements BlockEntityRenderer<RematchCageBlockEntity, RematchCageRenderer.State> {
    private static final Logger LOGGER = LogUtils.getLogger();

    public static final class State extends BlockEntityRenderState {
        @Nullable
        EntityRenderState entityState;
        float yaw;
        double bobOffset;
        float scale;
    }

    public RematchCageRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(
        RematchCageBlockEntity blockEntity,
        State state,
        float partialTick,
        Vec3 cameraPosition,
        @Nullable ModelFeatureRenderer.CrumblingOverlay breakProgress
    ) {
        BlockEntityRenderState.extractBase(blockEntity, state, breakProgress);
        state.entityState = null;
        state.yaw = 0.0F;
        state.bobOffset = 0.0D;
        state.scale = 0.0F;

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

        float time = level.getGameTime() + partialTick;
        float spinSpeed = RematchCageConfig.CLIENT.spinDegreesPerTick.get().floatValue();
        float bobAmp = RematchCageConfig.CLIENT.bobAmplitude.get().floatValue();
        state.yaw = (time * spinSpeed) % 360.0F;
        state.bobOffset = Mth.sin(time * 0.1F) * bobAmp;

        float bbWidth = Math.max(0.25F, displayEntity.getBbWidth());
        float bbHeight = Math.max(0.25F, displayEntity.getBbHeight());
        float baseScale = RematchCageConfig.CLIENT.baseEntityScale.get().floatValue();
        float maxDim = Math.max(bbWidth, bbHeight);
        float scale = maxDim > 1.0F ? (baseScale / maxDim) : baseScale;
        state.scale = Math.min(scale, Math.min(0.65F / bbWidth, 0.65F / bbHeight));

        try {
            EntityRenderState extracted = Minecraft.getInstance().getEntityRenderDispatcher().extractEntity(displayEntity, 0.0F);
            int blockLight = Math.max((state.lightCoords >> 4) & 0xF, 12);
            int skyLight = (state.lightCoords >> 20) & 0xF;
            extracted.lightCoords = (skyLight << 20) | (blockLight << 4);
            extracted.shadowRadius = 0.0F;
            extracted.shadowPieces.clear();
            state.entityState = extracted;
        } catch (Throwable t) {
            blockEntity.markDisplayRenderFailed();
            LOGGER.warn("[RematchCage] Disabled miniature rendering for {} due to entity extract error", blockEntity.getEntityId(), t);
        }
    }

    @Override
    public void submit(State state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        if (state.entityState == null) {
            return;
        }
        poseStack.pushPose();
        try {
            poseStack.translate(0.5D, 0.22D + state.bobOffset, 0.5D);
            poseStack.mulPose(Axis.YP.rotationDegrees(state.yaw));
            poseStack.scale(state.scale, state.scale, state.scale);
            Minecraft.getInstance().getEntityRenderDispatcher().submit(state.entityState, camera, 0.0D, 0.0D, 0.0D, poseStack, collector);
        } finally {
            poseStack.popPose();
        }
    }
}
