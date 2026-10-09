package com.mimiworld.mimiworld.client.renderer;

import com.mimiworld.mimiworld.client.model.DaomizheModel;
import com.mimiworld.mimiworld.entity.DaomizheEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

/**
 * 盗米者（Rice Stealer）实体渲染器：把 {@link DaomizheModel} 绑定到实体。
 * 阴影半径 0.55 对应实体碰撞箱宽度 0.8。
 *
 * 在 ClientSetup 的 EntityRenderersEvent.RegisterRenderers 中注册给 RICE_STEALER。
 */
public class DaomizheRenderer extends GeoEntityRenderer<DaomizheEntity> {

    public DaomizheRenderer(EntityRendererProvider.Context context) {
        super(context, new DaomizheModel());
        this.shadowRadius = 0.55F;
    }
}
