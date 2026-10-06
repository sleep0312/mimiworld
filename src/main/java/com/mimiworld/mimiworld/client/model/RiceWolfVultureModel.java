package com.mimiworld.mimiworld.client.model;

import com.mimiworld.mimiworld.MimiWorld;
import com.mimiworld.mimiworld.entity.RiceWolfVultureEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

/**
 * 哮噬米狼鹫（Rice Wolf-Vulture）的 GeckoLib 模型绑定类。
 *
 * <p>只负责告诉 GeckoLib 三个资源文件的位置：</p>
 * <ul>
 *   <li>模型：assets/mimiworld/geo/rice_wolf_vulture.geo.json（Blockbench 导出，
 *       15 根骨骼 181 方块，identifier geometry.xiaoshimilangjiu，脚底贴 y=0）</li>
 *   <li>贴图：assets/mimiworld/textures/entity/rice_wolf_vulture.png（4096×688，米白狼毛+金绿稻羽+金竖瞳）</li>
 *   <li>动画：assets/mimiworld/animations/rice_wolf_vulture.animation.json
 *       （animation.xiaoshimilangjiu.idle / .walk / .attack_bite / .attack_dive）</li>
 * </ul>
 * 动画名与 RiceWolfVultureEntity 中 RawAnimation 引用的键名严格一致。
 */
public class RiceWolfVultureModel extends GeoModel<RiceWolfVultureEntity> {

    private static final ResourceLocation MODEL =
            new ResourceLocation(MimiWorld.MODID, "geo/rice_wolf_vulture.geo.json");
    private static final ResourceLocation TEXTURE =
            new ResourceLocation(MimiWorld.MODID, "textures/entity/rice_wolf_vulture.png");
    private static final ResourceLocation ANIMATION =
            new ResourceLocation(MimiWorld.MODID, "animations/rice_wolf_vulture.animation.json");

    @Override
    public ResourceLocation getModelResource(RiceWolfVultureEntity animatable) {
        return MODEL;
    }

    @Override
    public ResourceLocation getTextureResource(RiceWolfVultureEntity animatable) {
        return TEXTURE;
    }

    @Override
    public ResourceLocation getAnimationResource(RiceWolfVultureEntity animatable) {
        return ANIMATION;
    }
}
