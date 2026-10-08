package com.mimiworld.mimiworld.client.model;

import com.mimiworld.mimiworld.MimiWorld;
import com.mimiworld.mimiworld.entity.HualingmiEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

/**
 * 化灵米（Hualing Rice）的 GeckoLib 模型绑定类。
 *
 * <p>只负责告诉 GeckoLib 三个资源文件的位置：</p>
 * <ul>
 *   <li>模型：assets/mimiworld/geo/hualingmi.geo.json（Blockbench 导出，geometry.hualingmi，
 *       16 骨骼 38 方块：米白长袍小人身 + 稻穗耳/额发 + 稻叶双翼与翼尖 + 幽灵尾尖）</li>
 *   <li>贴图：assets/mimiworld/textures/entity/hualingmi.png（64×64，大米飞灵配色）</li>
 *   <li>动画：assets/mimiworld/animations/hualingmi.animation.json
 *       （animation.hualingmi.idle / .fly / .attack）</li>
 * </ul>
 * 动画名与 HualingmiEntity 中 RawAnimation 引用的键名严格一致。
 */
public class HualingmiModel extends GeoModel<HualingmiEntity> {

    private static final ResourceLocation MODEL =
            new ResourceLocation(MimiWorld.MODID, "geo/hualingmi.geo.json");
    private static final ResourceLocation TEXTURE =
            new ResourceLocation(MimiWorld.MODID, "textures/entity/hualingmi.png");
    private static final ResourceLocation ANIMATION =
            new ResourceLocation(MimiWorld.MODID, "animations/hualingmi.animation.json");

    @Override
    public ResourceLocation getModelResource(HualingmiEntity animatable) {
        return MODEL;
    }

    @Override
    public ResourceLocation getTextureResource(HualingmiEntity animatable) {
        return TEXTURE;
    }

    @Override
    public ResourceLocation getAnimationResource(HualingmiEntity animatable) {
        return ANIMATION;
    }
}
