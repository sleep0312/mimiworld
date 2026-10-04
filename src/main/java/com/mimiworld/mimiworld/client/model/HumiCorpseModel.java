package com.mimiworld.mimiworld.client.model;

import com.mimiworld.mimiworld.MimiWorld;
import com.mimiworld.mimiworld.entity.HumiCorpseEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

/**
 * 糊米古尸（Humi Corpse）的 GeckoLib 模型绑定类。
 *
 * <p>只负责告诉 GeckoLib 三个资源文件的位置：</p>
 * <ul>
 *   <li>模型：assets/mimiworld/geo/humi_corpse.geo.json（Blockbench 导出，7 根骨骼：
 *       root/body/head/rightArm/leftArm/rightLeg/leftLeg，84 方块，双臂前伸 90°）</li>
 *   <li>贴图：assets/mimiworld/textures/entity/humi_corpse.png（128×128，大米古尸配色）</li>
 *   <li>动画：assets/mimiworld/animations/humi_corpse.animation.json
 *       （animation.idle / animation.walk / animation.attack）</li>
 * </ul>
 * 动画名与 HumiCorpseEntity 中 RawAnimation 引用的键名严格一致。
 */
public class HumiCorpseModel extends GeoModel<HumiCorpseEntity> {

    private static final ResourceLocation MODEL =
            new ResourceLocation(MimiWorld.MODID, "geo/humi_corpse.geo.json");
    private static final ResourceLocation TEXTURE =
            new ResourceLocation(MimiWorld.MODID, "textures/entity/humi_corpse.png");
    private static final ResourceLocation ANIMATION =
            new ResourceLocation(MimiWorld.MODID, "animations/humi_corpse.animation.json");

    @Override
    public ResourceLocation getModelResource(HumiCorpseEntity animatable) {
        return MODEL;
    }

    @Override
    public ResourceLocation getTextureResource(HumiCorpseEntity animatable) {
        return TEXTURE;
    }

    @Override
    public ResourceLocation getAnimationResource(HumiCorpseEntity animatable) {
        return ANIMATION;
    }
}
