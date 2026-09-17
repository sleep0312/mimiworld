package com.mimiworld.mimiworld.client.renderer;

import com.mimiworld.mimiworld.entity.PendingBossEntity;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.resources.ResourceLocation;

/**
 * 待定 Boss 的临时渲染器：借用原版玩家（Steve）模型与贴图。
 * 真实 Boss 模型（GeckoLib）实现后，替换为各自的 GeoEntityRenderer 并在 ClientSetup 改注册即可。
 */
public class PendingBossRenderer extends HumanoidMobRenderer<PendingBossEntity, PlayerModel<PendingBossEntity>> {

    private static final ResourceLocation TEXTURE =
            new ResourceLocation("minecraft", "textures/entity/player/wide/steve.png");

    public PendingBossRenderer(EntityRendererProvider.Context context) {
        super(context, new PlayerModel<>(context.bakeLayer(ModelLayers.PLAYER), false), 0.5F);
    }

    @Override
    public ResourceLocation getTextureLocation(PendingBossEntity entity) {
        return TEXTURE;
    }
}
