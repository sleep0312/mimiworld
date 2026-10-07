package com.mimiworld.mimiworld.client.renderer;

import com.mimiworld.mimiworld.client.model.MishenModel;
import com.mimiworld.mimiworld.entity.MishenEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

/**
 * 米蜃渲染器：贴地低伏爬虫，阴影半径 0.7。
 */
public class MishenRenderer extends GeoEntityRenderer<MishenEntity> {

    public MishenRenderer(EntityRendererProvider.Context context) {
        super(context, new MishenModel());
        this.shadowRadius = 0.7F;
    }
}
