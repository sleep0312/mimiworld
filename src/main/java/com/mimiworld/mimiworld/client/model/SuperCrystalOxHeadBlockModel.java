package com.mimiworld.mimiworld.client.model;

import com.mimiworld.mimiworld.MimiWorld;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntity;
import software.bernie.geckolib.core.animatable.GeoAnimatable;
import software.bernie.geckolib.model.GeoModel;

/**
 * 超晶牛头摆件方块的 GeckoLib 模型绑定类。
 *
 * <ul>
 *   <li>模型：geo/block/super_crystal_ox_head.geo.json
 *       （geometry.super_crystal_ox_head：head + 双角 + 双耳共 12 方块）</li>
 *   <li>贴图：textures/geckolib/super_crystal_ox_head.png（与牛王本体同源 256×256）</li>
 *   <li>动画：cooking_pots.animation.json（空动画文件，静态渲染）</li>
 * </ul>
 */
public class SuperCrystalOxHeadBlockModel<T extends BlockEntity & GeoAnimatable> extends GeoModel<T> {

    private static final ResourceLocation MODEL =
            new ResourceLocation(MimiWorld.MODID, "geo/block/super_crystal_ox_head.geo.json");
    private static final ResourceLocation TEXTURE =
            new ResourceLocation(MimiWorld.MODID, "textures/geckolib/super_crystal_ox_head.png");
    private static final ResourceLocation ANIMATION =
            new ResourceLocation(MimiWorld.MODID, "animations/cooking_pots.animation.json");

    @Override
    public ResourceLocation getModelResource(T animatable) {
        return MODEL;
    }

    @Override
    public ResourceLocation getTextureResource(T animatable) {
        return TEXTURE;
    }

    @Override
    public ResourceLocation getAnimationResource(T animatable) {
        return ANIMATION;
    }
}
