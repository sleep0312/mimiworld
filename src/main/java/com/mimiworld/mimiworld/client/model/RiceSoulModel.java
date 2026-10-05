package com.mimiworld.mimiworld.client.model;

import com.mimiworld.mimiworld.MimiWorld;
import com.mimiworld.mimiworld.entity.RiceSoulEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

/**
 * 米魂的 GeckoLib 模型绑定类：
 * geo/mihun.geo.json + textures/entity/mihun.png +
 * animations/mihun.animation.json（animation.mihun.idle/walk/attack）。
 */
public class RiceSoulModel extends GeoModel<RiceSoulEntity> {

    private static final ResourceLocation MODEL =
            new ResourceLocation(MimiWorld.MODID, "geo/mihun.geo.json");
    private static final ResourceLocation TEXTURE =
            new ResourceLocation(MimiWorld.MODID, "textures/entity/mihun.png");
    private static final ResourceLocation ANIMATION =
            new ResourceLocation(MimiWorld.MODID, "animations/mihun.animation.json");

    @Override
    public ResourceLocation getModelResource(RiceSoulEntity animatable) {
        return MODEL;
    }

    @Override
    public ResourceLocation getTextureResource(RiceSoulEntity animatable) {
        return TEXTURE;
    }

    @Override
    public ResourceLocation getAnimationResource(RiceSoulEntity animatable) {
        return ANIMATION;
    }
}
