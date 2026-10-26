package com.mimiworld.mimiworld.client.renderer;

import com.mimiworld.mimiworld.ModItems;
import com.mimiworld.mimiworld.block.MimiTeleporterBlock;
import com.mimiworld.mimiworld.block.entity.MimiTeleporterBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.LightLayer;
import net.minecraft.util.Mth;

/**
 * 米米传送器（mimi_teleporter）的方块实体渲染器：
 * 已插眼（{@link MimiTeleporterBlock#EYE}）时，在方块正上方 1 格渲染一只悬浮的米米之眼，
 * 随时间缓慢绕 Y 轴旋转（约 10 秒一圈）并轻微上下浮动。
 *
 * <p>纯客户端视觉，不生成任何实体；是否插眼读取方块状态，随方块状态同步与存档。</p>
 */
public class MimiTeleporterBlockRenderer implements BlockEntityRenderer<MimiTeleporterBlockEntity> {

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

        // 眼睛悬浮在方块正上方 1 格的中心：缓慢旋转 + 轻微浮动
        float time = level.getGameTime() + partialTick;
        BlockPos above = pos.above();
        int light = LightTexture.pack(
                level.getBrightness(LightLayer.BLOCK, above),
                level.getBrightness(LightLayer.SKY, above));

        poseStack.pushPose();
        poseStack.translate(0.5D, 1.45D, 0.5D);
        poseStack.translate(0.0D, Mth.sin(time * 0.10F) * 0.06D, 0.0D);
        poseStack.scale(0.8F, 0.8F, 0.8F);
        poseStack.mulPose(Axis.YP.rotationDegrees(time * 1.8F));
        itemRenderer.renderStatic(
                ModItems.MIMI_EYE.get().getDefaultInstance(),
                ItemDisplayContext.GROUND, light, OverlayTexture.NO_OVERLAY,
                poseStack, buffer, level, 0);
        poseStack.popPose();
    }
}
