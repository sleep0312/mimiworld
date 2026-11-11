package com.mimiworld.mimiworld.client.model;

import com.mimiworld.mimiworld.MimiWorld;
import com.mimiworld.mimiworld.entity.MimiMonkeyKingEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

/**
 * 米米猴王的 GeckoLib 模型绑定类。
 *
 * <p>资源（Blockbench 导出）：</p>
 * <ul>
 *   <li>模型：geo/mimihouwang.geo.json（10 骨骼：root/body/head/crown/rightArm/leftArm/
 *       tail/leftLeg/rightLeg/staff，80 方块；staff 独立挂在 root 下以便扔棒动画脱手，
 *       棒体方块直接建在 staff 骨骼里、位置对齐右手掌心）</li>
 *   <li>贴图：textures/entity/mimihouwang.png（128×128）</li>
 *   <li>动画：animations/mimihouwang.animation.json
 *       （animation.mimihouwang.idle / walk / attack / attack_slam / throw）</li>
 * </ul>
 */
public class MimiMonkeyKingModel extends GeoModel<MimiMonkeyKingEntity> {

    private static final ResourceLocation MODEL =
            new ResourceLocation(MimiWorld.MODID, "geo/mimihouwang.geo.json");
    private static final ResourceLocation TEXTURE =
            new ResourceLocation(MimiWorld.MODID, "textures/entity/mimihouwang.png");
    private static final ResourceLocation ANIMATION =
            new ResourceLocation(MimiWorld.MODID, "animations/mimihouwang.animation.json");

    @Override
    public ResourceLocation getModelResource(MimiMonkeyKingEntity animatable) {
        return MODEL;
    }

    @Override
    public ResourceLocation getTextureResource(MimiMonkeyKingEntity animatable) {
        return TEXTURE;
    }

    @Override
    public ResourceLocation getAnimationResource(MimiMonkeyKingEntity animatable) {
        return ANIMATION;
    }
}
