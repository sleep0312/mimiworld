package com.mimiworld.mimiworld.client.renderer;

import com.mimiworld.mimiworld.client.model.WujihuaModel;
import com.mimiworld.mimiworld.entity.WujihuaEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

/**
 * 污棘花渲染器：扎根植物，阴影半径 0.5 对应碰撞箱宽度 0.9（底部鳞茎+稻叶）。
 */
public class WujihuaRenderer extends GeoEntityRenderer<WujihuaEntity> {

    public WujihuaRenderer(EntityRendererProvider.Context context) {
        super(context, new WujihuaModel());
        this.shadowRadius = 0.5F;
    }
}
