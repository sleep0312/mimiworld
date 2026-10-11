package com.mimiworld.mimiworld.client.model;

import com.mimiworld.mimiworld.MimiWorld;
import com.mimiworld.mimiworld.entity.QingjiechangmingkuiEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

/**
 * 清洁长命夔的 GeckoLib 模型绑定类。
 *
 * <p>资源（Blockbench 导出）：</p>
 * <ul>
 *   <li>模型：geo/qingjiechangmingkui.geo.json（8 骨骼：root/body/head/jaw/leg/tail/tail2/tail3，
 *       147 方块；单独足、无角牛首、jaw 为独立颌骨、三节上卷尾）</li>
 *   <li>贴图：textures/entity/qingjiechangmingkui.png（256×256，苍青黑厚皮 + 商周青铜夔龙纹
 *       浮雕 + 淡青雷电裂隙 + 发光青眼）</li>
 *   <li>动画：animations/qingjiechangmingkui.animation.json
 *       （animation.qingjiechangmingkui.idle / walk / attack / roar / stomp / triple_bite）</li>
 * </ul>
 */
public class QingjiechangmingkuiModel extends GeoModel<QingjiechangmingkuiEntity> {

    private static final ResourceLocation MODEL =
            new ResourceLocation(MimiWorld.MODID, "geo/qingjiechangmingkui.geo.json");
    private static final ResourceLocation TEXTURE =
            new ResourceLocation(MimiWorld.MODID, "textures/entity/qingjiechangmingkui.png");
    private static final ResourceLocation ANIMATION =
            new ResourceLocation(MimiWorld.MODID, "animations/qingjiechangmingkui.animation.json");

    @Override
    public ResourceLocation getModelResource(QingjiechangmingkuiEntity animatable) {
        return MODEL;
    }

    @Override
    public ResourceLocation getTextureResource(QingjiechangmingkuiEntity animatable) {
        return TEXTURE;
    }

    @Override
    public ResourceLocation getAnimationResource(QingjiechangmingkuiEntity animatable) {
        return ANIMATION;
    }
}
