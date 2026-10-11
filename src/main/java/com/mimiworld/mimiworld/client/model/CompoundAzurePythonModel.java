package com.mimiworld.mimiworld.client.model;

import com.mimiworld.mimiworld.MimiWorld;
import com.mimiworld.mimiworld.entity.CompoundAzurePythonEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

/**
 * 复晶天青蟒的 GeckoLib 模型绑定类。
 *
 * <p>资源（Blockbench 导出）：</p>
 * <ul>
 *   <li>模型：geo/compound_azure_python.geo.json（18 骨骼：root/tail1-3/body1-6/neck1-3/
 *       hood/head/jaw/horn/tongue，58 方块；jaw 为独立颌骨以播放撕咬张颌动画，
 *       tail1-3 为独立尾骨以播放扫尾动画）</li>
 *   <li>贴图：textures/entity/compound_azure_python.png（256×256，深玉蟒鳞+米白腹甲+
 *       青碧复晶+金边兜帽眼纹，大米主题）</li>
 *   <li>动画：animations/compound_azure_python.animation.json
 *       （animation.compound_azure_python.idle / walk / attack / bite / kiss / tail_sweep）</li>
 * </ul>
 */
public class CompoundAzurePythonModel extends GeoModel<CompoundAzurePythonEntity> {

    private static final ResourceLocation MODEL =
            new ResourceLocation(MimiWorld.MODID, "geo/compound_azure_python.geo.json");
    private static final ResourceLocation TEXTURE =
            new ResourceLocation(MimiWorld.MODID, "textures/entity/compound_azure_python.png");
    private static final ResourceLocation ANIMATION =
            new ResourceLocation(MimiWorld.MODID, "animations/compound_azure_python.animation.json");

    @Override
    public ResourceLocation getModelResource(CompoundAzurePythonEntity animatable) {
        return MODEL;
    }

    @Override
    public ResourceLocation getTextureResource(CompoundAzurePythonEntity animatable) {
        return TEXTURE;
    }

    @Override
    public ResourceLocation getAnimationResource(CompoundAzurePythonEntity animatable) {
        return ANIMATION;
    }
}
