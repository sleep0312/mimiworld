package com.mimiworld.mimiworld.client.renderer;

import com.mimiworld.mimiworld.client.model.SuperCrystalOxHeadBlockModel;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.level.block.entity.BlockEntity;
import software.bernie.geckolib.core.animatable.GeoAnimatable;
import software.bernie.geckolib.renderer.GeoBlockRenderer;

/**
 * 超晶牛头摆件方块的 GeckoLib 渲染器。
 *
 * <p>抽取的是牛王实体模型的头部子树，几何坐标沿用实体坐标系：
 * 包围盒 x[-11,11]、y[24,40]、z[-13,3]（22×16×16 像素，模型中心轴 x=0、z 中心 -5、底 y=24）。
 * {@code GeoBlockRenderer} 内部还会平移 (0.5,0,0.5) 并以 1/16 落像素坐标：</p>
 * <ul>
 *   <li>缩放 s=0.72：摆件约 0.99 格宽（双角展）、0.72 格高、0.72 格深（吻部朝北）；</li>
 *   <li>x/z 居中、y 底贴地。</li>
 * </ul>
 */
public class SuperCrystalOxHeadBlockRenderer<T extends BlockEntity & GeoAnimatable>
        extends GeoBlockRenderer<T> {

    /** 缩放：16px 高 → 0.72 格。 */
    private static final float SCALE = 0.72F;

    /** x 中心对齐（模型中心轴 0 + 内部 0.5 平移）。 */
    private static final float TRANSLATE_X = 0.5F - 0.5F * SCALE;
    /** y 底部贴地（模型底 y=24px）。 */
    private static final float TRANSLATE_Y = -(24.0F / 16.0F) * SCALE;
    /** z 居中（模型 z 中心 -5px，叠加内部 0.5 平移后回到格中心）。 */
    private static final float TRANSLATE_Z = 0.5F - (0.5F - 5.0F / 16.0F) * SCALE;

    public SuperCrystalOxHeadBlockRenderer(BlockEntityRendererProvider.Context context) {
        super(new SuperCrystalOxHeadBlockModel<>());
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
