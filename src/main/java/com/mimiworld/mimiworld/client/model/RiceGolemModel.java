package com.mimiworld.mimiworld.client.model;

import com.mimiworld.mimiworld.MimiWorld;
import com.mimiworld.mimiworld.entity.RiceGolemEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

/**
 * 米傀儡（Rice Golem）的 GeckoLib 模型绑定类。
 *
 * <p>只负责告诉 GeckoLib 三个资源文件的位置：</p>
 * <ul>
 *   <li>模型：assets/mimiworld/geo/rice_golem.geo.json（Blockbench 导出，7 根骨骼：
 *       root/body/head/rightArm/leftArm/rightLeg/leftLeg，26 个方块）</li>
 *   <li>贴图：assets/mimiworld/textures/entity/rice_golem.png（128×128，大米主题：
 *       米白头与拳套、稻草躯干、麻绳束带、皮靴、草编胸甲）</li>
 *   <li>动画：assets/mimiworld/animations/rice_golem.animation.json
 *       （animation.rice_golem.idle / animation.rice_golem.walk）</li>
 * </ul>
 * 动画名与 {@link RiceGolemEntity} 中 RawAnimation 引用的键名严格一致。
 */
public class RiceGolemModel extends GeoModel<RiceGolemEntity> {

    private static final ResourceLocation MODEL =
            new ResourceLocation(MimiWorld.MODID, "geo/rice_golem.geo.json");
    private static final ResourceLocation TEXTURE =
            new ResourceLocation(MimiWorld.MODID, "textures/entity/rice_golem.png");
    private static final ResourceLocation ANIMATION =
            new ResourceLocation(MimiWorld.MODID, "animations/rice_golem.animation.json");

    @Override
    public ResourceLocation getModelResource(RiceGolemEntity animatable) {
        return MODEL;
    }

    @Override
    public ResourceLocation getTextureResource(RiceGolemEntity animatable) {
        return TEXTURE;
    }

    @Override
    public ResourceLocation getAnimationResource(RiceGolemEntity animatable) {
        return ANIMATION;
    }
}
