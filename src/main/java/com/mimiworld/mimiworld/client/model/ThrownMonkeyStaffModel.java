package com.mimiworld.mimiworld.client.model;

import com.mimiworld.mimiworld.MimiWorld;
import com.mimiworld.mimiworld.entity.ThrownMonkeyStaffEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

/**
 * 扔出的金箍棒投掷物 GeckoLib 模型绑定。
 *
 * <p>模型 geo/mimihouwang_staff.geo.json：只含一根以原点为中心、沿 Z 轴躺平的金箍棒
 * （从 mimihouwang.geo.json 的 staff 骨骼重定位而来，UV 完全一致），
 * 直接复用猴王贴图 textures/entity/mimihouwang.png；无动画文件，棒体朝向由渲染器
 * 按投掷物 yaw 旋转对齐飞行方向。</p>
 */
public class ThrownMonkeyStaffModel extends GeoModel<ThrownMonkeyStaffEntity> {

    private static final ResourceLocation MODEL =
            new ResourceLocation(MimiWorld.MODID, "geo/mimihouwang_staff.geo.json");
    private static final ResourceLocation TEXTURE =
            new ResourceLocation(MimiWorld.MODID, "textures/entity/mimihouwang.png");
    private static final ResourceLocation ANIMATION =
            new ResourceLocation(MimiWorld.MODID, "animations/mimihouwang.animation.json");

    @Override
    public ResourceLocation getModelResource(ThrownMonkeyStaffEntity animatable) {
        return MODEL;
    }

    @Override
    public ResourceLocation getTextureResource(ThrownMonkeyStaffEntity animatable) {
        return TEXTURE;
    }

    @Override
    public ResourceLocation getAnimationResource(ThrownMonkeyStaffEntity animatable) {
        return ANIMATION;
    }
}
