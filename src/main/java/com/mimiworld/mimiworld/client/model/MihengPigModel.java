package com.mimiworld.mimiworld.client.model;

import com.mimiworld.mimiworld.MimiWorld;
import com.mimiworld.mimiworld.entity.MihengPigEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

/**
 * 米哼猪（MiHeng Pig）的 GeckoLib 模型绑定类。
 *
 * <p>只负责告诉 GeckoLib 三个资源文件的位置：</p>
 * <ul>
 *   <li>模型：assets/mimiworld/geo/mihengzhu.geo.json（Blockbench 导出，8 根骨骼：
 *       root/body/head/leg0~3/tail，30 个方块）</li>
 *   <li>贴图：assets/mimiworld/textures/entity/mihengzhu.png（128×128，大米主题：
 *       米白毛色、米粉腮红、稻黄脊斑、稻芽绿卷尾）</li>
 *   <li>动画：assets/mimiworld/animations/mihengzhu.animation.json
 *       （animation.mihengzhu.idle / animation.mihengzhu.walk）</li>
 * </ul>
 * 动画名与 {@link MihengPigEntity} 中 RawAnimation 引用的键名严格一致。
 */
public class MihengPigModel extends GeoModel<MihengPigEntity> {

    private static final ResourceLocation MODEL =
            new ResourceLocation(MimiWorld.MODID, "geo/mihengzhu.geo.json");
    private static final ResourceLocation TEXTURE =
            new ResourceLocation(MimiWorld.MODID, "textures/entity/mihengzhu.png");
    private static final ResourceLocation ANIMATION =
            new ResourceLocation(MimiWorld.MODID, "animations/mihengzhu.animation.json");

    @Override
    public ResourceLocation getModelResource(MihengPigEntity animatable) {
        return MODEL;
    }

    @Override
    public ResourceLocation getTextureResource(MihengPigEntity animatable) {
        return TEXTURE;
    }

    @Override
    public ResourceLocation getAnimationResource(MihengPigEntity animatable) {
        return ANIMATION;
    }
}
