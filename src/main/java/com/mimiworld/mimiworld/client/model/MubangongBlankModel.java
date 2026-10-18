package com.mimiworld.mimiworld.client.model;

import com.mimiworld.mimiworld.MimiWorld;
import com.mimiworld.mimiworld.item.MubangongBlankItem;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

/**
 * 清洁复合弓胚（合成材料，无使用效果）的 GeckoLib 物品模型。
 *
 * <p>与正式弓共用同一套 geo 模型与动画资源（geo/mubangong.geo.json、
 * animations/mubangong.animation.json），贴图取 textures/item/&lt;textureName&gt;.png
 * （textureName 由 {@link MubangongBlankItem} 的构造参数给出）；各视角的位置缩放由
 * models/item/cleansing_compound_bow_blank.json 的 display 节点控制。</p>
 */
public class MubangongBlankModel extends GeoModel<MubangongBlankItem> {

    private static final ResourceLocation MODEL =
            new ResourceLocation(MimiWorld.MODID, "geo/mubangong.geo.json");
    private static final ResourceLocation ANIMATION =
            new ResourceLocation(MimiWorld.MODID, "animations/mubangong.animation.json");

    @Override
    public ResourceLocation getModelResource(MubangongBlankItem animatable) {
        return MODEL;
    }

    @Override
    public ResourceLocation getTextureResource(MubangongBlankItem animatable) {
        return new ResourceLocation(MimiWorld.MODID, "textures/item/" + animatable.getTextureName() + ".png");
    }

    @Override
    public ResourceLocation getAnimationResource(MubangongBlankItem animatable) {
        return ANIMATION;
    }
}
