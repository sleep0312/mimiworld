package com.mimiworld.mimiworld.client.renderer;

import com.mimiworld.mimiworld.client.model.MimiSheepModel;
import com.mimiworld.mimiworld.entity.MimiSheepEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

/**
 * 米羊羊（Mimi Sheep）实体渲染器：把 {@link MimiSheepModel} 绑定到实体。
 * 阴影半径 0.45 与实体碰撞箱宽度 0.9 的一半对应（同原版羊观感）。
 *
 * 在 ClientSetup 的 EntityRenderersEvent.RegisterRenderers 中注册给 MIMI_SHEEP。
 */
public class MimiSheepRenderer extends GeoEntityRenderer<MimiSheepEntity> {

    public MimiSheepRenderer(EntityRendererProvider.Context context) {
        super(context, new MimiSheepModel());
        this.shadowRadius = 0.45F;
    }
}
