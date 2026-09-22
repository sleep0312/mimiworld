package com.mimiworld.mimiworld.client.renderer;

import com.mimiworld.mimiworld.client.model.LaziestFinalDragonModel;
import com.mimiworld.mimiworld.entity.LaziestFinalDragonEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

/**
 * 最懒终末龙实体渲染器：GeckoLib 自动完成飞龙模型烘焙与骨骼动画。
 * 在 ClientSetup 的 EntityRenderersEvent.RegisterRenderers 中注册给 LAZIEST_FINAL_DRAGON。
 * 超巨型飞行 Boss，投影半径取碰撞箱宽度的 80%（2.5×0.8=2.0）。
 */
public class LaziestFinalDragonRenderer extends GeoEntityRenderer<LaziestFinalDragonEntity> {

    public LaziestFinalDragonRenderer(EntityRendererProvider.Context context) {
        super(context, new LaziestFinalDragonModel());
        this.shadowRadius = 2.0F;
    }
}
