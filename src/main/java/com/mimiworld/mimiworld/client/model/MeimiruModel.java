package com.mimiworld.mimiworld.client.model;

import com.mimiworld.mimiworld.MimiWorld;
import com.mimiworld.mimiworld.entity.MeimiruEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

/**
 * 没米入（Meimiru）的 GeckoLib 模型绑定类。
 *
 * <p>只负责告诉 GeckoLib 三个资源文件的位置：</p>
 * <ul>
 *   <li>模型：assets/mimiworld/geo/meimiru.geo.json（Blockbench 导出，7 根骨骼：
 *       root/body/head/rightArm/leftArm/rightLeg/leftLeg，51 方块修长末影人形）</li>
 *   <li>贴图：assets/mimiworld/textures/entity/meimiru.png（64×128，末影暗紫+米白+淡金大米主题）</li>
 *   <li>动画：assets/mimiworld/animations/meimiru.animation.json
 *       （animation.idle / animation.walk / animation.attack）</li>
 * </ul>
 * 动画名与 MeimiruEntity 中 RawAnimation 引用的键名严格一致。
 */
public class MeimiruModel extends GeoModel<MeimiruEntity> {

    private static final ResourceLocation MODEL =
            new ResourceLocation(MimiWorld.MODID, "geo/meimiru.geo.json");
    private static final ResourceLocation TEXTURE =
            new ResourceLocation(MimiWorld.MODID, "textures/entity/meimiru.png");
    private static final ResourceLocation ANIMATION =
            new ResourceLocation(MimiWorld.MODID, "animations/meimiru.animation.json");

    @Override
    public ResourceLocation getModelResource(MeimiruEntity animatable) {
        return MODEL;
    }

    @Override
    public ResourceLocation getTextureResource(MeimiruEntity animatable) {
        return TEXTURE;
    }

    @Override
    public ResourceLocation getAnimationResource(MeimiruEntity animatable) {
        return ANIMATION;
    }
}
