package com.mimiworld.mimiworld.client.model;

import com.mimiworld.mimiworld.MimiWorld;
import com.mimiworld.mimiworld.entity.HeavyCrystalRabbitKingEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

/**
 * 重晶兔王的 GeckoLib 模型绑定类。
 *
 * <p>资源（Blockbench 导出）：</p>
 * <ul>
 *   <li>模型：geo/heavy_crystal_rabbit_king.geo.json（root/body/head/ear_left/ear_right/
 *       arm_left/arm_right/leg_left/leg_right/tail/crystal_spikes 共 11 骨骼，70 方块，
 *       identifier = geometry.heavy_crystal_rabbit_king）</li>
 *   <li>贴图：textures/entity/heavy_crystal_rabbit_king.png（256×256，米白+稻金+叶绿+晶青+血红眼）</li>
 *   <li>动画：animations/heavy_crystal_rabbit_king.animation.json
 *       （animation.heavy_crystal_rabbit_king.idle / walk / attack / skill_slam / skill_enrage）</li>
 * </ul>
 */
public class HeavyCrystalRabbitKingModel extends GeoModel<HeavyCrystalRabbitKingEntity> {

    private static final ResourceLocation MODEL =
            new ResourceLocation(MimiWorld.MODID, "geo/heavy_crystal_rabbit_king.geo.json");
    private static final ResourceLocation TEXTURE =
            new ResourceLocation(MimiWorld.MODID, "textures/entity/heavy_crystal_rabbit_king.png");
    private static final ResourceLocation ANIMATION =
            new ResourceLocation(MimiWorld.MODID, "animations/heavy_crystal_rabbit_king.animation.json");

    @Override
    public ResourceLocation getModelResource(HeavyCrystalRabbitKingEntity animatable) {
        return MODEL;
    }

    @Override
    public ResourceLocation getTextureResource(HeavyCrystalRabbitKingEntity animatable) {
        return TEXTURE;
    }

    @Override
    public ResourceLocation getAnimationResource(HeavyCrystalRabbitKingEntity animatable) {
        return ANIMATION;
    }
}
