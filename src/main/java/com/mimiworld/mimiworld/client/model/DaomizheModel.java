package com.mimiworld.mimiworld.client.model;

import com.mimiworld.mimiworld.MimiWorld;
import com.mimiworld.mimiworld.entity.DaomizheEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

/**
 * 盗米者（Rice Stealer）的 GeckoLib 模型绑定类。
 *
 * <p>只负责告诉 GeckoLib 三个资源文件的位置：</p>
 * <ul>
 *   <li>模型：assets/mimiworld/geo/daomizhe.geo.json（geometry.daomizhe，狼人意象蹲踞人形）</li>
 *   <li>贴图：assets/mimiworld/textures/entity/daomizhe.png（128×128，
 *       米白绒毛+稻叶绿背脊尖刺+稻穗金鬃毛/尾尖+琥珀怒目的大米主题）</li>
 *   <li>动画：assets/mimiworld/animations/daomizhe.animation.json
 *       （animation.daomizhe.idle / .walk / .attack）</li>
 * </ul>
 * 动画名与 DaomizheEntity 中 RawAnimation 引用的键名严格一致。
 */
public class DaomizheModel extends GeoModel<DaomizheEntity> {

    private static final ResourceLocation MODEL =
            new ResourceLocation(MimiWorld.MODID, "geo/daomizhe.geo.json");
    private static final ResourceLocation TEXTURE =
            new ResourceLocation(MimiWorld.MODID, "textures/entity/daomizhe.png");
    private static final ResourceLocation ANIMATION =
            new ResourceLocation(MimiWorld.MODID, "animations/daomizhe.animation.json");

    @Override
    public ResourceLocation getModelResource(DaomizheEntity animatable) {
        return MODEL;
    }

    @Override
    public ResourceLocation getTextureResource(DaomizheEntity animatable) {
        return TEXTURE;
    }

    @Override
    public ResourceLocation getAnimationResource(DaomizheEntity animatable) {
        return ANIMATION;
    }
}
