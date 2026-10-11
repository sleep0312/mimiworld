package com.mimiworld.mimiworld.client.renderer;

import com.mimiworld.mimiworld.client.model.CompoundAzurePythonModel;
import com.mimiworld.mimiworld.entity.CompoundAzurePythonEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

/**
 * 复晶天青蟒渲染器：绑定 {@link CompoundAzurePythonModel}，阴影半径 0.9。
 *
 * <p>模型昂首展兜约 3.2 格高、蛇身贴地，与碰撞箱 1.4×3.2 基本吻合，无需额外缩放。</p>
 */
public class CompoundAzurePythonRenderer extends GeoEntityRenderer<CompoundAzurePythonEntity> {

    public CompoundAzurePythonRenderer(EntityRendererProvider.Context context) {
        super(context, new CompoundAzurePythonModel());
        this.shadowRadius = 0.9F;
    }
}
