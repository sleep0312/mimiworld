package com.mimiworld.mimiworld.client.renderer;

import com.mimiworld.mimiworld.client.model.WangmishangjiaoModel;
import com.mimiworld.mimiworld.entity.WangmishangjiaoEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

/**
 * 望米伤鲛渲染器：巨型古鲸碰撞箱 3.0×4.2，阴影半径 2.0。
 */
public class WangmishangjiaoRenderer extends GeoEntityRenderer<WangmishangjiaoEntity> {

    public WangmishangjiaoRenderer(EntityRendererProvider.Context context) {
        super(context, new WangmishangjiaoModel());
        this.shadowRadius = 2.0F;
    }
}
