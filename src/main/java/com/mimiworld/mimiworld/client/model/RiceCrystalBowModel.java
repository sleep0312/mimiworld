package com.mimiworld.mimiworld.client.model;

import com.mimiworld.mimiworld.MimiWorld;
import com.mimiworld.mimiworld.item.RiceCrystalBowItem;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

/**
 * 饭晶弓的 GeckoLib 物品模型。
 *
 * <p>资源路径：geo/fanjinggong.geo.json（模型）、textures/item/fanjinggong.png（贴图）、
 * animations/fanjinggong.animation.json（动画，内含 idle / draw / shoot 三条）；各视角的
 * 位置缩放由 models/item/rice_crystal_bow.json 的 display 节点控制。</p>
 */
public class RiceCrystalBowModel extends GeoModel<RiceCrystalBowItem> {

    private static final ResourceLocation MODEL =
            new ResourceLocation(MimiWorld.MODID, "geo/fanjinggong.geo.json");
    private static final ResourceLocation TEXTURE =
            new ResourceLocation(MimiWorld.MODID, "textures/item/fanjinggong.png");
    private static final ResourceLocation ANIMATION =
            new ResourceLocation(MimiWorld.MODID, "animations/fanjinggong.animation.json");

    @Override
    public ResourceLocation getModelResource(RiceCrystalBowItem animatable) {
        return MODEL;
    }

    @Override
    public ResourceLocation getTextureResource(RiceCrystalBowItem animatable) {
        return TEXTURE;
    }

    @Override
    public ResourceLocation getAnimationResource(RiceCrystalBowItem animatable) {
        return ANIMATION;
    }
}
