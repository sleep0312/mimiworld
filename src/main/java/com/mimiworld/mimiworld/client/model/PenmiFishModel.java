package com.mimiworld.mimiworld.client.model;

import com.mimiworld.mimiworld.MimiWorld;
import com.mimiworld.mimiworld.entity.PenmiFishEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

/**
 * 喷米鱼（Penmi Fish）的 GeckoLib 模型绑定类。
 *
 * <p>只负责告诉 GeckoLib 三个资源文件的位置：</p>
 * <ul>
 *   <li>模型：assets/mimiworld/geo/penmiyu.geo.json（root → body → tail，
 *       body → 双侧胸鳍）</li>
 *   <li>贴图：assets/mimiworld/textures/entity/penmiyu.png（64×16，大米主题）</li>
 *   <li>动画：assets/mimiworld/animations/penmiyu.animation.json
 *       （animation.penmiyu.swim / animation.penmiyu.idle）</li>
 * </ul>
 * 动画名与 {@link PenmiFishEntity} 中 RawAnimation 引用的键名严格一致。
 */
public class PenmiFishModel extends GeoModel<PenmiFishEntity> {

    private static final ResourceLocation MODEL =
            new ResourceLocation(MimiWorld.MODID, "geo/penmiyu.geo.json");
    private static final ResourceLocation TEXTURE =
            new ResourceLocation(MimiWorld.MODID, "textures/entity/penmiyu.png");
    private static final ResourceLocation ANIMATION =
            new ResourceLocation(MimiWorld.MODID, "animations/penmiyu.animation.json");

    @Override
    public ResourceLocation getModelResource(PenmiFishEntity animatable) {
        return MODEL;
    }

    @Override
    public ResourceLocation getTextureResource(PenmiFishEntity animatable) {
        return TEXTURE;
    }

    @Override
    public ResourceLocation getAnimationResource(PenmiFishEntity animatable) {
        return ANIMATION;
    }
}
