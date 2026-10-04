package com.mimiworld.mimiworld.client.renderer;

import com.mimiworld.mimiworld.client.model.HumiCorpseModel;
import com.mimiworld.mimiworld.entity.HumiCorpseEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

/**
 * 糊米古尸（Humi Corpse）实体渲染器：把 {@link HumiCorpseModel} 绑定到实体。
 * 阴影半径 0.5 对应实体碰撞箱宽度 0.7（双足人形，对齐原版僵尸阴影观感）。
 *
 * 在 ClientSetup 的 EntityRenderersEvent.RegisterRenderers 中注册给 HUMI_CORPSE。
 */
public class HumiCorpseRenderer extends GeoEntityRenderer<HumiCorpseEntity> {

    public HumiCorpseRenderer(EntityRendererProvider.Context context) {
        super(context, new HumiCorpseModel());
        this.shadowRadius = 0.5F;
    }
}
