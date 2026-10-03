package com.mimiworld.mimiworld.client.renderer;

import com.mimiworld.mimiworld.client.model.MihengPigModel;
import com.mimiworld.mimiworld.entity.MihengPigEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

/**
 * 米哼猪（MiHeng Pig）实体渲染器：把 {@link MihengPigModel} 绑定到实体。
 * 阴影半径 0.7 与实体碰撞箱宽度 0.9 的观感对应（同原版猪/牛）。
 *
 * 在 ClientSetup 的 EntityRenderersEvent.RegisterRenderers 中注册给 MIHENG_PIG。
 */
public class MihengPigRenderer extends GeoEntityRenderer<MihengPigEntity> {

    public MihengPigRenderer(EntityRendererProvider.Context context) {
        super(context, new MihengPigModel());
        this.shadowRadius = 0.7F;
    }
}
