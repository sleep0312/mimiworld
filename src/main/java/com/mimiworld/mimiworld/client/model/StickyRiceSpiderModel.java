package com.mimiworld.mimiworld.client.model;

import com.mimiworld.mimiworld.MimiWorld;
import com.mimiworld.mimiworld.entity.StickyRiceSpiderEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

/**
 * 黏米之蛛（Sticky Rice Spider）的 GeckoLib 模型绑定类。
 *
 * <p>只负责告诉 GeckoLib 三个资源文件的位置：</p>
 * <ul>
 *   <li>模型：assets/mimiworld/geo/spider.geo.json（Blockbench 导出的骨骼/立方体）</li>
 *   <li>贴图：assets/mimiworld/textures/entity/spider.png（32×16）</li>
 *   <li>动画：assets/mimiworld/animations/spider.animation.json（idle / walk / attack）</li>
 * </ul>
 * 动画名与 StickyRiceSpiderEntity 中 RawAnimation 引用的名称严格一致。
 */
public class StickyRiceSpiderModel extends GeoModel<StickyRiceSpiderEntity> {

    private static final ResourceLocation MODEL =
            new ResourceLocation(MimiWorld.MODID, "geo/spider.geo.json");
    private static final ResourceLocation TEXTURE =
            new ResourceLocation(MimiWorld.MODID, "textures/entity/spider.png");
    private static final ResourceLocation ANIMATION =
            new ResourceLocation(MimiWorld.MODID, "animations/spider.animation.json");

    @Override
    public ResourceLocation getModelResource(StickyRiceSpiderEntity animatable) {
        return MODEL;
    }

    @Override
    public ResourceLocation getTextureResource(StickyRiceSpiderEntity animatable) {
        return TEXTURE;
    }

    @Override
    public ResourceLocation getAnimationResource(StickyRiceSpiderEntity animatable) {
        return ANIMATION;
    }
}
