package com.mimiworld.mimiworld.client.model;

import com.mimiworld.mimiworld.MimiWorld;
import com.mimiworld.mimiworld.entity.LazyElfEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

/**
 * 懒精灵的 GeckoLib 模型绑定类：
 * geo/lanjingling.geo.json + textures/entity/lanjingling.png +
 * animations/lanjingling.animation.json（animation.lanjingling.idle/walk/attack，
 * 另有 attack_throw 投掷动画备用）。
 */
public class LazyElfModel extends GeoModel<LazyElfEntity> {

    private static final ResourceLocation MODEL =
            new ResourceLocation(MimiWorld.MODID, "geo/lanjingling.geo.json");
    private static final ResourceLocation TEXTURE =
            new ResourceLocation(MimiWorld.MODID, "textures/entity/lanjingling.png");
    private static final ResourceLocation ANIMATION =
            new ResourceLocation(MimiWorld.MODID, "animations/lanjingling.animation.json");

    @Override
    public ResourceLocation getModelResource(LazyElfEntity animatable) {
        return MODEL;
    }

    @Override
    public ResourceLocation getTextureResource(LazyElfEntity animatable) {
        return TEXTURE;
    }

    @Override
    public ResourceLocation getAnimationResource(LazyElfEntity animatable) {
        return ANIMATION;
    }
}
