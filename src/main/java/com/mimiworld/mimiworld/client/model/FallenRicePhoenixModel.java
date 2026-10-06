package com.mimiworld.mimiworld.client.model;

import com.mimiworld.mimiworld.MimiWorld;
import com.mimiworld.mimiworld.entity.FallenRicePhoenixEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

/**
 * 堕落米凤（Fallen Rice Phoenix）的 GeckoLib 模型绑定类。
 *
 * <p>只负责告诉 GeckoLib 三个资源文件的位置：</p>
 * <ul>
 *   <li>模型：assets/mimiworld/geo/fallen_rice_phoenix.geo.json（Blockbench 导出，
 *       identifier geometry.duoluomifeng，直立脚底贴 y=0、冠顶约 43px 高）</li>
 *   <li>贴图：assets/mimiworld/textures/entity/fallen_rice_phoenix.png（256×256，米白堕落羽+黑翅尖+金冠）</li>
 *   <li>动画：assets/mimiworld/animations/fallen_rice_phoenix.animation.json
 *       （animation.idle / .walk / .attack / .flying）</li>
 * </ul>
 * 动画名与 FallenRicePhoenixEntity 中 RawAnimation 引用的键名严格一致。
 */
public class FallenRicePhoenixModel extends GeoModel<FallenRicePhoenixEntity> {

    private static final ResourceLocation MODEL =
            new ResourceLocation(MimiWorld.MODID, "geo/fallen_rice_phoenix.geo.json");
    private static final ResourceLocation TEXTURE =
            new ResourceLocation(MimiWorld.MODID, "textures/entity/fallen_rice_phoenix.png");
    private static final ResourceLocation ANIMATION =
            new ResourceLocation(MimiWorld.MODID, "animations/fallen_rice_phoenix.animation.json");

    @Override
    public ResourceLocation getModelResource(FallenRicePhoenixEntity animatable) {
        return MODEL;
    }

    @Override
    public ResourceLocation getTextureResource(FallenRicePhoenixEntity animatable) {
        return TEXTURE;
    }

    @Override
    public ResourceLocation getAnimationResource(FallenRicePhoenixEntity animatable) {
        return ANIMATION;
    }
}
