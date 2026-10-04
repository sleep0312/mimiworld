package com.mimiworld.mimiworld.client.model;

import com.mimiworld.mimiworld.MimiWorld;
import com.mimiworld.mimiworld.entity.MimiSkeletonEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

/**
 * 米米骷髅（Mimi Skeleton）的 GeckoLib 模型绑定类。
 *
 * <p>只负责告诉 GeckoLib 三个资源文件的位置：</p>
 * <ul>
 *   <li>模型：assets/mimiworld/geo/mimi_skeleton.geo.json（Blockbench 导出，7 根骨骼：
 *       root/body/head/rightArm/leftArm/rightLeg/leftLeg）</li>
 *   <li>贴图：assets/mimiworld/textures/entity/mimi_skeleton.png（128×128，大米骷髅配色）</li>
 *   <li>动画：assets/mimiworld/animations/mimi_skeleton.animation.json（animation.idle / animation.walk）</li>
 * </ul>
 * 动画名与 MimiSkeletonEntity 中 RawAnimation 引用的键名严格一致。
 */
public class MimiSkeletonModel extends GeoModel<MimiSkeletonEntity> {

    private static final ResourceLocation MODEL =
            new ResourceLocation(MimiWorld.MODID, "geo/mimi_skeleton.geo.json");
    private static final ResourceLocation TEXTURE =
            new ResourceLocation(MimiWorld.MODID, "textures/entity/mimi_skeleton.png");
    private static final ResourceLocation ANIMATION =
            new ResourceLocation(MimiWorld.MODID, "animations/mimi_skeleton.animation.json");

    @Override
    public ResourceLocation getModelResource(MimiSkeletonEntity animatable) {
        return MODEL;
    }

    @Override
    public ResourceLocation getTextureResource(MimiSkeletonEntity animatable) {
        return TEXTURE;
    }

    @Override
    public ResourceLocation getAnimationResource(MimiSkeletonEntity animatable) {
        return ANIMATION;
    }
}
