package com.mimiworld.mimiworld.client.renderer;

import com.mimiworld.mimiworld.client.model.FanjinghuwangModel;
import com.mimiworld.mimiworld.entity.FanjinghuwangEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

/**
 * 饭晶虎王渲染器：绑定 {@link FanjinghuwangModel}，阴影半径 0.7 对齐 1.2 宽碰撞箱。
 *
 * <p>模型直立约 47px（≈2.9 格）与碰撞箱 1.2×2.9 基本吻合，无需额外缩放。</p>
 */
public class FanjinghuwangRenderer extends GeoEntityRenderer<FanjinghuwangEntity> {

    public FanjinghuwangRenderer(EntityRendererProvider.Context context) {
        super(context, new FanjinghuwangModel());
        this.shadowRadius = 0.7F;
    }
}
