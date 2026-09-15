package com.mimiworld.mimiworld.client;

import com.mimiworld.mimiworld.MimiWorld;
import com.mimiworld.mimiworld.entity.ModEntities;
import net.minecraft.client.renderer.entity.ThrownItemRenderer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * 仅客户端的注册：投掷物渲染器。ThrownItemRenderer 直接渲染物品自身贴图，
 * 因此米团飞行时显示为米团物品图标，无需单独的实体纹理文件。
 */
@Mod.EventBusSubscriber(modid = MimiWorld.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientSetup {

    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(ModEntities.RICE_BALL.get(), ThrownItemRenderer::new);
        event.registerEntityRenderer(ModEntities.COOKED_RICE_BALL.get(), ThrownItemRenderer::new);
    }
}
