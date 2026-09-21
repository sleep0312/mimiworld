package com.mimiworld.mimiworld.client.model;

import com.mimiworld.mimiworld.MimiWorld;
import com.mimiworld.mimiworld.item.RemnantFallenCardinalSinArmorItem;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

/**
 * 残堕宗罪套装的 GeckoLib 模型绑定。
 * 四件共用一套 geo/贴图/动画，槽位差异由 GeoArmorRenderer 按 armor* 骨控制。
 */
public class RemnantFallenCardinalSinArmorModel extends GeoModel<RemnantFallenCardinalSinArmorItem> {

    private static final ResourceLocation MODEL =
            new ResourceLocation(MimiWorld.MODID, "geo/remnant_fallen_cardinal_sin_armor.geo.json");
    private static final ResourceLocation TEXTURE =
            new ResourceLocation(MimiWorld.MODID, "textures/armor/remnant_fallen_cardinal_sin_armor.png");
    private static final ResourceLocation ANIMATION =
            new ResourceLocation(MimiWorld.MODID, "animations/remnant_fallen_cardinal_sin_armor.animation.json");

    @Override
    public ResourceLocation getModelResource(RemnantFallenCardinalSinArmorItem item) {
        return MODEL;
    }

    @Override
    public ResourceLocation getTextureResource(RemnantFallenCardinalSinArmorItem item) {
        return TEXTURE;
    }

    @Override
    public ResourceLocation getAnimationResource(RemnantFallenCardinalSinArmorItem item) {
        return ANIMATION;
    }
}
