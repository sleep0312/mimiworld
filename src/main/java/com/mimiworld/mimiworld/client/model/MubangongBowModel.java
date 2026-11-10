package com.mimiworld.mimiworld.client.model;

import com.mimiworld.mimiworld.MimiWorld;
import com.mimiworld.mimiworld.item.MubangongBowItem;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

/**
 * 六种通用弓（饭晶合金 / 超重结晶 / 复合米饭 / 清洁复合 / 超懒合金 / 残堕宗罪之弓）
 * 共用的 GeckoLib 物品模型。
 *
 * <p>资源路径：geo/mubangong.geo.json（模型）、animations/mubangong.animation.json（动画，
 * 内含 idle / draw / shoot）为六弓共用；贴图按弓区分，取 textures/item/&lt;textureName&gt;.png
 * （textureName 由 {@link MubangongBowItem} 的构造参数给出）；各视角的位置缩放由
 * models/item/&lt;弓注册名&gt;.json 的 display 节点控制。</p>
 */
public class MubangongBowModel extends GeoModel<MubangongBowItem> {

    private static final ResourceLocation MODEL =
            new ResourceLocation(MimiWorld.MODID, "geo/mubangong.geo.json");
    private static final ResourceLocation ANIMATION =
            new ResourceLocation(MimiWorld.MODID, "animations/mubangong.animation.json");

    @Override
    public ResourceLocation getModelResource(MubangongBowItem animatable) {
        return MODEL;
    }

    @Override
    public ResourceLocation getTextureResource(MubangongBowItem animatable) {
        return new ResourceLocation(MimiWorld.MODID, "textures/item/" + animatable.getTextureName() + ".png");
    }

    @Override
    public ResourceLocation getAnimationResource(MubangongBowItem animatable) {
        return ANIMATION;
    }
}
