package com.mimiworld.mimiworld.client.model;

import com.mimiworld.mimiworld.MimiWorld;
import com.mimiworld.mimiworld.entity.LaziestFinalDragonEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

/**
 * 最懒终末龙（宗罪龙模型）的 GeckoLib 模型绑定类。
 *
 * <p>三个资源文件：</p>
 * <ul>
 *   <li>模型：assets/mimiworld/geo/laziest_final_dragon.geo.json（Blockbench 1.12.0，飞龙骨架）</li>
 *   <li>贴图：assets/mimiworld/textures/entity/laziest_final_dragon.png（256×256 暗紫鳞片）</li>
 *   <li>动画：assets/mimiworld/animations/laziest_final_dragon.animation.json
 *       （animation.idle 待机 / animation.flying 飞行，均循环；无攻击动画）</li>
 * </ul>
 */
public class LaziestFinalDragonModel extends GeoModel<LaziestFinalDragonEntity> {

    private static final ResourceLocation MODEL =
            new ResourceLocation(MimiWorld.MODID, "geo/laziest_final_dragon.geo.json");
    private static final ResourceLocation TEXTURE =
            new ResourceLocation(MimiWorld.MODID, "textures/entity/laziest_final_dragon.png");
    private static final ResourceLocation ANIMATION =
            new ResourceLocation(MimiWorld.MODID, "animations/laziest_final_dragon.animation.json");

    @Override
    public ResourceLocation getModelResource(LaziestFinalDragonEntity animatable) {
        return MODEL;
    }

    @Override
    public ResourceLocation getTextureResource(LaziestFinalDragonEntity animatable) {
        return TEXTURE;
    }

    @Override
    public ResourceLocation getAnimationResource(LaziestFinalDragonEntity animatable) {
        return ANIMATION;
    }
}
