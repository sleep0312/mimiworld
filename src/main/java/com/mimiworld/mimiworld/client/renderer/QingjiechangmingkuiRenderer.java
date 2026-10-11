package com.mimiworld.mimiworld.client.renderer;

import com.mimiworld.mimiworld.client.model.QingjiechangmingkuiModel;
import com.mimiworld.mimiworld.entity.QingjiechangmingkuiEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

/**
 * 清洁长命夔渲染器：绑定 {@link QingjiechangmingkuiModel}，阴影半径 1.1 对齐 2.2 宽碰撞箱。
 *
 * <p>模型自地面起约 55px（≈3.4 格）高、巨蹄宽约 22px，与碰撞箱 2.2×3.4 吻合，无需额外缩放。</p>
 */
public class QingjiechangmingkuiRenderer extends GeoEntityRenderer<QingjiechangmingkuiEntity> {

    public QingjiechangmingkuiRenderer(EntityRendererProvider.Context context) {
        super(context, new QingjiechangmingkuiModel());
        this.shadowRadius = 1.1F;
    }
}
