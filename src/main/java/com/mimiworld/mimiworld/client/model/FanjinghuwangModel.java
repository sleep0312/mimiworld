package com.mimiworld.mimiworld.client.model;

import com.mimiworld.mimiworld.MimiWorld;
import com.mimiworld.mimiworld.entity.FanjinghuwangEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

/**
 * 饭晶虎王的 GeckoLib 模型绑定类。
 *
 * <p>资源（Blockbench 导出）：</p>
 * <ul>
 *   <li>模型：geo/fanjinghuwang.geo.json（13 骨骼：root/body/head/ear_left/ear_right/
 *       crown/jaw/arm_left/arm_right/leg_left/leg_right/tail/tail2/tail3，37 方块；
 *       jaw 为独立颌骨以播放撕咬张口动画）</li>
 *   <li>贴图：textures/entity/fanjinghuwang.png（128×128，米白+稻叶绿+稻金+晶青大米主题）</li>
 *   <li>动画：animations/fanjinghuwang.animation.json
 *       （animation.fanjinghuwang.idle / walk / attack / feast / slam）</li>
 * </ul>
 */
public class FanjinghuwangModel extends GeoModel<FanjinghuwangEntity> {

    private static final ResourceLocation MODEL =
            new ResourceLocation(MimiWorld.MODID, "geo/fanjinghuwang.geo.json");
    private static final ResourceLocation TEXTURE =
            new ResourceLocation(MimiWorld.MODID, "textures/entity/fanjinghuwang.png");
    private static final ResourceLocation ANIMATION =
            new ResourceLocation(MimiWorld.MODID, "animations/fanjinghuwang.animation.json");

    @Override
    public ResourceLocation getModelResource(FanjinghuwangEntity animatable) {
        return MODEL;
    }

    @Override
    public ResourceLocation getTextureResource(FanjinghuwangEntity animatable) {
        return TEXTURE;
    }

    @Override
    public ResourceLocation getAnimationResource(FanjinghuwangEntity animatable) {
        return ANIMATION;
    }
}
