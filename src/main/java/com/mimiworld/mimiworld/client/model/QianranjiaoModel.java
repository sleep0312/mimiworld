package com.mimiworld.mimiworld.client.model;

import com.mimiworld.mimiworld.MimiWorld;
import com.mimiworld.mimiworld.entity.QianranjiaoEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

/**
 * 浅染鲛的 GeckoLib 模型绑定类：
 * geo/qianranjiao.geo.json + textures/entity/qianranjiao.png +
 * animations/qianranjiao.animation.json（animation.qianranjiao.idle/walk/attack）。
 */
public class QianranjiaoModel extends GeoModel<QianranjiaoEntity> {

    private static final ResourceLocation MODEL =
            new ResourceLocation(MimiWorld.MODID, "geo/qianranjiao.geo.json");
    private static final ResourceLocation TEXTURE =
            new ResourceLocation(MimiWorld.MODID, "textures/entity/qianranjiao.png");
    private static final ResourceLocation ANIMATION =
            new ResourceLocation(MimiWorld.MODID, "animations/qianranjiao.animation.json");

    @Override
    public ResourceLocation getModelResource(QianranjiaoEntity animatable) {
        return MODEL;
    }

    @Override
    public ResourceLocation getTextureResource(QianranjiaoEntity animatable) {
        return TEXTURE;
    }

    @Override
    public ResourceLocation getAnimationResource(QianranjiaoEntity animatable) {
        return ANIMATION;
    }
}
