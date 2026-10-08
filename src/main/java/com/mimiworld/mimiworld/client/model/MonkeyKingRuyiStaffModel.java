package com.mimiworld.mimiworld.client.model;

import com.mimiworld.mimiworld.MimiWorld;
import com.mimiworld.mimiworld.item.MonkeyKingRuyiStaffItem;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

/**
 * 猴王如意棍的 GeckoLib 物品模型：无动画，仅静态 3D 模型。
 * 各视角（GUI/手持/地面/展示框）的位置缩放由
 * models/item/monkey_king_ruyi_staff.json 的 display 节点控制。
 */
public class MonkeyKingRuyiStaffModel extends GeoModel<MonkeyKingRuyiStaffItem> {

    private static final ResourceLocation MODEL =
            new ResourceLocation(MimiWorld.MODID, "geo/houwangruyigun.geo.json");
    private static final ResourceLocation TEXTURE =
            new ResourceLocation(MimiWorld.MODID, "textures/item/houwangruyigun.png");
    private static final ResourceLocation ANIMATION =
            new ResourceLocation(MimiWorld.MODID, "animations/houwangruyigun.animation.json");

    @Override
    public ResourceLocation getModelResource(MonkeyKingRuyiStaffItem item) {
        return MODEL;
    }

    @Override
    public ResourceLocation getTextureResource(MonkeyKingRuyiStaffItem item) {
        return TEXTURE;
    }

    @Override
    public ResourceLocation getAnimationResource(MonkeyKingRuyiStaffItem item) {
        return ANIMATION;
    }
}
