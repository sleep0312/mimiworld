package com.mimiworld.mimiworld.client.renderer;

import com.mimiworld.mimiworld.MimiWorld;
import com.mimiworld.mimiworld.block.MimiTeleporterBlock;
import com.mimiworld.mimiworld.block.entity.MimiTeleporterBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
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
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 米米传送门激活动画的激光——场景级渲染（挂在 {@link RenderLevelStageEvent} 上）。
 *
 * <p>为什么不画在框架方块实体渲染器里：方块实体按其渲染包围盒做视锥剔除，
 * 框架一转出视线其激光就整组消失，而激光本体（框架 → 中心）明明还在视野内。
 * 本类改为每帧场景阶段主动画出全部进行中序列的 12 束激光，与方块实体可见性无关。</p>
 *
 * <p>序列发现：客户端每次收到框架方块实体数据（区块初载 / 服务端同步）都会经
 * {@link #update} 登记「中心 → 起始刻」；每帧渲染时用锚点框架的方块实体校验序列仍然有效，
 * 已被清除（框架破坏等）则移除登记。</p>
 */
@Mod.EventBusSubscriber(modid = MimiWorld.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public final class MimiLaserRenderer {

    /** 进行中的激活序列：传送门中心 → 序列起始游戏刻。 */
    private static final Map<BlockPos, Long> ACTIVE = new ConcurrentHashMap<>();

    private MimiLaserRenderer() {
    }

    /**
     * 客户端收到框架方块实体数据时登记/注销序列（由 MimiTeleporterBlockEntity#load 调用）。
     * 序列有效 → 以中心为键登记；已清除 → 反推该框架所属的候选中心并移除（同服务端 clearSequence）。
     */
    public static void update(BlockPos framePos, long seqStart, @Nullable BlockPos seqCenter) {
        if (seqStart != MimiTeleporterBlockEntity.SEQ_NONE && seqCenter != null) {
            ACTIVE.put(seqCenter, seqStart);
            return;
        }
        ACTIVE.entrySet().removeIf(entry -> belongsTo(entry.getKey(), framePos));
    }

    /** center 的环上是否含 framePos。 */
    private static boolean belongsTo(BlockPos center, BlockPos framePos) {
        for (int[] offset : MimiTeleporterBlock.RING_OFFSETS) {
            if (center.offset(offset[0], 0, offset[1]).equals(framePos)) {
                return true;
            }
        }
        return false;
    }

    @SubscribeEvent
    public static void onRenderLevelStage(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_BLOCK_ENTITIES || ACTIVE.isEmpty()) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        Level level = mc.level;
        if (level == null) {
            return;
        }
        float time = level.getGameTime() + event.getPartialTick();

        for (BlockPos center : new ArrayList<>(ACTIVE.keySet())) {
            Long start = ACTIVE.get(center);
            if (start == null) {
                continue;
            }
            // 用锚点框架校验序列仍有效（框架被破坏 → 已同步清除 → 移除登记）
            BlockPos anchor = center.offset(MimiTeleporterBlock.RING_OFFSETS[0][0], 0,
                    MimiTeleporterBlock.RING_OFFSETS[0][1]);
            if (!level.hasChunkAt(anchor)) {
                continue; // 区块未加载，暂无法校验，保留登记
            }
            if (!(level.getBlockEntity(anchor) instanceof MimiTeleporterBlockEntity tele)
                    || tele.seqStart != start
                    || !center.equals(tele.seqCenter)) {
                ACTIVE.remove(center);
                continue;
            }
            float seqT = time - start;
            if (seqT >= MimiTeleporterBlock.SEQ_WARMUP && seqT < MimiTeleporterBlock.SEQ_FORM_AT) {
                renderBeams(event, center, time, seqT);
            }
        }
    }

    /** 画出该序列此刻已发射且尚未生成的全部激光（世界坐标，转为相机相对）。 */
    private static void renderBeams(RenderLevelStageEvent event, BlockPos center, float time, float seqT) {
        Camera camera = event.getCamera();
        Vec3 camPos = camera.getPosition();
        MultiBufferSource.BufferSource buffers = Minecraft.getInstance().renderBuffers().bufferSource();

        PoseStack poseStack = event.getPoseStack();
        poseStack.pushPose();
        poseStack.translate(-camPos.x, -camPos.y, -camPos.z);

        double bob = Mth.sin(time * 0.10F) * 0.06D; // 与悬浮眼睛的浮动同步
        for (int i = 0; i < MimiTeleporterBlock.RING_OFFSETS.length; i++) {
            float fireAt = MimiTeleporterBlock.SEQ_WARMUP + MimiTeleporterBlock.SEQ_INTERVAL * i;
            if (seqT < fireAt) {
                continue;
            }
            int[] offset = MimiTeleporterBlock.RING_OFFSETS[i];
            BlockPos frame = center.offset(offset[0], 0, offset[1]);
            Vec3 start = new Vec3(frame.getX() + 0.5D, frame.getY() + 1.45D + bob, frame.getZ() + 0.5D);
            Vec3 end = new Vec3(center.getX() + 0.5D, center.getY() + 2.5D, center.getZ() + 0.5D);
            renderLaser(poseStack, buffers, seqT - fireAt, start, end);
        }
        poseStack.popPose();
    }

    /**
     * 画一束激光：起点（眼睛悬浮处）到终点（泥米位置）的两组正交薄面片，
     * 外层青色辉光 + 内层亮芯；发射瞬间闪亮（flash），随后轻微脉动。
     */
    private static void renderLaser(PoseStack poseStack, MultiBufferSource buffer,
                                    float age, Vec3 start, Vec3 end) {
        Vec3 axis = end.subtract(start);
        double length = axis.length();
        if (length < 1.0E-4) {
            return;
        }
        axis = axis.scale(1.0D / length);
        Vec3 reference = Math.abs(axis.y) > 0.99D ? new Vec3(1.0D, 0.0D, 0.0D)
                : new Vec3(0.0D, 1.0D, 0.0D);
        Vec3 side1 = axis.cross(reference).normalize();
        Vec3 side2 = axis.cross(side1).normalize();

        float flash = Mth.clamp(1.0F - age / 6.0F, 0.0F, 1.0F);
        float pulse = 0.70F + 0.30F * Mth.sin(age * 0.8F);

        // 外层辉光（青色）
        float outerWidth = 0.07F;
        int outerAlpha = (int) (255.0F * Mth.clamp(0.30F + 0.35F * pulse + 0.30F * flash, 0.0F, 1.0F));
        emitBeam(buffer.getBuffer(RenderType.lightning()), poseStack, start, end,
                side1.scale(outerWidth), side2.scale(outerWidth), 79, 243, 220, outerAlpha);

        // 内层亮芯
        float coreWidth = 0.03F;
        int coreAlpha = (int) (255.0F * Mth.clamp(0.55F + 0.25F * pulse + 0.20F * flash, 0.0F, 1.0F));
        emitBeam(buffer.getBuffer(RenderType.lightning()), poseStack, start, end,
                side1.scale(coreWidth), side2.scale(coreWidth), 216, 255, 250, coreAlpha);
    }

    /** 以 start/end 为轴，用两组正交半宽向量展开成两个相交矩形面（QUADS，各 4 顶点）。 */
    private static void emitBeam(VertexConsumer consumer, PoseStack poseStack,
                                 Vec3 start, Vec3 end, Vec3 half1, Vec3 half2,
                                 int r, int g, int b, int a) {
        quad(consumer, poseStack, start.add(half1), start.add(half2),
                end.add(half2), end.add(half1), r, g, b, a);
        quad(consumer, poseStack, start.subtract(half2), start.subtract(half1),
                end.subtract(half1), end.subtract(half2), r, g, b, a);
    }

    private static void quad(VertexConsumer consumer, PoseStack poseStack,
                             Vec3 v0, Vec3 v1, Vec3 v2, Vec3 v3,
                             int r, int g, int b, int a) {
        var pose = poseStack.last().pose();
        consumer.vertex(pose, (float) v0.x, (float) v0.y, (float) v0.z).color(r, g, b, a).endVertex();
        consumer.vertex(pose, (float) v1.x, (float) v1.y, (float) v1.z).color(r, g, b, a).endVertex();
        consumer.vertex(pose, (float) v2.x, (float) v2.y, (float) v2.z).color(r, g, b, a).endVertex();
        consumer.vertex(pose, (float) v3.x, (float) v3.y, (float) v3.z).color(r, g, b, a).endVertex();
        // RenderType.lightning() 默认开启面剔除（CULL），单面片只从正面可见；
        // 反向再画一组顶点使面片双面可见，保证任意视角都能同时看到全部激光
        consumer.vertex(pose, (float) v3.x, (float) v3.y, (float) v3.z).color(r, g, b, a).endVertex();
        consumer.vertex(pose, (float) v2.x, (float) v2.y, (float) v2.z).color(r, g, b, a).endVertex();
        consumer.vertex(pose, (float) v1.x, (float) v1.y, (float) v1.z).color(r, g, b, a).endVertex();
        consumer.vertex(pose, (float) v0.x, (float) v0.y, (float) v0.z).color(r, g, b, a).endVertex();
    }
}
