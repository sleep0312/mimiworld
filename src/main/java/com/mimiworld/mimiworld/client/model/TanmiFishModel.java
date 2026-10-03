package com.mimiworld.mimiworld.client.model;

import com.mimiworld.mimiworld.MimiWorld;
import com.mimiworld.mimiworld.entity.TanmiFishEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

/**
 * 贪米鱼（Tanmi Fish）的 GeckoLib 模型绑定类。
 *
 * <p>只负责告诉 GeckoLib 三个资源文件的位置：</p>
 * <ul>
 *   <li>模型：assets/mimiworld/geo/tanmiyu.geo.json（鳕鱼式水栖造型，
 *       root → head/body，body → 双侧胸鳍/尾柄/尾鳍）</li>
 *   <li>贴图：assets/mimiworld/textures/entity/tanmiyu.png（64×32，大米主题：
 *       金背、暖米身、白米粒、稻芽绿鳍）</li>
 *   <li>动画：assets/mimiworld/animations/tanmiyu.animation.json
 *       （animation.tanmiyu.swim / animation.tanmiyu.idle）</li>
 * </ul>
 * 动画名与 {@link TanmiFishEntity} 中 RawAnimation 引用的键名严格一致。
 */
public class TanmiFishModel extends GeoModel<TanmiFishEntity> {

    private static final ResourceLocation MODEL =
            new ResourceLocation(MimiWorld.MODID, "geo/tanmiyu.geo.json");
    private static final ResourceLocation TEXTURE =
            new ResourceLocation(MimiWorld.MODID, "textures/entity/tanmiyu.png");
    private static final ResourceLocation ANIMATION =
            new ResourceLocation(MimiWorld.MODID, "animations/tanmiyu.animation.json");

    @Override
    public ResourceLocation getModelResource(TanmiFishEntity animatable) {
        return MODEL;
    }

    @Override
    public ResourceLocation getTextureResource(TanmiFishEntity animatable) {
        return TEXTURE;
    }

    @Override
    public ResourceLocation getAnimationResource(TanmiFishEntity animatable) {
        return ANIMATION;
    }
}
