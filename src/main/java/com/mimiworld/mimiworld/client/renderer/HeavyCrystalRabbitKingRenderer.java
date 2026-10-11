package com.mimiworld.mimiworld.client.renderer;

import com.mimiworld.mimiworld.client.model.HeavyCrystalRabbitKingModel;
import com.mimiworld.mimiworld.entity.HeavyCrystalRabbitKingEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

/**
 * 重晶兔王渲染器：绑定 {@link HeavyCrystalRabbitKingModel}，阴影半径 0.7 对齐 1.4 宽碰撞箱。
 *
 * <p>模型直立约 54px（≈3.4 格，耳尖）与碰撞箱 1.4×3.2 基本吻合，无需额外缩放；
 * 晶刺与双耳张角会略微超出箱体，属正常造型。</p>
 */
public class HeavyCrystalRabbitKingRenderer extends GeoEntityRenderer<HeavyCrystalRabbitKingEntity> {

    public HeavyCrystalRabbitKingRenderer(EntityRendererProvider.Context context) {
        super(context, new HeavyCrystalRabbitKingModel());
        this.shadowRadius = 0.7F;
    }
}
