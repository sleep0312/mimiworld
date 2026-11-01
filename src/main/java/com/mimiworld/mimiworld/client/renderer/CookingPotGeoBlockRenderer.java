package com.mimiworld.mimiworld.client.renderer;

import com.mimiworld.mimiworld.client.model.CookingPotGeoModel;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntity;
import software.bernie.geckolib.core.animatable.GeoAnimatable;
import software.bernie.geckolib.renderer.GeoBlockRenderer;

/**
 * 煮锅系列方块的 GeckoLib 渲染器（五种煮锅共用一个泛型类）。
 *
 * <p>资源路径由 {@link CookingPotGeoModel} 精确指定：
 * geo/block/&lt;id&gt;.geo.json、textures/geckolib/&lt;id&gt;.png、空动画静态渲染。</p>
 * 在 {@link ClientSetup} 的 EntityRenderersEvent.RegisterRenderers 中为五个 BlockEntity 各注册一次。
 */
public class CookingPotGeoBlockRenderer<T extends BlockEntity & GeoAnimatable> extends GeoBlockRenderer<T> {

    /** 世界渲染缩放。 */
    private final float scale;
    /** x/z 居中补偿：模型中心轴在 0，GeckoLib 内部平移 (0.5,0,0.5) 在先、缩放作用在其后。 */
    private final float offsetXZ;

    /**
     * @param assetSubpath 注册 id（对应 geo/block/&lt;id&gt;.geo.json 与 textures/geckolib/&lt;id&gt;.png）
     * @param scale        世界渲染缩放（参考堕落十字架的做法，在 BER 内等比缩放收进一格）。
     *                     煮锅 geo 模型以方块中心为 X/Z 原点、底面为 y=0。
     */
    public CookingPotGeoBlockRenderer(ResourceLocation assetSubpath, float scale) {
        super(new CookingPotGeoModel<T>(assetSubpath));
        this.scale = scale;
        this.offsetXZ = 0.5F - 0.5F * scale;
    }

    @Override
    public void render(BlockEntity blockEntity, float partialTick, PoseStack poseStack,
                       MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        // PoseStack 为后乘：这里先平移+缩放，等价于先作用到顶点，之后才是 GeckoLib 内部的
        // translate(0.5,0,0.5)。模型中心在 0，需补 0.5-0.5s 才回到格中心；y 底在 0，贴地无需偏移。
        // 按满亮渲染，与 Blockbench 预览（均匀满光、保留面方向明暗）目视一致，
        // 避免 BlockEntity 取到偏低光照导致整锅发黑
        poseStack.pushPose();
        // 带 FACING 的锅（如铁锅平底锅）：GeckoLib 的 GeoBlockRenderer 检测到状态里有
        // HorizontalDirectionalBlock.FACING 属性时，会自动绕方块中心按朝向旋转模型
        // （模型需按「锅柄朝南 +Z」建模），无需在此手动旋转。煮锅等无该属性的方块不旋转。
        poseStack.translate(offsetXZ, 0.0F, offsetXZ);
        poseStack.scale(scale, scale, scale);
        super.render(blockEntity, partialTick, poseStack, bufferSource,
                LightTexture.FULL_BRIGHT, packedOverlay);
        poseStack.popPose();
    }
}
