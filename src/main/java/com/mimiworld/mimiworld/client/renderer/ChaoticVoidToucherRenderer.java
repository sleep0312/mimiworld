package com.mimiworld.mimiworld.client.renderer;

import com.mimiworld.mimiworld.client.model.ChaoticVoidToucherModel;
import com.mimiworld.mimiworld.entity.ChaoticVoidToucherEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

/**
 * 混沌不可名状之米界虚无触渲染器。
 *
 * <p>静止包围盒约 19×19×24px（≈1.2×1.2×1.5 格，基座下沉 0.19 格埋进地里体现"扎根"），
 * 碰撞箱 2.2×1.6，整体放大 1.5 倍（高约 1.5 格、触手前伸约 2.25 格）更有 Boss 压迫感；
 * 阴影半径 1.0 对齐 2.2 宽的基座。</p>
 */
public class ChaoticVoidToucherRenderer extends GeoEntityRenderer<ChaoticVoidToucherEntity> {

    public ChaoticVoidToucherRenderer(EntityRendererProvider.Context context) {
        super(context, new ChaoticVoidToucherModel());
        this.withScale(1.5F);
        this.shadowRadius = 1.0F;
    }
}
