package com.mimiworld.mimiworld.client.renderer;

import com.mimiworld.mimiworld.client.model.LazyElfModel;
import com.mimiworld.mimiworld.entity.LazyElfEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

/**
 * 懒精灵渲染器：佝偻小人型敌对生物，碰撞箱宽 0.6，阴影半径 0.45。
 */
public class LazyElfRenderer extends GeoEntityRenderer<LazyElfEntity> {

    public LazyElfRenderer(EntityRendererProvider.Context context) {
        super(context, new LazyElfModel());
        this.shadowRadius = 0.45F;
    }
}
