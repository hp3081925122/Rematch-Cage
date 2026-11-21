package com.hp.rematchcage.client;

import com.hp.rematchcage.Rematchcage;
import com.hp.rematchcage.block.RematchCageBlockEntity;
import com.hp.rematchcage.config.RematchCageConfig;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import java.util.OptionalDouble;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Rematchcage.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public final class RematchCageGlowRenderer {
    private RematchCageGlowRenderer() {
    }

    @SubscribeEvent
    public static void onRenderLevelStage(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_WEATHER) {
            return;
        }
        if (!RematchCageConfig.CLIENT_SPEC.isLoaded() || !RematchCageConfig.CLIENT.xrayGlowOutline.get()) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        Level level = mc.level;
        if (level == null) {
            return;
        }
        double maxRange = RematchCageConfig.CLIENT.xrayGlowRange.get();
        if (maxRange <= 0.0D) {
            return;
        }
        double maxDistSq = maxRange * maxRange;

        Camera camera = event.getCamera();
        Vec3 camPos = camera.getPosition();
        PoseStack poseStack = event.getPoseStack();
        MultiBufferSource.BufferSource bufferSource = mc.renderBuffers().bufferSource();

        float time = level.getGameTime() + event.getPartialTick();
        float pulse = 0.72F + 0.28F * Mth.sin(time * 0.12F);
        boolean renderedAny = false;

        poseStack.pushPose();
        poseStack.translate(-camPos.x, -camPos.y, -camPos.z);
        VertexConsumer consumer = bufferSource.getBuffer(XrayRenderType.XRAY_OUTLINE);

        for (RematchCageBlockEntity cage : RematchCageBlockEntity.getClientCages()) {
            if (cage.isRemoved() || cage.getLevel() != level) {
                continue;
            }
            BlockPos pos = cage.getBlockPos();
            double cx = pos.getX() + 0.5D;
            double cy = pos.getY() + 0.5D;
            double cz = pos.getZ() + 0.5D;
            if (camPos.distanceToSqr(cx, cy, cz) > maxDistSq) {
                continue;
            }
            renderedAny = true;
            LevelRenderer.renderLineBox(
                poseStack,
                consumer,
                pos.getX() + 0.01D,
                pos.getY() + 0.01D,
                pos.getZ() + 0.01D,
                pos.getX() + 0.99D,
                pos.getY() + 0.99D,
                pos.getZ() + 0.99D,
                0.35F,
                0.95F,
                1.0F,
                0.90F * pulse
            );
            LevelRenderer.renderLineBox(
                poseStack,
                consumer,
                pos.getX() + 0.14D,
                pos.getY() + 0.14D,
                pos.getZ() + 0.14D,
                pos.getX() + 0.86D,
                pos.getY() + 0.86D,
                pos.getZ() + 0.86D,
                1.0F,
                0.82F,
                0.32F,
                0.55F * pulse
            );
        }

        poseStack.popPose();
        if (renderedAny) {
            bufferSource.endBatch(XrayRenderType.XRAY_OUTLINE);
        }
    }

    private static final class XrayRenderType extends RenderType {
        private static final RenderType XRAY_OUTLINE = RenderType.create(
            "rematchcage_xray_outline",
            DefaultVertexFormat.POSITION_COLOR_NORMAL,
            VertexFormat.Mode.LINES,
            256,
            false,
            false,
            RenderType.CompositeState.builder()
                .setShaderState(RENDERTYPE_LINES_SHADER)
                .setLineState(new LineStateShard(OptionalDouble.of(3.0D)))
                .setLayeringState(VIEW_OFFSET_Z_LAYERING)
                .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
                .setOutputState(ITEM_ENTITY_TARGET)
                .setWriteMaskState(COLOR_WRITE)
                .setCullState(NO_CULL)
                .setDepthTestState(NO_DEPTH_TEST)
                .createCompositeState(false)
        );

        private XrayRenderType(String name, VertexFormat format, VertexFormat.Mode mode, int bufferSize, boolean affectsCrumbling, boolean sortOnUpload, Runnable setupState, Runnable clearState) {
            super(name, format, mode, bufferSize, affectsCrumbling, sortOnUpload, setupState, clearState);
        }
    }
}
