package com.mimiworld.mimiworld.client.model;

import com.mimiworld.mimiworld.MimiWorld;
import com.mimiworld.mimiworld.entity.ChaoticVoidToucherEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

/**
 * 混沌不可名状之米界虚无触的 GeckoLib 模型绑定类：
 * geo/hundunbukemingzhuangzhimijiexuwuchu.geo.json
 * + textures/entity/hundunbukemingzhuangzhimijiexuwuchu.png
 * + animations/hundunbukemingzhuangzhimijiexuwuchu.animation.json
 * （animation....idle / .attack / .attack_spit）。
 */
public class ChaoticVoidToucherModel extends GeoModel<ChaoticVoidToucherEntity> {

    private static final ResourceLocation MODEL =
            new ResourceLocation(MimiWorld.MODID, "geo/hundunbukemingzhuangzhimijiexuwuchu.geo.json");
    private static final ResourceLocation TEXTURE =
            new ResourceLocation(MimiWorld.MODID, "textures/entity/hundunbukemingzhuangzhimijiexuwuchu.png");
    private static final ResourceLocation ANIMATION =
            new ResourceLocation(MimiWorld.MODID, "animations/hundunbukemingzhuangzhimijiexuwuchu.animation.json");

    @Override
    public ResourceLocation getModelResource(ChaoticVoidToucherEntity animatable) {
        return MODEL;
    }

    @Override
    public ResourceLocation getTextureResource(ChaoticVoidToucherEntity animatable) {
        return TEXTURE;
    }

    @Override
    public ResourceLocation getAnimationResource(ChaoticVoidToucherEntity animatable) {
        return ANIMATION;
    }
}
