package com.mimiworld.mimiworld.client.model;

import com.mimiworld.mimiworld.MimiWorld;
import com.mimiworld.mimiworld.entity.MihengPigCorpseEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

/**
 * 米哼猪尸的 GeckoLib 模型绑定类：
 * geo/mihengzhushi.geo.json + textures/entity/mihengzhushi.png +
 * animations/mihengzhushi.animation.json（animation.mihengzhushi.idle/walk/attack）。
 */
public class MihengPigCorpseModel extends GeoModel<MihengPigCorpseEntity> {

    private static final ResourceLocation MODEL =
            new ResourceLocation(MimiWorld.MODID, "geo/mihengzhushi.geo.json");
    private static final ResourceLocation TEXTURE =
            new ResourceLocation(MimiWorld.MODID, "textures/entity/mihengzhushi.png");
    private static final ResourceLocation ANIMATION =
            new ResourceLocation(MimiWorld.MODID, "animations/mihengzhushi.animation.json");

    @Override
    public ResourceLocation getModelResource(MihengPigCorpseEntity animatable) {
        return MODEL;
    }

    @Override
    public ResourceLocation getTextureResource(MihengPigCorpseEntity animatable) {
        return TEXTURE;
    }

    @Override
    public ResourceLocation getAnimationResource(MihengPigCorpseEntity animatable) {
        return ANIMATION;
    }
}
