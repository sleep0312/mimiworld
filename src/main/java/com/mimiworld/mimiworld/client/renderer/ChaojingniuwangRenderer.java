package com.mimiworld.mimiworld.client.renderer;

import com.mimiworld.mimiworld.client.model.ChaojingniuwangModel;
import com.mimiworld.mimiworld.entity.ChaojingniuwangEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

/**
 * 超晶牛王渲染器：绑定 {@link ChaojingniuwangModel}，阴影半径 0.75 对齐 1.5 宽碰撞箱。
 *
 * <p>模型直立约 41px（≈2.6 格）与碰撞箱 1.5×2.6 基本吻合，无需额外缩放；
 * 巨斧刃尖斜擎时会超出箱体，属正常持械造型。</p>
 */
public class ChaojingniuwangRenderer extends GeoEntityRenderer<ChaojingniuwangEntity> {

    public ChaojingniuwangRenderer(EntityRendererProvider.Context context) {
        super(context, new ChaojingniuwangModel());
        this.shadowRadius = 0.75F;
    }
}
