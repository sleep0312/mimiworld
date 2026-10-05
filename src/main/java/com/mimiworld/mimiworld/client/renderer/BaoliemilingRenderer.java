package com.mimiworld.mimiworld.client.renderer;

import com.mimiworld.mimiworld.client.model.BaoliemilingModel;
import com.mimiworld.mimiworld.entity.BaoliemilingEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

/**
 * 爆烈米灵（Explosive Rice Spirit）实体渲染器：把 {@link BaoliemilingModel} 绑定到实体。
 * 阴影半径 0.4 对应碰撞箱宽度 0.8（烈焰人式悬浮飞行生物，无落地脚步阴影）。
 *
 * 在 ClientSetup 的 EntityRenderersEvent.RegisterRenderers 中注册给 EXPLOSIVE_RICE_SPIRIT。
 */
public class BaoliemilingRenderer extends GeoEntityRenderer<BaoliemilingEntity> {

    public BaoliemilingRenderer(EntityRendererProvider.Context context) {
        super(context, new BaoliemilingModel());
        this.shadowRadius = 0.4F;
    }
}
