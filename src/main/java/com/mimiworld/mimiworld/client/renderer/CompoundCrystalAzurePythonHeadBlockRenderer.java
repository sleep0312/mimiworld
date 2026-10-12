package com.mimiworld.mimiworld.client.renderer;

import com.mimiworld.mimiworld.client.model.CompoundCrystalAzurePythonHeadBlockModel;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.level.block.entity.BlockEntity;
import software.bernie.geckolib.core.animatable.GeoAnimatable;
import software.bernie.geckolib.renderer.GeoBlockRenderer;

/**
 * 复晶天青蟒头摆件方块的 GeckoLib 渲染器。
 *
 * <p>抽取的是天青蟒实体模型的头部子树，几何坐标沿用实体坐标系：
 * 包围盒 x[-7,7]、y[34,58]、z[-37,-16]（14×24×21 像素，长吻；
 * 模型中心轴 x=0、z 中心 -26.5、底 y=34）。
 * {@code GeoBlockRenderer} 内部还会平移 (0.5,0,0.5) 并以 1/16 落像素坐标：</p>
 * <ul>
 *   <li>缩放 s=0.46：摆件约 0.40 格宽、0.69 格高、0.60 格深（吻部朝北）；</li>
 *   <li>x/z 居中、y 底贴地。</li>
 * </ul>
 */
public class CompoundCrystalAzurePythonHeadBlockRenderer<T extends BlockEntity & GeoAnimatable>
        extends GeoBlockRenderer<T> {

    /** 缩放：24px 高 → 0.69 格。 */
    private static final float SCALE = 0.46F;

    /** x 中心对齐（模型中心轴 0 + 内部 0.5 平移）。 */
    private static final float TRANSLATE_X = 0.5F - 0.5F * SCALE;
    /** y 底部贴地（模型底 y=34px）。 */
    private static final float TRANSLATE_Y = -(34.0F / 16.0F) * SCALE;
    /** z 居中（模型 z 中心 -26.5px，叠加内部 0.5 平移后回到格中心）。 */
    private static final float TRANSLATE_Z = 0.5F - (0.5F - 26.5F / 16.0F) * SCALE;

    public CompoundCrystalAzurePythonHeadBlockRenderer(BlockEntityRendererProvider.Context context) {
        super(new CompoundCrystalAzurePythonHeadBlockModel<>());
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
