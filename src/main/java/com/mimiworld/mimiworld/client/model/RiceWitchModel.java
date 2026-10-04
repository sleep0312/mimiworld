package com.mimiworld.mimiworld.client.model;

import com.mimiworld.mimiworld.MimiWorld;
import com.mimiworld.mimiworld.entity.RiceWitchEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

/**
 * 米巫（Rice Witch）的 GeckoLib 模型绑定类。
 *
 * <p>资源位置：</p>
 * <ul>
 *   <li>模型：assets/mimiworld/geo/rice_witch.geo.json（64 方块 8 骨骼：
 *       root/body/head/hatTip/rightArm/leftArm/rightLeg/leftLeg，弯帽尖为独立骨）</li>
 *   <li>贴图：assets/mimiworld/textures/entity/rice_witch.png（128×128，黑紫丝绒大米女巫配色）</li>
 *   <li>动画：assets/mimiworld/animations/rice_witch.animation.json
 *       （animation.idle / animation.walk / animation.attack / animation.throw）</li>
 * </ul>
 * 动画名与 RiceWitchEntity 中 RawAnimation 引用的键名严格一致。
 */
public class RiceWitchModel extends GeoModel<RiceWitchEntity> {

    private static final ResourceLocation MODEL =
            new ResourceLocation(MimiWorld.MODID, "geo/rice_witch.geo.json");
    private static final ResourceLocation TEXTURE =
            new ResourceLocation(MimiWorld.MODID, "textures/entity/rice_witch.png");
    private static final ResourceLocation ANIMATION =
            new ResourceLocation(MimiWorld.MODID, "animations/rice_witch.animation.json");

    @Override
    public ResourceLocation getModelResource(RiceWitchEntity animatable) {
        return MODEL;
    }

    @Override
    public ResourceLocation getTextureResource(RiceWitchEntity animatable) {
        return TEXTURE;
    }

    @Override
    public ResourceLocation getAnimationResource(RiceWitchEntity animatable) {
        return ANIMATION;
    }
}
