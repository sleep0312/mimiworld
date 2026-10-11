package com.mimiworld.mimiworld.client.renderer;

import com.mimiworld.mimiworld.client.model.LaziestFinalDragonModel;
import com.mimiworld.mimiworld.entity.LaziestFinalDragonEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

/**
 * 最懒终末龙实体渲染器：GeckoLib 自动完成 Promax 末影龙模型烘焙与骨骼动画。
 * 在 ClientSetup 的 EntityRenderersEvent.RegisterRenderers 中注册给 LAZIEST_FINAL_DRAGON。
 * 超巨型飞行 Boss，碰撞箱宽 3.5，阴影半径取 3.0。
 */
public class LaziestFinalDragonRenderer extends GeoEntityRenderer<LaziestFinalDragonEntity> {

    public LaziestFinalDragonRenderer(EntityRendererProvider.Context context) {
        super(context, new LaziestFinalDragonModel());
        this.shadowRadius = 3.0F;
    }
}
