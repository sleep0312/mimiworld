package com.mimiworld.mimiworld.client.model;

import com.mimiworld.mimiworld.MimiWorld;
import com.mimiworld.mimiworld.entity.ZhuomiChickenEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

/**
 * 啄米鸡（Zhuomi Chicken）的 GeckoLib 模型绑定类。
 *
 * <p>只负责告诉 GeckoLib 三个资源文件的位置：</p>
 * <ul>
 *   <li>模型：assets/mimiworld/geo/zhuomi_chicken.geo.json（Blockbench 导出，8 根骨骼：
 *       root/body/head/left_wing/right_wing/tail/left_leg/right_leg）</li>
 *   <li>贴图：assets/mimiworld/textures/entity/zhuomi_chicken.png（128×128，大米主题配色）</li>
 *   <li>动画：assets/mimiworld/animations/zhuomi_chicken.animation.json（animation.idle / animation.walk）</li>
 * </ul>
 * 动画名与 ZhuomiChickenEntity 中 RawAnimation 引用的键名严格一致。
 */
public class ZhuomiChickenModel extends GeoModel<ZhuomiChickenEntity> {

    private static final ResourceLocation MODEL =
            new ResourceLocation(MimiWorld.MODID, "geo/zhuomi_chicken.geo.json");
    private static final ResourceLocation TEXTURE =
            new ResourceLocation(MimiWorld.MODID, "textures/entity/zhuomi_chicken.png");
    private static final ResourceLocation ANIMATION =
            new ResourceLocation(MimiWorld.MODID, "animations/zhuomi_chicken.animation.json");

    @Override
    public ResourceLocation getModelResource(ZhuomiChickenEntity animatable) {
        return MODEL;
    }

    @Override
    public ResourceLocation getTextureResource(ZhuomiChickenEntity animatable) {
        return TEXTURE;
    }

    @Override
    public ResourceLocation getAnimationResource(ZhuomiChickenEntity animatable) {
        return ANIMATION;
    }
}
