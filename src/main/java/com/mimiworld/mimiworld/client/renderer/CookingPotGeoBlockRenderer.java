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

    public CookingPotGeoBlockRenderer(ResourceLocation assetSubpath) {
        super(new CookingPotGeoModel<T>(assetSubpath));
    }

    @Override
    public void render(BlockEntity blockEntity, float partialTick, PoseStack poseStack,
                       MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        // 按满亮渲染，与 Blockbench 预览（均匀满光、保留面方向明暗）目视一致，
        // 避免 BlockEntity 取到偏低光照导致整锅发黑
        super.render(blockEntity, partialTick, poseStack, bufferSource,
                LightTexture.FULL_BRIGHT, packedOverlay);
    }
}
