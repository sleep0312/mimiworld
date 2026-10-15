package com.mimiworld.mimiworld.client.renderer;

import com.mimiworld.mimiworld.client.model.FallenCrossBlockModel;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.level.block.entity.BlockEntity;
import software.bernie.geckolib.core.animatable.GeoAnimatable;
import software.bernie.geckolib.renderer.GeoBlockRenderer;

/**
 * 堕落十字架方块的 GeckoLib 渲染器。
 *
 * <p>几何为标准方块坐标系（中心轴 x=z=8，y 从 0 起），包围盒
 * x[2,14]、y[0,27]、z[5,11]，整座高 27px 超过一格。这里在 super 渲染前
 * 做一次等比缩放+平移（{@code GeoBlockRenderer} 内部还会再平移 (0.5,0,0.5)
 * 且以 1/16 比例落像素坐标，需一并计入）：</p>
 * <ul>
 *   <li>以模型总高 27px 恰好收进一格反算缩放 s = 16/27 ≈ 0.593，
 *       缩放后满一格高、宽约 0.44 格、厚约 0.22 格；</li>
 *   <li>x、z 模型中心轴 8 → 格中心：平移 0.5 - s（叠加内部 0.5 平移）；</li>
 *   <li>y 模型底在 0 → 贴地（y=0），无需 y 偏移。</li>
 * </ul>
 */
public class FallenCrossBlockRenderer<T extends BlockEntity & GeoAnimatable> extends GeoBlockRenderer<T> {

    /** 以模型总高（27px）收进一格反算。 */
    private static final float SCALE = 16.0F / 27.0F;

    /** x 中心对齐：0.5 - s（模型中心轴在 x=8，叠加内部 0.5 平移后回到格中心）。 */
    private static final float TRANSLATE_X = 0.5F - SCALE;
    /** y 底部贴地：模型底在 y=0。 */
    private static final float TRANSLATE_Y = 0.0F;
    /** z 中心对齐：同 x。 */
    private static final float TRANSLATE_Z = 0.5F - SCALE;

    public FallenCrossBlockRenderer(BlockEntityRendererProvider.Context context) {
        super(new FallenCrossBlockModel<>());
    }

    @Override
    public void render(BlockEntity blockEntity, float partialTick, PoseStack poseStack,
                       MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        poseStack.pushPose();
        poseStack.translate(TRANSLATE_X, TRANSLATE_Y, TRANSLATE_Z);
        poseStack.scale(SCALE, SCALE, SCALE);
        super.render(blockEntity, partialTick, poseStack, bufferSource, packedLight, packedOverlay);
        poseStack.popPose();
    }
}
