package com.mimiworld.mimiworld.client.model;

import com.mimiworld.mimiworld.MimiWorld;
import com.mimiworld.mimiworld.entity.MimouniuEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

/**
 * 米哞牛（MiMoo Cow）的 GeckoLib 模型绑定类。
 *
 * <p>只负责告诉 GeckoLib 三个资源文件的位置：</p>
 * <ul>
 *   <li>模型：assets/mimiworld/geo/mimouniu.geo.json（Blockbench 导出，7 根骨骼：
 *       root/body/head/leg0~3/tail，33 个方块）</li>
 *   <li>贴图：assets/mimiworld/textures/entity/mimouniu.png（128×128，大米主题：
 *       米白毛色、米色谷粒斑块、稻绿尾巴）</li>
 *   <li>动画：assets/mimiworld/animations/mimouniu.animation.json
 *       （animation.mimouniu.idle / animation.mimouniu.walk）</li>
 * </ul>
 * 动画名与 {@link MimouniuEntity} 中 RawAnimation 引用的键名严格一致。
 */
public class MimouniuModel extends GeoModel<MimouniuEntity> {

    private static final ResourceLocation MODEL =
            new ResourceLocation(MimiWorld.MODID, "geo/mimouniu.geo.json");
    private static final ResourceLocation TEXTURE =
            new ResourceLocation(MimiWorld.MODID, "textures/entity/mimouniu.png");
    private static final ResourceLocation ANIMATION =
            new ResourceLocation(MimiWorld.MODID, "animations/mimouniu.animation.json");

    @Override
    public ResourceLocation getModelResource(MimouniuEntity animatable) {
        return MODEL;
    }

    @Override
    public ResourceLocation getTextureResource(MimouniuEntity animatable) {
        return TEXTURE;
    }

    @Override
    public ResourceLocation getAnimationResource(MimouniuEntity animatable) {
        return ANIMATION;
    }
}
