package com.mimiworld.mimiworld.client;

import com.mimiworld.mimiworld.MimiWorld;
import com.mimiworld.mimiworld.block.ModBlocks;
import com.mimiworld.mimiworld.client.renderer.LaziestFinalDragonRenderer;
import com.mimiworld.mimiworld.client.renderer.MicroDuckRenderer;
import com.mimiworld.mimiworld.client.renderer.PendingBossRenderer;
import com.mimiworld.mimiworld.client.renderer.StickyRiceSpiderRenderer;
import com.mimiworld.mimiworld.client.screen.RiceMakerScreen;
import com.mimiworld.mimiworld.client.screen.RicePoundingTableScreen;
import com.mimiworld.mimiworld.client.screen.RicePressScreen;
import com.mimiworld.mimiworld.client.screen.AdvancedRicePoundingTableScreen;
import com.mimiworld.mimiworld.client.screen.IonizationTableScreen;
import com.mimiworld.mimiworld.client.screen.PrecipitationTableScreen;
import com.mimiworld.mimiworld.client.screen.DissociationTableScreen;
import com.mimiworld.mimiworld.client.screen.SwordForgingTableScreen;
import com.mimiworld.mimiworld.client.screen.ArmorForgingTableScreen;
import com.mimiworld.mimiworld.client.screen.BowForgingTableScreen;
import com.mimiworld.mimiworld.client.screen.SwordQuenchingTableScreen;
import com.mimiworld.mimiworld.client.screen.BloodlettingTableScreen;
import com.mimiworld.mimiworld.client.screen.GrindingTableScreen;
import com.mimiworld.mimiworld.client.screen.CrusherScreen;
import com.mimiworld.mimiworld.entity.ModEntities;
import com.mimiworld.mimiworld.menu.ModMenuTypes;
import com.mimiworld.mimiworld.fluid.ModFluids;
import net.minecraft.client.gui.screens.MenuScreens;
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
        // 157 最懒终末龙：GeckoLib 宗罪龙模型（idle/flying 动画），其余六个 Boss 仍用 Steve 占位
        event.registerEntityRenderer(ModEntities.LAZIEST_FINAL_DRAGON.get(), LaziestFinalDragonRenderer::new);
        // 257 盗米者：真实 GeckoLib 实体 MicroDuckEntity，使用 microduck 模型渲染
        event.registerEntityRenderer(ModEntities.RICE_STEALER.get(), MicroDuckRenderer::new);
        // 黏米之蛛：GeckoLib spider 模型（idle/walk/attack 动画）
        event.registerEntityRenderer(ModEntities.STICKY_RICE_SPIDER.get(), StickyRiceSpiderRenderer::new);
    }

    // 造米台菜单 ↔ GUI 屏幕的绑定（1.20.1 Forge 47.x 仍用 MenuScreens.register）
    // 随 FMLClientSetupEvent 的 enqueueWork 一起在主线程注册，见 onClientSetup

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

            // 绑定造米台菜单与 GUI 屏幕
            MenuScreens.register(ModMenuTypes.RICE_MAKER_MENU.get(), RiceMakerScreen::new);
            // 绑定锤米台菜单与 GUI 屏幕
            MenuScreens.register(ModMenuTypes.RICE_POUNDING_TABLE_MENU.get(), RicePoundingTableScreen::new);
            // 绑定压饭机菜单与 GUI 屏幕
            MenuScreens.register(ModMenuTypes.RICE_PRESS_MENU.get(), RicePressScreen::new);
            // 绑定高级锤米台菜单与 GUI 屏幕
            MenuScreens.register(ModMenuTypes.ADVANCED_RICE_POUNDING_TABLE_MENU.get(), AdvancedRicePoundingTableScreen::new);
            // 绑定电离台菜单与 GUI 屏幕
            MenuScreens.register(ModMenuTypes.IONIZATION_TABLE_MENU.get(), IonizationTableScreen::new);
            // 绑定沉淀台菜单与 GUI 屏幕
            MenuScreens.register(ModMenuTypes.PRECIPITATION_TABLE_MENU.get(), PrecipitationTableScreen::new);
            // 绑定离解台菜单与 GUI 屏幕
            MenuScreens.register(ModMenuTypes.DISSOCIATION_TABLE_MENU.get(), DissociationTableScreen::new);
            // 绑定铸剑台/铸甲台/铸弓台菜单与 GUI 屏幕（原版工作台外观，各自独立配方表）
            MenuScreens.register(ModMenuTypes.SWORD_FORGING_TABLE_MENU.get(), SwordForgingTableScreen::new);
            MenuScreens.register(ModMenuTypes.ARMOR_FORGING_TABLE_MENU.get(), ArmorForgingTableScreen::new);
            MenuScreens.register(ModMenuTypes.BOW_FORGING_TABLE_MENU.get(), BowForgingTableScreen::new);
            // 绑定淬剑台/汲血台/碾磨台/搅碎器菜单与 GUI 屏幕（多输入熔炉式定时加工）
            MenuScreens.register(ModMenuTypes.SWORD_QUENCHING_TABLE_MENU.get(), SwordQuenchingTableScreen::new);
            MenuScreens.register(ModMenuTypes.BLOODLETTING_TABLE_MENU.get(), BloodlettingTableScreen::new);
            MenuScreens.register(ModMenuTypes.GRINDING_TABLE_MENU.get(), GrindingTableScreen::new);
            MenuScreens.register(ModMenuTypes.CRUSHER_MENU.get(), CrusherScreen::new);
        });
    }
}
