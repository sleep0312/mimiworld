package com.mimiworld.mimiworld.client.renderer;

import com.mimiworld.mimiworld.client.model.MihengPigCorpseModel;
import com.mimiworld.mimiworld.entity.MihengPigCorpseEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

/**
 * 米哼猪尸渲染器：阴影半径 0.6 对应碰撞箱宽度 0.9（四足猪形）。
 */
public class MihengPigCorpseRenderer extends GeoEntityRenderer<MihengPigCorpseEntity> {

    public MihengPigCorpseRenderer(EntityRendererProvider.Context context) {
        super(context, new MihengPigCorpseModel());
        this.shadowRadius = 0.6F;
    }
}
