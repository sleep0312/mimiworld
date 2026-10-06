package com.mimiworld.mimiworld.client.renderer;

import com.mimiworld.mimiworld.client.model.RiceWolfVultureModel;
import com.mimiworld.mimiworld.entity.RiceWolfVultureEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

/**
 * 哮噬米狼鹫（Rice Wolf-Vulture）实体渲染器：把 {@link RiceWolfVultureModel} 绑定到实体。
 * 阴影半径 0.7 对应实体碰撞箱宽度 1.2（四足狼鹫，折翼翼尖超出箱体）。
 *
 * 在 ClientSetup 的 EntityRenderersEvent.RegisterRenderers 中注册给 RICE_WOLF_VULTURE。
 */
public class RiceWolfVultureRenderer extends GeoEntityRenderer<RiceWolfVultureEntity> {

    public RiceWolfVultureRenderer(EntityRendererProvider.Context context) {
        super(context, new RiceWolfVultureModel());
        this.shadowRadius = 0.7F;
    }
}
