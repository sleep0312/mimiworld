package com.mimiworld.mimiworld.client.model;

import com.mimiworld.mimiworld.MimiWorld;
import com.mimiworld.mimiworld.entity.WujihuaEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

/**
 * 污棘花的 GeckoLib 模型绑定类：
 * geo/wujihua.geo.json + textures/entity/wujihua.png +
 * animations/wujihua.animation.json（animation.wujihua.idle/attack）。
 */
public class WujihuaModel extends GeoModel<WujihuaEntity> {

    private static final ResourceLocation MODEL =
            new ResourceLocation(MimiWorld.MODID, "geo/wujihua.geo.json");
    private static final ResourceLocation TEXTURE =
            new ResourceLocation(MimiWorld.MODID, "textures/entity/wujihua.png");
    private static final ResourceLocation ANIMATION =
            new ResourceLocation(MimiWorld.MODID, "animations/wujihua.animation.json");

    @Override
    public ResourceLocation getModelResource(WujihuaEntity animatable) {
        return MODEL;
    }

    @Override
    public ResourceLocation getTextureResource(WujihuaEntity animatable) {
        return TEXTURE;
    }

    @Override
    public ResourceLocation getAnimationResource(WujihuaEntity animatable) {
        return ANIMATION;
    }
}
