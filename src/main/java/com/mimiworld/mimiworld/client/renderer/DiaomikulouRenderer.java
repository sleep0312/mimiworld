package com.mimiworld.mimiworld.client.renderer;

import com.mimiworld.mimiworld.client.model.DiaomikulouModel;
import com.mimiworld.mimiworld.entity.DiaomikulouEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

/**
 * 雕米骷髅（Carved Rice Skeleton）实体渲染器：把 {@link DiaomikulouModel} 绑定到实体。
 * 阴影半径 0.5 对应实体碰撞箱宽度 0.6（双足人形，对齐原版骷髅阴影观感）。
 *
 * 在 ClientSetup 的 EntityRenderersEvent.RegisterRenderers 中注册给 CARVED_RICE_SKELETON。
 */
public class DiaomikulouRenderer extends GeoEntityRenderer<DiaomikulouEntity> {

    public DiaomikulouRenderer(EntityRendererProvider.Context context) {
        super(context, new DiaomikulouModel());
        this.shadowRadius = 0.5F;
    }
}
