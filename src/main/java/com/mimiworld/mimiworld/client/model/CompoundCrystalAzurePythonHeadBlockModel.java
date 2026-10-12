package com.mimiworld.mimiworld.client.model;

import com.mimiworld.mimiworld.MimiWorld;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntity;
import software.bernie.geckolib.core.animatable.GeoAnimatable;
import software.bernie.geckolib.model.GeoModel;

/**
 * 复晶天青蟒头摆件方块的 GeckoLib 模型绑定类。
 *
 * <ul>
 *   <li>模型：geo/block/compound_crystal_azure_python_head.geo.json
 *       （geometry.compound_crystal_azure_python_head：head + jaw + tongue + horn 共 17 方块）</li>
 *   <li>贴图：textures/geckolib/compound_crystal_azure_python_head.png（与天青蟒本体同源 256×256）</li>
 *   <li>动画：cooking_pots.animation.json（空动画文件，静态渲染）</li>
 * </ul>
 */
public class CompoundCrystalAzurePythonHeadBlockModel<T extends BlockEntity & GeoAnimatable> extends GeoModel<T> {

    private static final ResourceLocation MODEL =
            new ResourceLocation(MimiWorld.MODID, "geo/block/compound_crystal_azure_python_head.geo.json");
    private static final ResourceLocation TEXTURE =
            new ResourceLocation(MimiWorld.MODID, "textures/geckolib/compound_crystal_azure_python_head.png");
    private static final ResourceLocation ANIMATION =
            new ResourceLocation(MimiWorld.MODID, "animations/cooking_pots.animation.json");

    @Override
    public ResourceLocation getModelResource(T animatable) {
        return MODEL;
    }

    @Override
    public ResourceLocation getTextureResource(T animatable) {
        return TEXTURE;
    }

    @Override
    public ResourceLocation getAnimationResource(T animatable) {
        return ANIMATION;
    }
}
