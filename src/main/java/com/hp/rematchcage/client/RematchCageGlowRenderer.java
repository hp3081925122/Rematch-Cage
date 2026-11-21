package com.hp.rematchcage.client;

import com.hp.rematchcage.Rematchcage;
import com.hp.rematchcage.block.RematchCageBlockEntity;
import com.hp.rematchcage.config.RematchCageConfig;
import com.mojang.blaze3d.pipeline.DepthStencilState;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.platform.CompareOp;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.rendertype.OutputTarget;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;

@EventBusSubscriber(modid = Rematchcage.MOD_ID, value = Dist.CLIENT)
public final class RematchCageGlowRenderer {
    private static final RenderPipeline XRAY_PIPELINE = RenderPipeline.builder(RenderPipelines.LINES_SNIPPET)
        .withLocation(Rematchcage.id("pipeline/xray_outline"))
        .withDepthStencilState(new DepthStencilState(CompareOp.ALWAYS_PASS, false))
        .withCull(false)
        .build();

    private static final RenderType XRAY_OUTLINE = RenderType.create(
        "rematchcage_xray_outline",
        RenderSetup.builder(XRAY_PIPELINE)
            .setOutputTarget(OutputTarget.ITEM_ENTITY_TARGET)
            .createRenderSetup()
    );

    private RematchCageGlowRenderer() {
    }

    @SubscribeEvent
    public static void onRenderLevelStage(RenderLevelStageEvent.AfterWeather event) {
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

        Vec3 camPos = event.getLevelRenderState().cameraRenderState.pos;
        PoseStack poseStack = event.getPoseStack();
        MultiBufferSource.BufferSource bufferSource = mc.renderBuffers().bufferSource();

        float partialTick = mc.getDeltaTracker().getGameTimeDeltaPartialTick(false);
        float time = level.getGameTime() + partialTick;
        float pulse = 0.72F + 0.28F * Mth.sin(time * 0.12F);
        boolean renderedAny = false;

        poseStack.pushPose();
        poseStack.translate(-camPos.x, -camPos.y, -camPos.z);
        VertexConsumer consumer = bufferSource.getBuffer(XRAY_OUTLINE);

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
            drawBox(
                poseStack.last(),
                consumer,
                pos.getX() + 0.01F,
                pos.getY() + 0.01F,
                pos.getZ() + 0.01F,
                pos.getX() + 0.99F,
                pos.getY() + 0.99F,
                pos.getZ() + 0.99F,
                0.35F,
                0.95F,
                1.0F,
                0.90F * pulse,
                3.0F
            );
            drawBox(
                poseStack.last(),
                consumer,
                pos.getX() + 0.14F,
                pos.getY() + 0.14F,
                pos.getZ() + 0.14F,
                pos.getX() + 0.86F,
                pos.getY() + 0.86F,
                pos.getZ() + 0.86F,
                1.0F,
                0.82F,
                0.32F,
                0.55F * pulse,
                2.0F
            );
        }

        poseStack.popPose();
        if (renderedAny) {
            bufferSource.endBatch(XRAY_OUTLINE);
        }
    }

    private static void drawBox(
        PoseStack.Pose pose,
        VertexConsumer consumer,
        float x0,
        float y0,
        float z0,
        float x1,
        float y1,
        float z1,
        float r,
        float g,
        float b,
        float a,
        float lineWidth
    ) {
        line(pose, consumer, x0, y0, z0, x1, y0, z0, 1, 0, 0, r, g, b, a, lineWidth);
        line(pose, consumer, x0, y0, z0, x0, y1, z0, 0, 1, 0, r, g, b, a, lineWidth);
        line(pose, consumer, x0, y0, z0, x0, y0, z1, 0, 0, 1, r, g, b, a, lineWidth);
        line(pose, consumer, x1, y0, z0, x1, y1, z0, 0, 1, 0, r, g, b, a, lineWidth);
        line(pose, consumer, x1, y1, z0, x0, y1, z0, -1, 0, 0, r, g, b, a, lineWidth);
        line(pose, consumer, x0, y1, z0, x0, y1, z1, 0, 0, 1, r, g, b, a, lineWidth);
        line(pose, consumer, x0, y1, z1, x0, y0, z1, 0, -1, 0, r, g, b, a, lineWidth);
        line(pose, consumer, x0, y0, z1, x1, y0, z1, 1, 0, 0, r, g, b, a, lineWidth);
        line(pose, consumer, x1, y0, z1, x1, y0, z0, 0, 0, -1, r, g, b, a, lineWidth);
        line(pose, consumer, x0, y1, z1, x1, y1, z1, 1, 0, 0, r, g, b, a, lineWidth);
        line(pose, consumer, x1, y0, z1, x1, y1, z1, 0, 1, 0, r, g, b, a, lineWidth);
        line(pose, consumer, x1, y1, z0, x1, y1, z1, 0, 0, 1, r, g, b, a, lineWidth);
    }

    private static void line(
        PoseStack.Pose pose,
        VertexConsumer consumer,
        float x0,
        float y0,
        float z0,
        float x1,
        float y1,
        float z1,
        float nx,
        float ny,
        float nz,
        float r,
        float g,
        float b,
        float a,
        float lineWidth
    ) {
        consumer.addVertex(pose, x0, y0, z0).setColor(r, g, b, a).setNormal(pose, nx, ny, nz).setLineWidth(lineWidth);
        consumer.addVertex(pose, x1, y1, z1).setColor(r, g, b, a).setNormal(pose, nx, ny, nz).setLineWidth(lineWidth);
    }
}
