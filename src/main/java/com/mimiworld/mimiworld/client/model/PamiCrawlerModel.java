package com.mimiworld.mimiworld.client.model;

import com.mimiworld.mimiworld.MimiWorld;
import com.mimiworld.mimiworld.entity.PamiCrawlerEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

/**
 * 爪巴米者（Pami Crawler）的 GeckoLib 模型绑定类。
 *
 * <p>只负责告诉 GeckoLib 三个资源文件的位置：</p>
 * <ul>
 *   <li>模型：assets/mimiworld/geo/pami_crawler.geo.json（Blockbench 导出，7 根骨骼：
 *       root/body/head/legFL/legFR/legBL/legBR，30 方块，Q 版四足麻袋苦力怕）</li>
 *   <li>贴图：assets/mimiworld/textures/entity/pami_crawler.png（128×128，旧米麻袋配色）</li>
 *   <li>动画：assets/mimiworld/animations/pami_crawler.animation.json
 *       （animation.idle / animation.walk / animation.attack）</li>
 * </ul>
 * 动画名与 PamiCrawlerEntity 中 RawAnimation 引用的键名严格一致。
 */
public class PamiCrawlerModel extends GeoModel<PamiCrawlerEntity> {

    private static final ResourceLocation MODEL =
            new ResourceLocation(MimiWorld.MODID, "geo/pami_crawler.geo.json");
    private static final ResourceLocation TEXTURE =
            new ResourceLocation(MimiWorld.MODID, "textures/entity/pami_crawler.png");
    private static final ResourceLocation ANIMATION =
            new ResourceLocation(MimiWorld.MODID, "animations/pami_crawler.animation.json");

    @Override
    public ResourceLocation getModelResource(PamiCrawlerEntity animatable) {
        return MODEL;
    }

    @Override
    public ResourceLocation getTextureResource(PamiCrawlerEntity animatable) {
        return TEXTURE;
    }

    @Override
    public ResourceLocation getAnimationResource(PamiCrawlerEntity animatable) {
        return ANIMATION;
    }
}
