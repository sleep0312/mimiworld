package com.mimiworld.mimiworld.client.model;

import com.mimiworld.mimiworld.MimiWorld;
import com.mimiworld.mimiworld.entity.MimiSheepEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

/**
 * 米羊羊（Mimi Sheep）的 GeckoLib 模型绑定类。
 *
 * <p>只负责告诉 GeckoLib 三个资源文件的位置：</p>
 * <ul>
 *   <li>模型：assets/mimiworld/geo/mimi_sheep.geo.json（Blockbench 导出，8 根骨骼：
 *       head/body/leg1~4 加上羊毛层 head_wool/body_wool）</li>
 *   <li>贴图：assets/mimiworld/textures/entity/mimi_sheep.png（128×128）</li>
 *   <li>动画：assets/mimiworld/animations/mimi_sheep.animation.json（idle / walk / scared_run）</li>
 * </ul>
 * 动画名与 MimiSheepEntity 中 RawAnimation 引用的裸名严格一致。
 */
public class MimiSheepModel extends GeoModel<MimiSheepEntity> {

    private static final ResourceLocation MODEL =
            new ResourceLocation(MimiWorld.MODID, "geo/mimi_sheep.geo.json");
    private static final ResourceLocation TEXTURE =
            new ResourceLocation(MimiWorld.MODID, "textures/entity/mimi_sheep.png");
    private static final ResourceLocation ANIMATION =
            new ResourceLocation(MimiWorld.MODID, "animations/mimi_sheep.animation.json");

    @Override
    public ResourceLocation getModelResource(MimiSheepEntity animatable) {
        return MODEL;
    }

    @Override
    public ResourceLocation getTextureResource(MimiSheepEntity animatable) {
        return TEXTURE;
    }

    @Override
    public ResourceLocation getAnimationResource(MimiSheepEntity animatable) {
        return ANIMATION;
    }
}
