package com.mimiworld.mimiworld.client.renderer;

import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Mob;

/**
 * 待定实体的临时渲染器：借用原版玩家（Steve）模型与贴图。
 *
 * <p>服务于两类尚未制作专属 GeckoLib 模型的占位实体：七个待定 Boss
 * （{@link com.mimiworld.mimiworld.entity.PendingBossEntity}）以及通用占位生物
 * （{@link com.mimiworld.mimiworld.entity.PlaceholderRiceMobEntity}，如化灵米）。
 * 真实模型实现后，替换为各自的 GeoEntityRenderer 并在 ClientSetup 改注册即可。</p>
 *
 * <p>注意：不注册渲染器的实体一在世界中生成，客户端
 * {@code EntityRenderDispatcher.shouldRender} 就会因 renderer 为 null 直接崩溃。</p>
 */
public class PendingBossRenderer<T extends Mob> extends HumanoidMobRenderer<T, PlayerModel<T>> {

    private static final ResourceLocation TEXTURE =
            new ResourceLocation("minecraft", "textures/entity/player/wide/steve.png");

    public PendingBossRenderer(EntityRendererProvider.Context context) {
        super(context, new PlayerModel<>(context.bakeLayer(ModelLayers.PLAYER), false), 0.5F);
    }

    @Override
    public ResourceLocation getTextureLocation(T entity) {
        return TEXTURE;
    }
}
