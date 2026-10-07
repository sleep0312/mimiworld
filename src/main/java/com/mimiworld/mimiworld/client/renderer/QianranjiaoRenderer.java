package com.mimiworld.mimiworld.client.renderer;

import com.mimiworld.mimiworld.client.model.QianranjiaoModel;
import com.mimiworld.mimiworld.entity.QianranjiaoEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

/**
 * 浅染鲛渲染器：鲸类成体碰撞箱 2.6×2.0，阴影半径 1.3。
 */
public class QianranjiaoRenderer extends GeoEntityRenderer<QianranjiaoEntity> {

    public QianranjiaoRenderer(EntityRendererProvider.Context context) {
        super(context, new QianranjiaoModel());
        this.shadowRadius = 1.3F;
    }
}
