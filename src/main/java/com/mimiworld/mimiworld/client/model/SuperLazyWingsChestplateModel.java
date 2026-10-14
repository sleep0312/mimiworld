package com.mimiworld.mimiworld.client.model;

import com.mimiworld.mimiworld.MimiWorld;
import com.mimiworld.mimiworld.item.SuperLazyWingsChestplateItem;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

/**
 * 超懒神翼胸甲的 GeckoLib 模型绑定。
 */
public class SuperLazyWingsChestplateModel extends GeoModel<SuperLazyWingsChestplateItem> {

    private static final ResourceLocation MODEL =
            new ResourceLocation(MimiWorld.MODID, "geo/super_lazy_wings_chestplate.geo.json");
    private static final ResourceLocation TEXTURE =
            new ResourceLocation(MimiWorld.MODID, "textures/armor/super_lazy_wings_chestplate.png");
    private static final ResourceLocation ANIMATION =
            new ResourceLocation(MimiWorld.MODID, "animations/super_lazy_wings_chestplate.animation.json");

    @Override
    public ResourceLocation getModelResource(SuperLazyWingsChestplateItem item) {
        return MODEL;
    }

    @Override
    public ResourceLocation getTextureResource(SuperLazyWingsChestplateItem item) {
        return TEXTURE;
    }

    @Override
    public ResourceLocation getAnimationResource(SuperLazyWingsChestplateItem item) {
        return ANIMATION;
    }
}
