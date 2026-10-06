package com.mimiworld.mimiworld.client.renderer;

import com.mimiworld.mimiworld.client.model.FallenRicePhoenixModel;
import com.mimiworld.mimiworld.entity.FallenRicePhoenixEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

/**
 * 堕落米凤实体渲染器：把 {@link FallenRicePhoenixModel} 绑定到实体。
 * 阴影半径 0.7 对应实体碰撞箱宽度 1.4（直立凤凰，折翼翼尖略超出箱体）。
 *
 * 在 ClientSetup 的 EntityRenderersEvent.RegisterRenderers 中注册给 FALLEN_RICE_PHOENIX。
 */
public class FallenRicePhoenixRenderer extends GeoEntityRenderer<FallenRicePhoenixEntity> {

    public FallenRicePhoenixRenderer(EntityRendererProvider.Context context) {
        super(context, new FallenRicePhoenixModel());
        this.shadowRadius = 0.7F;
    }
}
