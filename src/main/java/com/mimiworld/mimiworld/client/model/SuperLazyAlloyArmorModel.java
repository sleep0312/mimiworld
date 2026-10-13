package com.mimiworld.mimiworld.client.model;

import com.mimiworld.mimiworld.MimiWorld;
import com.mimiworld.mimiworld.item.SuperLazyAlloyArmorItem;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

/**
 * 超懒合金套（T5·终极套）的 GeckoLib 模型绑定。
 * 四件共用一套 geo/贴图，槽位差异由 GeoArmorRenderer 按 armor* 骨控制；
 * 本期无动画，返回项目公共的空动画文件 cooking_pots.animation.json。
 */
public class SuperLazyAlloyArmorModel extends GeoModel<SuperLazyAlloyArmorItem> {

    private static final ResourceLocation MODEL =
            new ResourceLocation(MimiWorld.MODID, "geo/super_lazy_alloy_armor.geo.json");
    private static final ResourceLocation TEXTURE =
            new ResourceLocation(MimiWorld.MODID, "textures/armor/super_lazy_alloy_armor.png");
    private static final ResourceLocation ANIMATION =
            new ResourceLocation(MimiWorld.MODID, "animations/cooking_pots.animation.json");

    @Override
    public ResourceLocation getModelResource(SuperLazyAlloyArmorItem item) {
        return MODEL;
    }

    @Override
    public ResourceLocation getTextureResource(SuperLazyAlloyArmorItem item) {
        return TEXTURE;
    }

    @Override
    public ResourceLocation getAnimationResource(SuperLazyAlloyArmorItem item) {
        return ANIMATION;
    }
}
