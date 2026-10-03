package com.mimiworld.mimiworld.client.renderer;

import com.mimiworld.mimiworld.client.model.TanmiFishModel;
import com.mimiworld.mimiworld.entity.TanmiFishEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

/**
 * 贪米鱼（Tanmi Fish）实体渲染器：把 {@link TanmiFishModel} 绑定到实体。
 * 阴影半径 0.3 与实体碰撞箱宽度 0.5 的小型水生体型对应（同原版鳕鱼）。
 *
 * 在 ClientSetup 的 EntityRenderersEvent.RegisterRenderers 中注册给 TANMI_FISH。
 */
public class TanmiFishRenderer extends GeoEntityRenderer<TanmiFishEntity> {

    public TanmiFishRenderer(EntityRendererProvider.Context context) {
        super(context, new TanmiFishModel());
        this.shadowRadius = 0.3F;
    }
}
