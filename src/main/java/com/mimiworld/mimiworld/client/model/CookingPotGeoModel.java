package com.mimiworld.mimiworld.client.model;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntity;
import software.bernie.geckolib.core.animatable.GeoAnimatable;
import software.bernie.geckolib.model.GeoModel;

/**
 * 煮锅系列方块的 GeckoLib 模型绑定类（五种煮锅共用一个泛型类）。
 *
 * <p>按注册 id 精确指定三个资源文件的位置：</p>
 * <ul>
 *   <li>geo/block/&lt;id&gt;.geo.json（Blockbench GeckoLib 导出）</li>
 *   <li>textures/geckolib/&lt;id&gt;.png（独立目录，不影响物品图标使用的 textures/block 旧贴图）</li>
 *   <li>animations/cooking_pots.animation.json（空动画，静态渲染）</li>
 * </ul>
 */
public class CookingPotGeoModel<T extends BlockEntity & GeoAnimatable> extends GeoModel<T> {

    private final ResourceLocation model;
    private final ResourceLocation texture;
    private final ResourceLocation animation;

    public CookingPotGeoModel(ResourceLocation id) {
        String path = id.getPath();
        this.model = new ResourceLocation(id.getNamespace(), "geo/block/" + path + ".geo.json");
        this.texture = new ResourceLocation(id.getNamespace(), "textures/geckolib/" + path + ".png");
        this.animation = new ResourceLocation(id.getNamespace(),
                "animations/cooking_pots.animation.json");
    }

    @Override
    public ResourceLocation getModelResource(T animatable) {
        return model;
    }

    @Override
    public ResourceLocation getTextureResource(T animatable) {
        return texture;
    }

    @Override
    public ResourceLocation getAnimationResource(T animatable) {
        return animation;
    }
}
