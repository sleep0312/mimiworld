package com.mimiworld.mimiworld.client.renderer;

import com.mimiworld.mimiworld.client.model.MeimiruModel;
import com.mimiworld.mimiworld.entity.MeimiruEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

/**
 * 没米入（Meimiru）实体渲染器：把 {@link MeimiruModel} 绑定到实体。
 *
 * <p>模型实测约 34.5px≈2.16 格高，碰撞箱按末影人式高个定为 0.6×2.5，
 * 故整体放大 1.15 倍（2.16×1.15≈2.48）正好撑满箱体，脚底仍贴 y=0。
 * 阴影半径 0.5 对齐原版末影人观感（碰撞箱宽 0.6）。</p>
 *
 * 在 ClientSetup 的 EntityRenderersEvent.RegisterRenderers 中注册给 MOMI_ENTRANT。
 */
public class MeimiruRenderer extends GeoEntityRenderer<MeimiruEntity> {

    public MeimiruRenderer(EntityRendererProvider.Context context) {
        super(context, new MeimiruModel());
        this.withScale(1.15F);
        this.shadowRadius = 0.5F;
    }
}
