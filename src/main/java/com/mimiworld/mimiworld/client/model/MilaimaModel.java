package com.mimiworld.mimiworld.client.model;

import com.mimiworld.mimiworld.MimiWorld;
import com.mimiworld.mimiworld.entity.MilaimaEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

/**
 * 米莱马（Milaima）的 GeckoLib 模型绑定类。
 *
 * <p>只负责告诉 GeckoLib 三个资源文件的位置：</p>
 * <ul>
 *   <li>模型：assets/mimiworld/geo/milaima.geo.json（Blockbench 导出，3 根骨骼：
 *       root/body/sprout，38 方块圆墩凝胶造型，含液裙/软手/大眼/笑口/头顶嫩芽）</li>
 *   <li>贴图：assets/mimiworld/textures/entity/milaima.png（64×64，暖白米浆+悬浮米粒、
 *       酱油色大眼+米白高光、梅粉腮红、稻绿嫩芽的大米主题）</li>
 *   <li>动画：assets/mimiworld/animations/milaima.animation.json
 *       （animation.milaima.idle / walk / attack）</li>
 * </ul>
 * 动画名与 MilaimaEntity 中 RawAnimation 引用的键名严格一致。
 */
public class MilaimaModel extends GeoModel<MilaimaEntity> {

    private static final ResourceLocation MODEL =
            new ResourceLocation(MimiWorld.MODID, "geo/milaima.geo.json");
    private static final ResourceLocation TEXTURE =
            new ResourceLocation(MimiWorld.MODID, "textures/entity/milaima.png");
    private static final ResourceLocation ANIMATION =
            new ResourceLocation(MimiWorld.MODID, "animations/milaima.animation.json");

    @Override
    public ResourceLocation getModelResource(MilaimaEntity animatable) {
        return MODEL;
    }

    @Override
    public ResourceLocation getTextureResource(MilaimaEntity animatable) {
        return TEXTURE;
    }

    @Override
    public ResourceLocation getAnimationResource(MilaimaEntity animatable) {
        return ANIMATION;
    }
}
