package com.mimiworld.mimiworld.client.renderer;

import com.mimiworld.mimiworld.client.model.HaomiWolfModel;
import com.mimiworld.mimiworld.entity.HaomiWolfEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

/**
 * 嗥米狼（Haomi Wolf）实体渲染器：把 {@link HaomiWolfModel} 绑定到实体。
 * 阴影半径 0.4 对应实体碰撞箱宽度 0.6（四足体长，阴影略宽观感更稳）。
 *
 * 在 ClientSetup 的 EntityRenderersEvent.RegisterRenderers 中注册给 HAOMI_WOLF。
 */
public class HaomiWolfRenderer extends GeoEntityRenderer<HaomiWolfEntity> {

    public HaomiWolfRenderer(EntityRendererProvider.Context context) {
        super(context, new HaomiWolfModel());
        this.shadowRadius = 0.4F;
    }
}
