package com.mimiworld.mimiworld.client.renderer;

import com.mimiworld.mimiworld.client.model.MimiSkeletonModel;
import com.mimiworld.mimiworld.entity.MimiSkeletonEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

/**
 * 米米骷髅（Mimi Skeleton）实体渲染器：把 {@link MimiSkeletonModel} 绑定到实体。
 * 阴影半径 0.5 对应实体碰撞箱宽度 0.6（双足人形，对齐原版骷髅阴影观感）。
 *
 * 在 ClientSetup 的 EntityRenderersEvent.RegisterRenderers 中注册给 MIMI_SKELETON。
 */
public class MimiSkeletonRenderer extends GeoEntityRenderer<MimiSkeletonEntity> {

    public MimiSkeletonRenderer(EntityRendererProvider.Context context) {
        super(context, new MimiSkeletonModel());
        this.shadowRadius = 0.5F;
    }
}
