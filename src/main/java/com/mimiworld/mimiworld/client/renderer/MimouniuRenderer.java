package com.mimiworld.mimiworld.client.renderer;

import com.mimiworld.mimiworld.client.model.MimouniuModel;
import com.mimiworld.mimiworld.entity.MimouniuEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

/**
 * 米哞牛（MiMoo Cow）实体渲染器：把 {@link MimouniuModel} 绑定到实体。
 * 阴影半径 0.7 与实体碰撞箱宽度 0.9 的观感对应（同原版牛）。
 *
 * 在 ClientSetup 的 EntityRenderersEvent.RegisterRenderers 中注册给 MIMOO_COW。
 */
public class MimouniuRenderer extends GeoEntityRenderer<MimouniuEntity> {

    public MimouniuRenderer(EntityRendererProvider.Context context) {
        super(context, new MimouniuModel());
        this.shadowRadius = 0.7F;
    }
}
