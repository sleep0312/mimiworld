package com.mimiworld.mimiworld.client.renderer;

import com.mimiworld.mimiworld.client.model.HualingmiModel;
import com.mimiworld.mimiworld.entity.HualingmiEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

/**
 * 化灵米（Hualing Rice）实体渲染器：把 {@link HualingmiModel} 绑定到实体。
 * 阴影半径 0.6 对应实体碰撞箱宽度 1.0（空中飞灵，阴影略小于箱体观感更轻）。
 *
 * 在 ClientSetup 的 EntityRenderersEvent.RegisterRenderers 中注册给 HUALING_RICE。
 */
public class HualingmiRenderer extends GeoEntityRenderer<HualingmiEntity> {

    public HualingmiRenderer(EntityRendererProvider.Context context) {
        super(context, new HualingmiModel());
        this.shadowRadius = 0.6F;
    }
}
