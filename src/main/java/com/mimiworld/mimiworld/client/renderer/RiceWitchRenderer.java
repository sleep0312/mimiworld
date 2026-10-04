package com.mimiworld.mimiworld.client.renderer;

import com.mimiworld.mimiworld.client.model.RiceWitchModel;
import com.mimiworld.mimiworld.entity.RiceWitchEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

/**
 * 米巫（Rice Witch）实体渲染器：把 {@link RiceWitchModel} 绑定到实体。
 * 阴影半径 0.45 对应实体碰撞箱宽度 0.6。
 *
 * 在 ClientSetup 的 EntityRenderersEvent.RegisterRenderers 中注册给 RICE_WITCH。
 */
public class RiceWitchRenderer extends GeoEntityRenderer<RiceWitchEntity> {

    public RiceWitchRenderer(EntityRendererProvider.Context context) {
        super(context, new RiceWitchModel());
        this.shadowRadius = 0.45F;
    }
}
