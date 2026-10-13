package com.mimiworld.mimiworld.client.model;

import com.mimiworld.mimiworld.MimiWorld;
import com.mimiworld.mimiworld.item.CleansingCompoundArmorItem;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

/**
 * 清洁米饭套（T4）的 GeckoLib 模型绑定。
 * 四件共用一套 geo/贴图，槽位差异由 GeoArmorRenderer 按 armor* 骨控制；
 * 本期无动画，返回项目公共的空动画文件 cooking_pots.animation.json。
 */
public class CleansingCompoundArmorModel extends GeoModel<CleansingCompoundArmorItem> {

    private static final ResourceLocation MODEL =
            new ResourceLocation(MimiWorld.MODID, "geo/cleansing_compound_armor.geo.json");
    private static final ResourceLocation TEXTURE =
            new ResourceLocation(MimiWorld.MODID, "textures/armor/cleansing_compound_armor.png");
    private static final ResourceLocation ANIMATION =
            new ResourceLocation(MimiWorld.MODID, "animations/cooking_pots.animation.json");

    @Override
    public ResourceLocation getModelResource(CleansingCompoundArmorItem item) {
        return MODEL;
    }

    @Override
    public ResourceLocation getTextureResource(CleansingCompoundArmorItem item) {
        return TEXTURE;
    }

    @Override
    public ResourceLocation getAnimationResource(CleansingCompoundArmorItem item) {
        return ANIMATION;
    }
}
