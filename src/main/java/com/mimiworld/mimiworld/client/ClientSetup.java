package com.mimiworld.mimiworld.client;

import com.mimiworld.mimiworld.MimiWorld;
import com.mimiworld.mimiworld.block.ModBlocks;
import com.mimiworld.mimiworld.client.renderer.PendingBossRenderer;
import com.mimiworld.mimiworld.entity.ModEntities;
import com.mimiworld.mimiworld.fluid.ModFluids;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.ThrownItemRenderer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

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

        // 七个待定 Boss 统一使用 Steve 人形模型占位，真实 GeckoLib 模型实现后在此替换
        event.registerEntityRenderer(ModEntities.MIMI_MONKEY_KING.get(), PendingBossRenderer::new);
        event.registerEntityRenderer(ModEntities.RICE_CRYSTAL_TIGER_KING.get(), PendingBossRenderer::new);
        event.registerEntityRenderer(ModEntities.SUPER_CRYSTAL_COW_KING.get(), PendingBossRenderer::new);
        event.registerEntityRenderer(ModEntities.HEAVY_CRYSTAL_RABBIT_KING.get(), PendingBossRenderer::new);
        event.registerEntityRenderer(ModEntities.COMPOUND_AZURE_PYTHON.get(), PendingBossRenderer::new);
        event.registerEntityRenderer(ModEntities.CLEANSING_LONGEVITY_KUI.get(), PendingBossRenderer::new);
        event.registerEntityRenderer(ModEntities.LAZIEST_FINAL_DRAGON.get(), PendingBossRenderer::new);
        // 257 盗米者：同样是 PendingBossEntity 占位实体，必须注册渲染器，
        // 否则生物蛋右键生成实体后客户端找不到 renderer 直接崩溃
        event.registerEntityRenderer(ModEntities.RICE_STEALER.get(), PendingBossRenderer::new);
    }

    /**
     * 十字型植物方块（米树苗、米草）的纹理带透明像素，必须挂到 cutout 渲染层，
     * 否则默认 solid 层不丢弃 Alpha=0 像素，透明区域在世界中会显示成黑色（与原版树苗/草一致的处理）。
     */
    @SuppressWarnings({"deprecation", "removal"}) // 1.20.1 中 setRenderLayer 被标记待删除但仍是官方可用 API（替代方案 1.21 才有）
    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            ItemBlockRenderTypes.setRenderLayer(ModBlocks.RICE_SAPLING.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(ModBlocks.RICE_GRASS.get(), RenderType.cutout());
            // 米汤流体方块：半透明奶白，需要 translucent 层（同原版水）
            ItemBlockRenderTypes.setRenderLayer(ModFluids.RICE_SOUP_BLOCK.get(), RenderType.translucent());
            // 饭汤流体方块：半透明暖汤，同样需要 translucent 层
            ItemBlockRenderTypes.setRenderLayer(ModFluids.RICE_BROTH_BLOCK.get(), RenderType.translucent());
        });
    }
}
