package com.mimiworld.mimiworld.client.model;

import com.mimiworld.mimiworld.MimiWorld;
import com.mimiworld.mimiworld.entity.DibazongzuilongEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

/**
 * 第八宗罪龙（最终 Boss）的 GeckoLib 模型绑定类。
 *
 * <p>三个资源文件：</p>
 * <ul>
 *   <li>模型：assets/mimiworld/geo/dibazongzuilong.geo.json（Blockbench 1.12.0，451 方块/32 骨骼，翼展 919.5px）</li>
 *   <li>贴图：assets/mimiworld/textures/entity/dibazongzuilong.png（2048×2048 残堕精华红主题）</li>
 *   <li>动画：assets/mimiworld/animations/dibazongzuilong.animation.json
 *       （animation.circling 盘旋 / dive_strike 冲撞 / dragon_breath 龙息 /
 *       laser_beam 光束 / melee_bite 撕咬 / hurt_roar 受伤嘶吼）</li>
 * </ul>
 */
public class DibazongzuilongModel extends GeoModel<DibazongzuilongEntity> {

    private static final ResourceLocation MODEL =
            new ResourceLocation(MimiWorld.MODID, "geo/dibazongzuilong.geo.json");
    private static final ResourceLocation TEXTURE =
            new ResourceLocation(MimiWorld.MODID, "textures/entity/dibazongzuilong.png");
    private static final ResourceLocation ANIMATION =
            new ResourceLocation(MimiWorld.MODID, "animations/dibazongzuilong.animation.json");

    @Override
    public ResourceLocation getModelResource(DibazongzuilongEntity animatable) {
        return MODEL;
    }

    @Override
    public ResourceLocation getTextureResource(DibazongzuilongEntity animatable) {
        return TEXTURE;
    }

    @Override
    public ResourceLocation getAnimationResource(DibazongzuilongEntity animatable) {
        return ANIMATION;
    }
}
