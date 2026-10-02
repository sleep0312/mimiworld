package com.mimiworld.mimiworld.client.renderer;

import com.mimiworld.mimiworld.client.model.ZhuomiChickenModel;
import com.mimiworld.mimiworld.entity.ZhuomiChickenEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

/**
 * 啄米鸡（Zhuomi Chicken）实体渲染器：把 {@link ZhuomiChickenModel} 绑定到实体。
 * 阴影半径 0.3 与实体碰撞箱宽度 0.6 的一半对应（同原版鸡观感）。
 *
 * 在 ClientSetup 的 EntityRenderersEvent.RegisterRenderers 中注册给 ZHUOMI_CHICKEN。
 */
public class ZhuomiChickenRenderer extends GeoEntityRenderer<ZhuomiChickenEntity> {

    public ZhuomiChickenRenderer(EntityRendererProvider.Context context) {
        super(context, new ZhuomiChickenModel());
        this.shadowRadius = 0.3F;
    }
}
