package com.mimiworld.mimiworld.client.renderer;

import com.mimiworld.mimiworld.ModItems;
import com.mimiworld.mimiworld.block.MimiTeleporterBlock;
import com.mimiworld.mimiworld.block.entity.MimiTeleporterBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.state.BlockState;

/**
 * 米米传送器（mimi_teleporter）的方块实体渲染器：
 * 已插眼（{@link MimiTeleporterBlock#EYE}）时，在方块正上方 1 格渲染一只悬浮的米米之眼，
 * 随时间缓慢绕 Y 轴旋转（约 10 秒一圈）并轻微上下浮动。
 *
 * <p>激活动画（序列进度读自方块实体的 {@code seqStart}/{@code seqCenter}，由服务端同步）：</p>
 * <ol>
 *   <li>0–60 刻：转速平滑升至 3 倍（{@link #sequenceExtra} 按积分给出连续相位，不发生跳变）；</li>
 *   <li>60 刻起每 20 刻：本框架向中心泥米位置发射一束激光（发射顺序见
 *       {@link MimiTeleporterBlock#RING_OFFSETS} 下标），激光保持到生成时刻——
 *       激光为场景级渲染，见 {@link MimiLaserRenderer}（不随本方块实体被视锥剔除而消失）；</li>
 *   <li>300 刻生成：激光瞬间消失，转速在 60 刻内平滑降回原速。</li>
 * </ol>
 *
 * <p>纯客户端视觉，不生成任何实体；插眼状态读方块状态，序列进度读方块实体同步数据。</p>
 */
public class MimiTeleporterBlockRenderer implements BlockEntityRenderer<MimiTeleporterBlockEntity> {

    /** 原速：度/刻（1.8°/刻 ≈ 每 10 秒一圈）。 */
    private static final float BASE_SPIN = 1.8F;
    /** 激活期间的转速倍数（相对原速）。 */
    private static final float BOOST = 10.0F;

    private final ItemRenderer itemRenderer;

    public MimiTeleporterBlockRenderer(BlockEntityRendererProvider.Context context) {
        this.itemRenderer = context.getItemRenderer();
    }

    @Override
    public void render(MimiTeleporterBlockEntity be, float partialTick, PoseStack poseStack,
                       MultiBufferSource buffer, int packedLight, int packedOverlay) {
        Level level = be.getLevel();
        if (level == null) {
            return;
        }
        BlockPos pos = be.getBlockPos();
        BlockState state = level.getBlockState(pos);
        if (!state.hasProperty(MimiTeleporterBlock.EYE) || !state.getValue(MimiTeleporterBlock.EYE)) {
            return;
        }

        float time = level.getGameTime() + partialTick;

        // ---------- 序列状态（未同步到 / 已清除时为未激活） ----------
        boolean inSequence = be.seqStart != MimiTeleporterBlockEntity.SEQ_NONE
                && be.seqCenter != null;
        float seqT = inSequence ? time - be.seqStart : 0.0F;

        // ---------- 眼睛：悬浮在方块正上方 1 格的中心，按序列相位变速旋转 ----------
        float spinDegrees;
        if (inSequence) {
            spinDegrees = (time + sequenceExtra(seqT)) * BASE_SPIN;
        } else {
            spinDegrees = time * BASE_SPIN;
        }

        BlockPos above = pos.above();
        int light = LightTexture.pack(
                level.getBrightness(LightLayer.BLOCK, above),
                level.getBrightness(LightLayer.SKY, above));

        poseStack.pushPose();
        poseStack.translate(0.5D, 1.45D, 0.5D);
        poseStack.translate(0.0D, Mth.sin(time * 0.10F) * 0.06D, 0.0D);
        poseStack.scale(0.8F, 0.8F, 0.8F);
        poseStack.mulPose(Axis.YP.rotationDegrees(spinDegrees));
        itemRenderer.renderStatic(
                ModItems.MIMI_EYE.get().getDefaultInstance(),
                ItemDisplayContext.GROUND, light, OverlayTexture.NO_OVERLAY,
                poseStack, buffer, level, 0);
        poseStack.popPose();
    }

    /**
     * 激活序列期间叠加在时间上的相位补偿（刻），保证转速变化连续：
     * 恒速时补偿为常数增量、加速段为对 3 倍速的积分、减速段为从 3 倍速回落的积分。
     * 转角 = (time + 补偿) × 原速。
     */
    private static float sequenceExtra(float t) {
        float warm = MimiTeleporterBlock.SEQ_WARMUP;      // 60
        float form = MimiTeleporterBlock.SEQ_FORM_AT;     // 300
        float down = MimiTeleporterBlock.SEQ_SPIN_DOWN;   // 60
        if (t <= 0.0F) {
            return 0.0F;
        }
        if (t <= warm) {
            // 升速段：速度 1→3，smoothstep 插值，∫(speed-1)dt
            float p = t / warm;
            return (BOOST - 1.0F) * warm * (p * p * p - 0.5F * p * p * p * p);
        }
        if (t <= form) {
            // 匀速段：恒为 3 倍速
            return (BOOST - 1.0F) * warm + (BOOST - 1.0F) * (t - warm);
        }
        // 降速段（生成后 3 秒）：3→1，smoothstep 回落，之后保持常数（=原速）
        float q = Mth.clamp((t - form) / down, 0.0F, 1.0F);
        float downIntegral = (BOOST - 1.0F) * q * down
                - (BOOST - 1.0F) * down * (q * q * q - 0.5F * q * q * q * q);
        return (BOOST - 1.0F) * warm + (BOOST - 1.0F) * (form - warm) + downIntegral;
    }
}
