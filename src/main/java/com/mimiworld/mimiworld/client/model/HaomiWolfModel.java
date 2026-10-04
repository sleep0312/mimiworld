package com.mimiworld.mimiworld.client.model;

import com.mimiworld.mimiworld.MimiWorld;
import com.mimiworld.mimiworld.entity.HaomiWolfEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

/**
 * 嗥米狼（Haomi Wolf）的 GeckoLib 模型绑定类。
 *
 * <p>只负责告诉 GeckoLib 三个资源文件的位置：</p>
 * <ul>
 *   <li>模型：assets/mimiworld/geo/haomi_wolf.geo.json（Blockbench 导出，8 根骨骼：
 *       root/body/head/rightFrontLeg/leftFrontLeg/rightHindLeg/leftHindLeg/tail）</li>
 *   <li>贴图：assets/mimiworld/textures/entity/haomi_wolf.png（128×128，大米主题配色）</li>
 *   <li>动画：assets/mimiworld/animations/haomi_wolf.animation.json（animation.idle / animation.walk）</li>
 * </ul>
 * 动画名与 HaomiWolfEntity 中 RawAnimation 引用的键名严格一致。
 */
public class HaomiWolfModel extends GeoModel<HaomiWolfEntity> {

    private static final ResourceLocation MODEL =
            new ResourceLocation(MimiWorld.MODID, "geo/haomi_wolf.geo.json");
    private static final ResourceLocation TEXTURE =
            new ResourceLocation(MimiWorld.MODID, "textures/entity/haomi_wolf.png");
    private static final ResourceLocation ANIMATION =
            new ResourceLocation(MimiWorld.MODID, "animations/haomi_wolf.animation.json");

    @Override
    public ResourceLocation getModelResource(HaomiWolfEntity animatable) {
        return MODEL;
    }

    @Override
    public ResourceLocation getTextureResource(HaomiWolfEntity animatable) {
        return TEXTURE;
    }

    @Override
    public ResourceLocation getAnimationResource(HaomiWolfEntity animatable) {
        return ANIMATION;
    }
}
