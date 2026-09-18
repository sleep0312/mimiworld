package com.mimiworld.mimiworld.client.renderer;

import com.mimiworld.mimiworld.client.model.MicroDuckModel;
import com.mimiworld.mimiworld.entity.MicroDuckEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

/**
 * 微型鸭（MicroDuck）实体渲染器：把 {@link MicroDuckModel} 绑定到实体上。
 * GeckoLib 的 GeoEntityRenderer 会自动处理模型烘焙、动画矩阵与贴图绘制，
 * 无需像原版实体那样手写 LayerDefinition。
 *
 * 在 ClientSetup 的 EntityRenderersEvent.RegisterRenderers 中注册给 RICE_STEALER 实体类型。
 */
public class MicroDuckRenderer extends GeoEntityRenderer<MicroDuckEntity> {

    public MicroDuckRenderer(EntityRendererProvider.Context context) {
        super(context, new MicroDuckModel());
        // 小体型生物的地面投影半径（实体宽 0.5，阴影稍小一圈更协调）
        this.shadowRadius = 0.25F;
    }
}
