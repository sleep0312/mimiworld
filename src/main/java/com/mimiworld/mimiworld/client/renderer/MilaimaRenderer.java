package com.mimiworld.mimiworld.client.renderer;

import com.mimiworld.mimiworld.client.model.MilaimaModel;
import com.mimiworld.mimiworld.entity.MilaimaEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

/**
 * 米莱马（Milaima）实体渲染器：把 {@link MilaimaModel} 绑定到实体。
 *
 * <p>模型实测约 12.7px 宽 × 11.15px 高（含软手与头顶嫩芽），≈0.79×0.70 格；
 * 实体固定为 2 号史莱姆体型（碰撞箱 1.02×1.02），故整体放大 1.3 倍
 * （0.79×1.3≈1.03 宽、0.70×1.3≈0.90 高）正好落进箱体，脚底贴 y=0。
 * 阴影半径 0.5 对齐 2 号原版史莱姆观感（碰撞箱宽约 1.0）。</p>
 *
 * 在 ClientSetup 的 EntityRenderersEvent.RegisterRenderers 中注册给 MILAI_HORSE。
 */
public class MilaimaRenderer extends GeoEntityRenderer<MilaimaEntity> {

    public MilaimaRenderer(EntityRendererProvider.Context context) {
        super(context, new MilaimaModel());
        this.withScale(1.3F);
        this.shadowRadius = 0.5F;
    }
}
