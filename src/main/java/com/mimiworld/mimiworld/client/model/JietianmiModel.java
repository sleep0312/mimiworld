package com.mimiworld.mimiworld.client.model;

import com.mimiworld.mimiworld.MimiWorld;
import com.mimiworld.mimiworld.entity.JietianmiEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

/**
 * 界天米的 GeckoLib 模型绑定类：
 * geo/jietianmi.geo.json + textures/entity/jietianmi.png +
 * animations/jietianmi.animation.json（animation.jietianmi.idle/walk/attack）。
 */
public class JietianmiModel extends GeoModel<JietianmiEntity> {

    private static final ResourceLocation MODEL =
            new ResourceLocation(MimiWorld.MODID, "geo/jietianmi.geo.json");
    private static final ResourceLocation TEXTURE =
            new ResourceLocation(MimiWorld.MODID, "textures/entity/jietianmi.png");
    private static final ResourceLocation ANIMATION =
            new ResourceLocation(MimiWorld.MODID, "animations/jietianmi.animation.json");

    @Override
    public ResourceLocation getModelResource(JietianmiEntity animatable) {
        return MODEL;
    }

    @Override
    public ResourceLocation getTextureResource(JietianmiEntity animatable) {
        return TEXTURE;
    }

    @Override
    public ResourceLocation getAnimationResource(JietianmiEntity animatable) {
        return ANIMATION;
    }
}
