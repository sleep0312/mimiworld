package com.mimiworld.mimiworld.client.renderer;

import com.mimiworld.mimiworld.client.model.DibazongzuiHeadBlockModel;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.level.block.entity.BlockEntity;
import software.bernie.geckolib.core.animatable.GeoAnimatable;
import software.bernie.geckolib.renderer.GeoBlockRenderer;

/**
 * 第八宗罪龙头摆件方块的 GeckoLib 渲染器。
 *
 * <p>抽取的是第八宗罪龙实体模型的头部子树（head+jaw），几何坐标沿用实体坐标系：
 * 包围盒 x[-36,36]、y[106.8,204]、z[-351,-201]（72×97.2×150 像素，1.5 倍终末龙体型的巨型龙头；
 * 模型中心轴 x=0、z 中心 -276、底 y=106.8）。
 * {@code GeoBlockRenderer} 内部还会平移 (0.5,0,0.5) 并以 1/16 落像素坐标：</p>
 * <ul>
 *   <li>缩放 s=16/150：摆件约 0.48 格宽、0.65 格高、1.0 格深（长吻朝北占满一格）；</li>
 *   <li>x/z 居中、y 底贴地。</li>
 * </ul>
 */
public class DibazongzuiHeadBlockRenderer<T extends BlockEntity & GeoAnimatable>
        extends GeoBlockRenderer<T> {

    /** 缩放：150px 深的龙头 → 深 1.0 格。 */
    private static final float SCALE = 16.0F / 150.0F;

    /** x 中心对齐（模型中心轴 0 + 内部 0.5 平移）。 */
    private static final float TRANSLATE_X = 0.5F - 0.5F * SCALE;
    /** y 底部贴地（模型底 y=106.8px）。 */
    private static final float TRANSLATE_Y = -(106.8F / 16.0F) * SCALE;
    /** z 居中（模型 z 中心 -276px，叠加内部 0.5 平移后回到格中心）。 */
    private static final float TRANSLATE_Z = 0.5F - (0.5F - 276.0F / 16.0F) * SCALE;

    public DibazongzuiHeadBlockRenderer(BlockEntityRendererProvider.Context context) {
        super(new DibazongzuiHeadBlockModel<>());
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
