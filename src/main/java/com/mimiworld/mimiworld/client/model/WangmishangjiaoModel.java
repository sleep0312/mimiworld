package com.mimiworld.mimiworld.client.model;

import com.mimiworld.mimiworld.MimiWorld;
import com.mimiworld.mimiworld.entity.WangmishangjiaoEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

/**
 * 望米伤鲛的 GeckoLib 模型绑定类：
 * geo/wangmishangjiao.geo.json + textures/entity/wangmishangjiao.png +
 * animations/wangmishangjiao.animation.json（animation.wangmishangjiao.idle/.walk/.attack）。
 */
public class WangmishangjiaoModel extends GeoModel<WangmishangjiaoEntity> {

    private static final ResourceLocation MODEL =
            new ResourceLocation(MimiWorld.MODID, "geo/wangmishangjiao.geo.json");
    private static final ResourceLocation TEXTURE =
            new ResourceLocation(MimiWorld.MODID, "textures/entity/wangmishangjiao.png");
    private static final ResourceLocation ANIMATION =
            new ResourceLocation(MimiWorld.MODID, "animations/wangmishangjiao.animation.json");

    @Override
    public ResourceLocation getModelResource(WangmishangjiaoEntity animatable) {
        return MODEL;
    }

    @Override
    public ResourceLocation getTextureResource(WangmishangjiaoEntity animatable) {
        return TEXTURE;
    }

    @Override
    public ResourceLocation getAnimationResource(WangmishangjiaoEntity animatable) {
        return ANIMATION;
    }
}
