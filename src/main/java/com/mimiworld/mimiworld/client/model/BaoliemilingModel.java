package com.mimiworld.mimiworld.client.model;

import com.mimiworld.mimiworld.MimiWorld;
import com.mimiworld.mimiworld.entity.BaoliemilingEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

/**
 * 爆烈米灵（Explosive Rice Spirit）的 GeckoLib 模型绑定类。
 *
 * <p>资源位置：</p>
 * <ul>
 *   <li>模型：assets/mimiworld/geo/baoliemiling.geo.json（geometry.baoliemiling，
 *       root/head/core/rods(12)/embers(4) 共 21 骨骼 39 方块）</li>
 *   <li>贴图：assets/mimiworld/textures/entity/baoliemiling.png（64×64，白米怒脸+金米棒+爆裂火红配色）</li>
 *   <li>动画：assets/mimiworld/animations/baoliemiling.animation.json
 *       （animation.baoliemiling.idle / .walk / .attack）</li>
 * </ul>
 * 动画键名与 BaoliemilingEntity 中 RawAnimation 引用严格一致。
 */
public class BaoliemilingModel extends GeoModel<BaoliemilingEntity> {

    private static final ResourceLocation MODEL =
            new ResourceLocation(MimiWorld.MODID, "geo/baoliemiling.geo.json");
    private static final ResourceLocation TEXTURE =
            new ResourceLocation(MimiWorld.MODID, "textures/entity/baoliemiling.png");
    private static final ResourceLocation ANIMATION =
            new ResourceLocation(MimiWorld.MODID, "animations/baoliemiling.animation.json");

    @Override
    public ResourceLocation getModelResource(BaoliemilingEntity animatable) {
        return MODEL;
    }

    @Override
    public ResourceLocation getTextureResource(BaoliemilingEntity animatable) {
        return TEXTURE;
    }

    @Override
    public ResourceLocation getAnimationResource(BaoliemilingEntity animatable) {
        return ANIMATION;
    }
}
