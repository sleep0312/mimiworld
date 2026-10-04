package com.mimiworld.mimiworld.client;

import com.mimiworld.mimiworld.MimiWorld;
import com.mimiworld.mimiworld.block.ModBlocks;
import com.mimiworld.mimiworld.block.entity.ModBlockEntities;
import com.mimiworld.mimiworld.client.renderer.CookingPotGeoBlockRenderer;
import com.mimiworld.mimiworld.client.renderer.LaziestFinalDragonRenderer;
import com.mimiworld.mimiworld.client.renderer.MimiSheepRenderer;
import com.mimiworld.mimiworld.client.renderer.MicroDuckRenderer;
import com.mimiworld.mimiworld.client.renderer.MimouniuRenderer;
import com.mimiworld.mimiworld.client.renderer.MihengPigRenderer;
import com.mimiworld.mimiworld.client.renderer.PendingBossRenderer;
import com.mimiworld.mimiworld.client.renderer.PenmiFishRenderer;
import com.mimiworld.mimiworld.client.renderer.RiceGolemRenderer;
import com.mimiworld.mimiworld.client.renderer.HaomiWolfRenderer;
import com.mimiworld.mimiworld.client.renderer.HumiCorpseRenderer;
import com.mimiworld.mimiworld.client.renderer.PamiCrawlerRenderer;
import com.mimiworld.mimiworld.client.renderer.RiceWitchRenderer;
import com.mimiworld.mimiworld.client.renderer.MimiSkeletonRenderer;
import com.mimiworld.mimiworld.client.renderer.StickyRiceSpiderRenderer;
import com.mimiworld.mimiworld.client.renderer.TanmiFishRenderer;
import com.mimiworld.mimiworld.client.renderer.ZhuomiChickenRenderer;
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
import com.mimiworld.mimiworld.client.screen.PestleRealmTableScreen;
import com.mimiworld.mimiworld.client.screen.IngotCondensationTableScreen;
import com.mimiworld.mimiworld.client.screen.MixerScreen;
import com.mimiworld.mimiworld.client.screen.SmallCookingPotScreen;
import com.mimiworld.mimiworld.client.screen.CookingPotScreen;
import com.mimiworld.mimiworld.client.screen.LargeCookingPotScreen;
import com.mimiworld.mimiworld.client.screen.ExtraLargeCookingPotScreen;
import com.mimiworld.mimiworld.client.screen.JumboMegaCookingPotScreen;
import com.mimiworld.mimiworld.client.screen.IronWokScreen;
import com.mimiworld.mimiworld.client.screen.OvenScreen;
import com.mimiworld.mimiworld.entity.ModEntities;
import com.mimiworld.mimiworld.menu.ModMenuTypes;
import com.mimiworld.mimiworld.fluid.ModFluids;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.resources.ResourceLocation;
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
        // 最懒终末龙：GeckoLib 宗罪龙模型（idle/flying 动画），其余六个 Boss 仍用 Steve 占位
        event.registerEntityRenderer(ModEntities.LAZIEST_FINAL_DRAGON.get(), LaziestFinalDragonRenderer::new);
        // 盗米者：真实 GeckoLib 实体 MicroDuckEntity，使用 microduck 模型渲染
        event.registerEntityRenderer(ModEntities.RICE_STEALER.get(), MicroDuckRenderer::new);
        // 黏米之蛛：GeckoLib spider 模型（idle/walk/attack 动画）
        event.registerEntityRenderer(ModEntities.STICKY_RICE_SPIDER.get(), StickyRiceSpiderRenderer::new);
        // 米羊羊：GeckoLib mimi_sheep 模型（idle/walk/scared_run 动画）
        event.registerEntityRenderer(ModEntities.MIMI_SHEEP.get(), MimiSheepRenderer::new);
        // 啄米鸡：GeckoLib zhuomi_chicken 模型（animation.idle/animation.walk 动画）
        event.registerEntityRenderer(ModEntities.ZHUOMI_CHICKEN.get(), ZhuomiChickenRenderer::new);
        // 米哞牛：GeckoLib mimouniu 模型（animation.mimouniu.idle/walk 动画，大米主题贴图）
        event.registerEntityRenderer(ModEntities.MIMOO_COW.get(), MimouniuRenderer::new);
        // 米哼猪：GeckoLib mihengzhu 模型（animation.mihengzhu.idle/walk 动画，大米主题贴图）
        event.registerEntityRenderer(ModEntities.MIHENG_PIG.get(), MihengPigRenderer::new);
        // 贪米鱼：GeckoLib tanmiyu 模型（animation.tanmiyu.swim/idle 动画，水生鳕鱼式造型）
        event.registerEntityRenderer(ModEntities.TANMI_FISH.get(), TanmiFishRenderer::new);
        // 喷米鱼：GeckoLib penmiyu 模型（animation.penmiyu.swim/idle 动画，水生造型）
        event.registerEntityRenderer(ModEntities.PENMI_FISH.get(), PenmiFishRenderer::new);
        // 米傀儡：GeckoLib rice_golem 模型（animation.rice_golem.idle/walk 动画，稻草+麻绳贴图）
        event.registerEntityRenderer(ModEntities.RICE_GOLEM.get(), RiceGolemRenderer::new);
        // 嗥米狼：GeckoLib haomi_wolf 模型（animation.idle/animation.walk 动画，狼灰+米白+稻草金贴图）
        event.registerEntityRenderer(ModEntities.HAOMI_WOLF.get(), HaomiWolfRenderer::new);
        // 米米骷髅：GeckoLib mimi_skeleton 模型（animation.idle/animation.walk 动画，陈米骨色+稻绿鬼火眼+米袋腰裙贴图）
        event.registerEntityRenderer(ModEntities.MIMI_SKELETON.get(), MimiSkeletonRenderer::new);
        // 糊米古尸：GeckoLib humi_corpse 模型（animation.idle/walk/attack 动画，陈米尸肤+符纸+青灰官袍贴图）
        event.registerEntityRenderer(ModEntities.HUMI_CORPSE.get(), HumiCorpseRenderer::new);
        // 爪巴米者：GeckoLib pami_crawler 模型（animation.idle/walk/attack 动画，旧米麻袋苦力怕贴图），引信期间 energySwirl 白闪
        event.registerEntityRenderer(ModEntities.PAMI_CRAWLER.get(), PamiCrawlerRenderer::new);
        event.registerEntityRenderer(ModEntities.RICE_WITCH.get(), RiceWitchRenderer::new);

        // 煮锅系列五方块：GeckoLib geo 模型（无动画，静态渲染）
        event.registerBlockEntityRenderer(ModBlockEntities.SMALL_COOKING_POT.get(),
                ctx -> new CookingPotGeoBlockRenderer<>(
                        new ResourceLocation(MimiWorld.MODID, "small_cooking_pot")));
        event.registerBlockEntityRenderer(ModBlockEntities.COOKING_POT.get(),
                ctx -> new CookingPotGeoBlockRenderer<>(
                        new ResourceLocation(MimiWorld.MODID, "cooking_pot")));
        event.registerBlockEntityRenderer(ModBlockEntities.LARGE_COOKING_POT.get(),
                ctx -> new CookingPotGeoBlockRenderer<>(
                        new ResourceLocation(MimiWorld.MODID, "large_cooking_pot")));
        event.registerBlockEntityRenderer(ModBlockEntities.EXTRA_LARGE_COOKING_POT.get(),
                ctx -> new CookingPotGeoBlockRenderer<>(
                        new ResourceLocation(MimiWorld.MODID, "extra_large_cooking_pot")));
        event.registerBlockEntityRenderer(ModBlockEntities.JUMBO_MEGA_COOKING_POT.get(),
                ctx -> new CookingPotGeoBlockRenderer<>(
                        new ResourceLocation(MimiWorld.MODID, "jumbo_mega_cooking_pot")));
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
            // 绑定杵界台/凝锭台菜单与 GUI 屏幕（NxN 网格工作台式即时合成）
            MenuScreens.register(ModMenuTypes.PESTLE_REALM_TABLE_MENU.get(), PestleRealmTableScreen::new);
            MenuScreens.register(ModMenuTypes.INGOT_CONDENSATION_TABLE_MENU.get(), IngotCondensationTableScreen::new);
            // 绑定搅拌器（9 入 1 出即时合成）/ 小煮锅（熔炉式燃料加工）菜单与 GUI 屏幕
            MenuScreens.register(ModMenuTypes.MIXER_MENU.get(), MixerScreen::new);
            MenuScreens.register(ModMenuTypes.SMALL_COOKING_POT_MENU.get(), SmallCookingPotScreen::new);
            // 绑定煮锅/大煮锅/超大煮锅/巨无霸大大大煮锅菜单与 GUI 屏幕（NxN 网格熔炉式燃料加工）
            MenuScreens.register(ModMenuTypes.COOKING_POT_MENU.get(), CookingPotScreen::new);
            MenuScreens.register(ModMenuTypes.LARGE_COOKING_POT_MENU.get(), LargeCookingPotScreen::new);
            MenuScreens.register(ModMenuTypes.EXTRA_LARGE_COOKING_POT_MENU.get(), ExtraLargeCookingPotScreen::new);
            MenuScreens.register(ModMenuTypes.JUMBO_MEGA_COOKING_POT_MENU.get(), JumboMegaCookingPotScreen::new);
            // 绑定铁锅（菱形 4 输入）/ 烤箱（几何同大煮锅、配方独立）菜单与 GUI 屏幕
            MenuScreens.register(ModMenuTypes.IRON_WOK_MENU.get(), IronWokScreen::new);
            MenuScreens.register(ModMenuTypes.OVEN_MENU.get(), OvenScreen::new);
        });
    }
}
