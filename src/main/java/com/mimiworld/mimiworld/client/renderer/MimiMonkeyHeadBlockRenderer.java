package com.mimiworld.mimiworld.client.renderer;

import com.mimiworld.mimiworld.client.model.MimiMonkeyHeadBlockModel;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.level.block.entity.BlockEntity;
import software.bernie.geckolib.core.animatable.GeoAnimatable;
import software.bernie.geckolib.renderer.GeoBlockRenderer;

/**
 * 米米猴头摆件方块的 GeckoLib 渲染器。
 *
 * <p>抽取的是猴王实体模型的头部子树，几何坐标仍沿用实体坐标系（像素原点在脚底、
 * 模型中心轴 x=0）：几何包围盒 x[-15.4,15.4]、y[19.9,37.26]、z[-5.85,4]，
 * 直接渲染会又高又偏。这里在 super 渲染前做一次缩放+平移（{@code GeoBlockRenderer}
 * 内部还会再平移 (0.5,0,0.5) 且以 1/16 比例落像素坐标，需一并计入）：</p>
 * <ul>
 *   <li>以宽度铺满两格反算缩放 s = 32/30.8 ≈ 1.04（双雉鸡翎尖端恰好到两格边），
 *       缩放后约 2 格宽、1.12 格高；</li>
 *   <li>x 模型中心轴 0 → 格中心；z 中心 -0.925px → 格中心；
 *       y 底 19.9px → 贴地（y=0）。</li>
 * </ul>
 */
public class MimiMonkeyHeadBlockRenderer<T extends BlockEntity & GeoAnimatable> extends GeoBlockRenderer<T> {

    /** 以双雉鸡翎展开的模型宽度（30.8px）铺满两格反算。 */
    private static final float SCALE = 32.0F / 30.8F;

    /** x 中心对齐：0.5 - s·0.5（内部 0.5 平移）。 */
    private static final float TRANSLATE_X = 0.5F - 0.5F * SCALE;
    /** y 底部贴地：模型底在 y=19.9px，内部无 y 偏移。 */
    private static final float TRANSLATE_Y = -(19.9F / 16.0F) * SCALE;
    /** z 中心对齐：模型 z 中心为 -0.925px，叠加内部 0.5 平移后回到格中心。 */
    private static final float TRANSLATE_Z = 0.5F - (0.5F - 0.925F / 16.0F) * SCALE;

    public MimiMonkeyHeadBlockRenderer(BlockEntityRendererProvider.Context context) {
        super(new MimiMonkeyHeadBlockModel<>());
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
