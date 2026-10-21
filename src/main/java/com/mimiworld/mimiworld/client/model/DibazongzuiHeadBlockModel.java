package com.mimiworld.mimiworld.client.model;

import com.mimiworld.mimiworld.MimiWorld;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntity;
import software.bernie.geckolib.core.animatable.GeoAnimatable;
import software.bernie.geckolib.model.GeoModel;

/**
 * 第八宗罪龙头摆件方块的 GeckoLib 模型绑定类。
 *
 * <ul>
 *   <li>模型：geo/block/dibazongzui_head.geo.json
 *       （geometry.dibazongzui_head：龙头 head + jaw 共 99 方块）</li>
 *   <li>贴图：textures/geckolib/dibazongzui_head.png（与第八宗罪龙本体同源 2048×2048）</li>
 *   <li>动画：cooking_pots.animation.json（空动画文件，静态渲染）</li>
 * </ul>
 */
public class DibazongzuiHeadBlockModel<T extends BlockEntity & GeoAnimatable> extends GeoModel<T> {

    private static final ResourceLocation MODEL =
            new ResourceLocation(MimiWorld.MODID, "geo/block/dibazongzui_head.geo.json");
    private static final ResourceLocation TEXTURE =
            new ResourceLocation(MimiWorld.MODID, "textures/geckolib/dibazongzui_head.png");
    private static final ResourceLocation ANIMATION =
            new ResourceLocation(MimiWorld.MODID, "animations/cooking_pots.animation.json");

    @Override
    public ResourceLocation getModelResource(T animatable) {
        return MODEL;
    }

    @Override
    public ResourceLocation getTextureResource(T animatable) {
        return TEXTURE;
    }

    @Override
    public ResourceLocation getAnimationResource(T animatable) {
        return ANIMATION;
    }
}
