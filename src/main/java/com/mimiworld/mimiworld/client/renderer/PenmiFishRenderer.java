package com.mimiworld.mimiworld.client.renderer;

import com.mimiworld.mimiworld.client.model.PenmiFishModel;
import com.mimiworld.mimiworld.entity.PenmiFishEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

/**
 * 喷米鱼（Penmi Fish）实体渲染器：把 {@link PenmiFishModel} 绑定到实体。
 * 阴影半径 0.3 与实体碰撞箱宽度 0.5 的小型水生体型对应（同原版鳕鱼）。
 *
 * 在 ClientSetup 的 EntityRenderersEvent.RegisterRenderers 中注册给 PENMI_FISH。
 */
public class PenmiFishRenderer extends GeoEntityRenderer<PenmiFishEntity> {

    public PenmiFishRenderer(EntityRendererProvider.Context context) {
        super(context, new PenmiFishModel());
        this.shadowRadius = 0.3F;
    }
}
