package com.mimiworld.mimiworld.client.renderer;

import com.mimiworld.mimiworld.client.model.RiceSoulModel;
import com.mimiworld.mimiworld.entity.RiceSoulEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

/**
 * 米魂渲染器：飞行魂体，阴影半径 0.55 对应碰撞箱宽度 1.1。
 */
public class RiceSoulRenderer extends GeoEntityRenderer<RiceSoulEntity> {

    public RiceSoulRenderer(EntityRendererProvider.Context context) {
        super(context, new RiceSoulModel());
        this.shadowRadius = 0.55F;
    }
}
