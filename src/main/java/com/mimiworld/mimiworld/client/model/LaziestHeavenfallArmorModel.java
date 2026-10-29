package com.mimiworld.mimiworld.client.model;

import com.mimiworld.mimiworld.MimiWorld;
import com.mimiworld.mimiworld.item.LaziestHeavenfallArmorItem;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

/**
 * 最懒天堕套的 GeckoLib 模型绑定。
 * 四件共用一套 geo/贴图/动画，槽位差异由 GeoArmorRenderer 按 armor* 骨控制。
 */
public class LaziestHeavenfallArmorModel extends GeoModel<LaziestHeavenfallArmorItem> {

    private static final ResourceLocation MODEL =
            new ResourceLocation(MimiWorld.MODID, "geo/laziest_heavenfall_armor.geo.json");
    private static final ResourceLocation TEXTURE =
            new ResourceLocation(MimiWorld.MODID, "textures/armor/laziest_heavenfall_armor.png");
    private static final ResourceLocation ANIMATION =
            new ResourceLocation(MimiWorld.MODID, "animations/laziest_heavenfall_armor.animation.json");

    @Override
    public ResourceLocation getModelResource(LaziestHeavenfallArmorItem item) {
        return MODEL;
    }

    @Override
    public ResourceLocation getTextureResource(LaziestHeavenfallArmorItem item) {
        return TEXTURE;
    }

    @Override
    public ResourceLocation getAnimationResource(LaziestHeavenfallArmorItem item) {
        return ANIMATION;
    }
}
