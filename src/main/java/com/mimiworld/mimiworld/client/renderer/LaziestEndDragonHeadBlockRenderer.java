package com.mimiworld.mimiworld.client.renderer;

import com.mimiworld.mimiworld.client.model.LaziestEndDragonHeadBlockModel;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.level.block.entity.BlockEntity;
import software.bernie.geckolib.core.animatable.GeoAnimatable;
import software.bernie.geckolib.renderer.GeoBlockRenderer;

/**
 * 最懒终末龙头摆件方块的 GeckoLib 渲染器。
 *
 * <p>抽取的是终末龙（Promax）实体模型的头部子树，几何坐标沿用实体坐标系：
 * 包围盒 x[-14,14]、y[82,136]、z[-234,-134]（28×54×100 像素，极长的龙头；
 * 模型中心轴 x=0、z 中心 -184、底 y=82）。
 * {@code GeoBlockRenderer} 内部还会平移 (0.5,0,0.5) 并以 1/16 落像素坐标：</p>
 * <ul>
 *   <li>缩放 s=0.16：摆件约 0.28 格宽、0.54 格高、1.0 格深（长吻朝北占满一格）；</li>
 *   <li>x/z 居中、y 底贴地。</li>
 * </ul>
 */
public class LaziestEndDragonHeadBlockRenderer<T extends BlockEntity & GeoAnimatable>
        extends GeoBlockRenderer<T> {

    /** 缩放：100px 深的龙头 → 深 1.0 格。 */
    private static final float SCALE = 0.16F;

    /** x 中心对齐（模型中心轴 0 + 内部 0.5 平移）。 */
    private static final float TRANSLATE_X = 0.5F - 0.5F * SCALE;
    /** y 底部贴地（模型底 y=82px）。 */
    private static final float TRANSLATE_Y = -(82.0F / 16.0F) * SCALE;
    /** z 居中（模型 z 中心 -184px，叠加内部 0.5 平移后回到格中心）。 */
    private static final float TRANSLATE_Z = 0.5F - (0.5F - 184.0F / 16.0F) * SCALE;

    public LaziestEndDragonHeadBlockRenderer(BlockEntityRendererProvider.Context context) {
        super(new LaziestEndDragonHeadBlockModel<>());
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
