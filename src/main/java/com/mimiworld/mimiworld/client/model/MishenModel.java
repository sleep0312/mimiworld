package com.mimiworld.mimiworld.client.model;

import com.mimiworld.mimiworld.MimiWorld;
import com.mimiworld.mimiworld.entity.MishenEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

/**
 * 米蜃的 GeckoLib 模型绑定类：
 * geo/mishen.geo.json + textures/entity/mishen.png +
 * animations/mishen.animation.json（animation.mishen.idle/walk/attack）。
 */
public class MishenModel extends GeoModel<MishenEntity> {

    private static final ResourceLocation MODEL =
            new ResourceLocation(MimiWorld.MODID, "geo/mishen.geo.json");
    private static final ResourceLocation TEXTURE =
            new ResourceLocation(MimiWorld.MODID, "textures/entity/mishen.png");
    private static final ResourceLocation ANIMATION =
            new ResourceLocation(MimiWorld.MODID, "animations/mishen.animation.json");

    @Override
    public ResourceLocation getModelResource(MishenEntity animatable) {
        return MODEL;
    }

    @Override
    public ResourceLocation getTextureResource(MishenEntity animatable) {
        return TEXTURE;
    }

    @Override
    public ResourceLocation getAnimationResource(MishenEntity animatable) {
        return ANIMATION;
    }
}
