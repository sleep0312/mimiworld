package com.mimiworld.mimiworld.client.model;

import com.mimiworld.mimiworld.MimiWorld;
import com.mimiworld.mimiworld.item.MimiDivineWingsItem;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

/**
 * 米米神翼的 GeckoLib 模型绑定（仅双翼几何体）。
 */
public class MimiDivineWingsModel extends GeoModel<MimiDivineWingsItem> {

    private static final ResourceLocation MODEL =
            new ResourceLocation(MimiWorld.MODID, "geo/mimi_divine_wings.geo.json");
    private static final ResourceLocation TEXTURE =
            new ResourceLocation(MimiWorld.MODID, "textures/armor/mimi_divine_wings.png");
    private static final ResourceLocation ANIMATION =
            new ResourceLocation(MimiWorld.MODID, "animations/mimi_divine_wings.animation.json");

    @Override
    public ResourceLocation getModelResource(MimiDivineWingsItem item) {
        return MODEL;
    }

    @Override
    public ResourceLocation getTextureResource(MimiDivineWingsItem item) {
        return TEXTURE;
    }

    @Override
    public ResourceLocation getAnimationResource(MimiDivineWingsItem item) {
        return ANIMATION;
    }
}
