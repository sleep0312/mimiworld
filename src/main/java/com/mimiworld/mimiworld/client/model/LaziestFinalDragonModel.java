package com.mimiworld.mimiworld.client.model;

import com.mimiworld.mimiworld.MimiWorld;
import com.mimiworld.mimiworld.entity.LaziestFinalDragonEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

/**
 * 最懒终末龙（末影龙 Promax 狂暴强化版）的 GeckoLib 模型绑定类。
 *
 * <p>三个资源文件：</p>
 * <ul>
 *   <li>模型：assets/mimiworld/geo/laziest_final_dragon.geo.json（Blockbench 1.12.0，233 方块/32 骨骼）</li>
 *   <li>贴图：assets/mimiworld/textures/entity/laziest_final_dragon.png（1024×1024 墨黑深紫战纹）</li>
 *   <li>动画：assets/mimiworld/animations/laziest_final_dragon.animation.json
 *       （animation.circling 盘旋 / dive_strike 冲撞 / dragon_breath 龙息 /
 *       laser_beam 光束 / melee_bite 撕咬 / hurt_roar 受伤嘶吼）</li>
 * </ul>
 */
public class LaziestFinalDragonModel extends GeoModel<LaziestFinalDragonEntity> {

    private static final ResourceLocation MODEL =
            new ResourceLocation(MimiWorld.MODID, "geo/laziest_final_dragon.geo.json");
    private static final ResourceLocation TEXTURE =
            new ResourceLocation(MimiWorld.MODID, "textures/entity/laziest_final_dragon.png");
    private static final ResourceLocation ANIMATION =
            new ResourceLocation(MimiWorld.MODID, "animations/laziest_final_dragon.animation.json");

    @Override
    public ResourceLocation getModelResource(LaziestFinalDragonEntity animatable) {
        return MODEL;
    }

    @Override
    public ResourceLocation getTextureResource(LaziestFinalDragonEntity animatable) {
        return TEXTURE;
    }

    @Override
    public ResourceLocation getAnimationResource(LaziestFinalDragonEntity animatable) {
        return ANIMATION;
    }
}
