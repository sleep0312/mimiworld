package com.mimiworld.mimiworld.client.renderer;

import com.mimiworld.mimiworld.client.model.DibazongzuilongModel;
import com.mimiworld.mimiworld.entity.DibazongzuilongEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

/**
 * 第八宗罪龙（最终 Boss）实体渲染器：GeckoLib 自动完成模型烘焙与骨骼动画。
 * 在 ClientSetup 的 EntityRenderersEvent.RegisterRenderers 中注册给 DIBAZONGZUILONG。
 * 龙系超巨型飞行 Boss（最懒终末龙 1.5 倍体型），阴影半径取 4.5。
 */
public class DibazongzuilongRenderer extends GeoEntityRenderer<DibazongzuilongEntity> {

    public DibazongzuilongRenderer(EntityRendererProvider.Context context) {
        super(context, new DibazongzuilongModel());
        this.shadowRadius = 4.5F;
    }
}
