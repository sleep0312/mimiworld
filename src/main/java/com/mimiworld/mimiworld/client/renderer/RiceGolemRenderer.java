package com.mimiworld.mimiworld.client.renderer;

import com.mimiworld.mimiworld.client.model.RiceGolemModel;
import com.mimiworld.mimiworld.entity.RiceGolemEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

/**
 * 米傀儡（Rice Golem）实体渲染器：把 {@link RiceGolemModel} 绑定到实体。
 * 阴影半径 0.7 与实体碰撞箱宽度 1.4 的观感对应（同原版铁傀儡）。
 *
 * 在 ClientSetup 的 EntityRenderersEvent.RegisterRenderers 中注册给 RICE_GOLEM。
 */
public class RiceGolemRenderer extends GeoEntityRenderer<RiceGolemEntity> {

    public RiceGolemRenderer(EntityRendererProvider.Context context) {
        super(context, new RiceGolemModel());
        this.shadowRadius = 0.7F;
    }
}
