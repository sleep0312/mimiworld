package com.mimiworld.mimiworld.client.model;

import com.mimiworld.mimiworld.MimiWorld;
import com.mimiworld.mimiworld.entity.MicroDuckEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

/**
 * 微型鸭（MicroDuck）的 GeckoLib 模型绑定类。
 *
 * <p>只负责告诉 GeckoLib 三个资源文件的位置：</p>
 * <ul>
 *   <li>模型：assets/mimiworld/geo/microduck.geo.json（Blockbench 导出的骨骼/立方体）</li>
 *   <li>贴图：assets/mimiworld/textures/entity/microduck.png（32×32）</li>
 *   <li>动画：assets/mimiworld/animations/microduck.animation.json（idle / walk）</li>
 * </ul>
 * 模型里的骨骼动画名（idle、walk）与 MicroDuckEntity 中 RawAnimation 引用的名称一致。
 */
public class MicroDuckModel extends GeoModel<MicroDuckEntity> {

    private static final ResourceLocation MODEL =
            new ResourceLocation(MimiWorld.MODID, "geo/microduck.geo.json");
    private static final ResourceLocation TEXTURE =
            new ResourceLocation(MimiWorld.MODID, "textures/entity/microduck.png");
    private static final ResourceLocation ANIMATION =
            new ResourceLocation(MimiWorld.MODID, "animations/microduck.animation.json");

    @Override
    public ResourceLocation getModelResource(MicroDuckEntity animatable) {
        return MODEL;
    }

    @Override
    public ResourceLocation getTextureResource(MicroDuckEntity animatable) {
        return TEXTURE;
    }

    @Override
    public ResourceLocation getAnimationResource(MicroDuckEntity animatable) {
        return ANIMATION;
    }
}
