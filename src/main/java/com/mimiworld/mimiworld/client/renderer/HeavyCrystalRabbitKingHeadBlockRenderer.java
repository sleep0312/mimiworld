package com.mimiworld.mimiworld.client.renderer;

import com.mimiworld.mimiworld.client.model.HeavyCrystalRabbitKingHeadBlockModel;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.level.block.entity.BlockEntity;
import software.bernie.geckolib.core.animatable.GeoAnimatable;
import software.bernie.geckolib.renderer.GeoBlockRenderer;

/**
 * 重晶兔王头摆件方块的 GeckoLib 渲染器。
 *
 * <p>抽取的是兔王实体模型的头部子树，几何坐标沿用实体坐标系：
 * 包围盒 x[-6,6]、y[29,54]、z[-16.5,-5]（12×25×11.5 像素，含长耳；
 * 模型中心轴 x=0、z 中心 -10.75、底 y=29）。
 * {@code GeoBlockRenderer} 内部还会平移 (0.5,0,0.5) 并以 1/16 落像素坐标：</p>
 * <ul>
 *   <li>缩放 s=0.90：摆件约 0.68 格宽、1.40 格高（耳尖）、0.64 格深（吻部朝北）；</li>
 *   <li>x/z 居中、y 底贴地。</li>
 * </ul>
 */
public class HeavyCrystalRabbitKingHeadBlockRenderer<T extends BlockEntity & GeoAnimatable>
        extends GeoBlockRenderer<T> {

    /** 缩放：25px 高（含长耳）→ 1.40 格。 */
    private static final float SCALE = 0.90F;

    /** x 中心对齐（模型中心轴 0 + 内部 0.5 平移）。 */
    private static final float TRANSLATE_X = 0.5F - 0.5F * SCALE;
    /** y 底部贴地（模型底 y=29px）。 */
    private static final float TRANSLATE_Y = -(29.0F / 16.0F) * SCALE;
    /** z 居中（模型 z 中心 -10.75px，叠加内部 0.5 平移后回到格中心）。 */
    private static final float TRANSLATE_Z = 0.5F - (0.5F - 10.75F / 16.0F) * SCALE;

    public HeavyCrystalRabbitKingHeadBlockRenderer(BlockEntityRendererProvider.Context context) {
        super(new HeavyCrystalRabbitKingHeadBlockModel<>());
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
