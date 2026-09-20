package com.mimiworld.mimiworld.client.renderer;

import com.mimiworld.mimiworld.client.model.StickyRiceSpiderModel;
import com.mimiworld.mimiworld.entity.StickyRiceSpiderEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

/**
 * 黏米之蛛（Sticky Rice Spider）实体渲染器：把 {@link StickyRiceSpiderModel} 绑定到实体。
 * 阴影半径 0.7 与实体碰撞箱宽度 1.4 的一半对应（同原版蜘蛛观感）。
 *
 * 在 ClientSetup 的 EntityRenderersEvent.RegisterRenderers 中注册给 STICKY_RICE_SPIDER。
 */
public class StickyRiceSpiderRenderer extends GeoEntityRenderer<StickyRiceSpiderEntity> {

    public StickyRiceSpiderRenderer(EntityRendererProvider.Context context) {
        super(context, new StickyRiceSpiderModel());
        this.shadowRadius = 0.7F;
    }
}
