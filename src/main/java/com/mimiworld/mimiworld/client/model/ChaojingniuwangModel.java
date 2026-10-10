package com.mimiworld.mimiworld.client.model;

import com.mimiworld.mimiworld.MimiWorld;
import com.mimiworld.mimiworld.entity.ChaojingniuwangEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

/**
 * 超晶牛王的 GeckoLib 模型绑定类。
 *
 * <p>资源（Blockbench 导出）：</p>
 * <ul>
 *   <li>模型：geo/super_crystal_cow_king.geo.json（root/body/head/arm_left/arm_right/
 *       leg_left/leg_right/tail/tail2/horn_left/horn_right/ear_left/ear_right/axe 等骨骼，
 *       79 方块；axe 挂在 arm_right 下，绑定姿态与右臂成 45° 夹角）</li>
 *   <li>贴图：textures/entity/super_crystal_cow_king.png（256×256，米白+稻金护甲+叶绿+晶青大米主题）</li>
 *   <li>动画：animations/super_crystal_cow_king.animation.json
 *       （animation.super_crystal_cow_king.idle / walk / attack / axe_combo / charge_start / charge）</li>
 * </ul>
 */
public class ChaojingniuwangModel extends GeoModel<ChaojingniuwangEntity> {

    private static final ResourceLocation MODEL =
            new ResourceLocation(MimiWorld.MODID, "geo/super_crystal_cow_king.geo.json");
    private static final ResourceLocation TEXTURE =
            new ResourceLocation(MimiWorld.MODID, "textures/entity/super_crystal_cow_king.png");
    private static final ResourceLocation ANIMATION =
            new ResourceLocation(MimiWorld.MODID, "animations/super_crystal_cow_king.animation.json");

    @Override
    public ResourceLocation getModelResource(ChaojingniuwangEntity animatable) {
        return MODEL;
    }

    @Override
    public ResourceLocation getTextureResource(ChaojingniuwangEntity animatable) {
        return TEXTURE;
    }

    @Override
    public ResourceLocation getAnimationResource(ChaojingniuwangEntity animatable) {
        return ANIMATION;
    }
}
