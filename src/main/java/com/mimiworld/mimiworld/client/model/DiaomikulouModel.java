package com.mimiworld.mimiworld.client.model;

import com.mimiworld.mimiworld.MimiWorld;
import com.mimiworld.mimiworld.entity.DiaomikulouEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

/**
 * 雕米骷髅（Carved Rice Skeleton）的 GeckoLib 模型绑定类。
 *
 * <p>只负责告诉 GeckoLib 三个资源文件的位置：</p>
 * <ul>
 *   <li>模型：assets/mimiworld/geo/diaomikulou.geo.json（Blockbench 导出，7 根骨骼：
 *       root/body/head/rightArm/leftArm/rightLeg/leftLeg，80 方块）</li>
 *   <li>贴图：assets/mimiworld/textures/entity/diaomikulou.png（128×128，大米主题骨体配色）</li>
 *   <li>动画：assets/mimiworld/animations/diaomikulou.animation.json
 *       （animation.diaomikulou.idle / walk / attack）</li>
 * </ul>
 * 动画名与 DiaomikulouEntity 中 RawAnimation 引用的键名严格一致。
 */
public class DiaomikulouModel extends GeoModel<DiaomikulouEntity> {

    private static final ResourceLocation MODEL =
            new ResourceLocation(MimiWorld.MODID, "geo/diaomikulou.geo.json");
    private static final ResourceLocation TEXTURE =
            new ResourceLocation(MimiWorld.MODID, "textures/entity/diaomikulou.png");
    private static final ResourceLocation ANIMATION =
            new ResourceLocation(MimiWorld.MODID, "animations/diaomikulou.animation.json");

    @Override
    public ResourceLocation getModelResource(DiaomikulouEntity animatable) {
        return MODEL;
    }

    @Override
    public ResourceLocation getTextureResource(DiaomikulouEntity animatable) {
        return TEXTURE;
    }

    @Override
    public ResourceLocation getAnimationResource(DiaomikulouEntity animatable) {
        return ANIMATION;
    }
}
