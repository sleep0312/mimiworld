package com.mimiworld.mimiworld.client;

import com.mimiworld.mimiworld.MimiWorld;
import com.mimiworld.mimiworld.block.ModBlocks;
import com.mimiworld.mimiworld.block.entity.ModBlockEntities;
import com.mimiworld.mimiworld.client.renderer.CookingPotGeoBlockRenderer;
import com.mimiworld.mimiworld.client.renderer.DiaomikulouRenderer;
import com.mimiworld.mimiworld.client.renderer.DragonBeamRenderer;
import com.mimiworld.mimiworld.client.renderer.FallenRicePhoenixRenderer;
import com.mimiworld.mimiworld.client.renderer.LaziestFinalDragonRenderer;
import com.mimiworld.mimiworld.client.renderer.DibazongzuilongRenderer;
import com.mimiworld.mimiworld.client.renderer.LazyElfRenderer;
import com.mimiworld.mimiworld.client.renderer.MimiSheepRenderer;
import com.mimiworld.mimiworld.client.renderer.MimiMonkeyKingRenderer;
import com.mimiworld.mimiworld.client.renderer.MimiMonkeyHeadBlockRenderer;
import com.mimiworld.mimiworld.client.renderer.RiceCrystalTigerKingHeadBlockRenderer;
import com.mimiworld.mimiworld.client.renderer.SuperCrystalOxHeadBlockRenderer;
import com.mimiworld.mimiworld.client.renderer.HeavyCrystalRabbitKingHeadBlockRenderer;
import com.mimiworld.mimiworld.client.renderer.CompoundCrystalAzurePythonHeadBlockRenderer;
import com.mimiworld.mimiworld.client.renderer.PurifiedLongevityKuiHeadBlockRenderer;
import com.mimiworld.mimiworld.client.renderer.LaziestEndDragonHeadBlockRenderer;
import com.mimiworld.mimiworld.client.renderer.DibazongzuiHeadBlockRenderer;
import com.mimiworld.mimiworld.client.renderer.FallenCrossBlockRenderer;
import com.mimiworld.mimiworld.client.renderer.MimiTeleporterBlockRenderer;
import com.mimiworld.mimiworld.client.renderer.FanjinghuwangRenderer;
import com.mimiworld.mimiworld.client.renderer.ChaojingniuwangRenderer;
import com.mimiworld.mimiworld.client.renderer.CompoundAzurePythonRenderer;
import com.mimiworld.mimiworld.client.renderer.HeavyCrystalRabbitKingRenderer;
import com.mimiworld.mimiworld.client.renderer.DaomizheRenderer;
import com.mimiworld.mimiworld.client.renderer.MimouniuRenderer;
import com.mimiworld.mimiworld.client.renderer.MihengPigCorpseRenderer;
import com.mimiworld.mimiworld.client.renderer.MihengPigRenderer;
import com.mimiworld.mimiworld.client.renderer.ThrownMonkeyStaffRenderer;
import com.mimiworld.mimiworld.client.renderer.RiceSoulRenderer;
import com.mimiworld.mimiworld.client.renderer.RiceWolfVultureRenderer;
import com.mimiworld.mimiworld.client.renderer.PenmiFishRenderer;
import com.mimiworld.mimiworld.client.renderer.QingjiechangmingkuiRenderer;
import com.mimiworld.mimiworld.client.renderer.QianranjiaoRenderer;
import com.mimiworld.mimiworld.client.renderer.MishenRenderer;
import com.mimiworld.mimiworld.client.renderer.WangmishangjiaoRenderer;
import com.mimiworld.mimiworld.client.renderer.RiceGolemRenderer;
import com.mimiworld.mimiworld.client.renderer.HaomiWolfRenderer;
import com.mimiworld.mimiworld.client.renderer.BaoliemilingRenderer;
import com.mimiworld.mimiworld.client.renderer.ChaoticVoidToucherRenderer;
import com.mimiworld.mimiworld.client.renderer.HumiCorpseRenderer;
import com.mimiworld.mimiworld.client.renderer.HualingmiRenderer;
import com.mimiworld.mimiworld.client.renderer.JietianmiRenderer;
import com.mimiworld.mimiworld.client.renderer.MeimiruRenderer;
import com.mimiworld.mimiworld.client.renderer.MilaimaRenderer;
import com.mimiworld.mimiworld.client.renderer.PamiCrawlerRenderer;
import com.mimiworld.mimiworld.client.renderer.RiceWitchRenderer;
import com.mimiworld.mimiworld.client.renderer.MimiSkeletonRenderer;
import com.mimiworld.mimiworld.client.renderer.StickyRiceSpiderRenderer;
import com.mimiworld.mimiworld.client.renderer.TanmiFishRenderer;
import com.mimiworld.mimiworld.client.renderer.WujihuaRenderer;
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
import net.minecraft.client.renderer.entity.TippableArrowRenderer;
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
        // 虚无触毒液：ThrownItemRenderer 渲染史莱姆球贴图
        event.registerEntityRenderer(ModEntities.VOID_POISON_SPIT.get(), ThrownItemRenderer::new);
        // 米米世界箭矢：沿用原版箭模型与 ARROW 模型层贴图
        event.registerEntityRenderer(ModEntities.MIMI_ARROW.get(), TippableArrowRenderer::new);
        // 米米猴王扔出的金箍棒：独立 GeckoLib 投掷物模型（复用猴王贴图）
        event.registerEntityRenderer(ModEntities.THROWN_MONKEY_STAFF.get(), ThrownMonkeyStaffRenderer::new);

        // 七个待定 Boss 统一使用 Steve 人形模型占位，真实 GeckoLib 模型实现后在此替换
        // 米米猴王：已转正，GeckoLib mimihouwang 模型（idle/walk/attack/attack_slam/throw 动画）
        event.registerEntityRenderer(ModEntities.MIMI_MONKEY_KING.get(), MimiMonkeyKingRenderer::new);
        // 饭晶虎王：已转正，GeckoLib fanjinghuwang 模型（idle/walk/attack/feast/slam 动画）
        event.registerEntityRenderer(ModEntities.RICE_CRYSTAL_TIGER_KING.get(), FanjinghuwangRenderer::new);
        // 超晶牛王：已转正，GeckoLib super_crystal_cow_king 模型（idle/walk/attack/axe_combo/charge_start/charge 动画）
        event.registerEntityRenderer(ModEntities.SUPER_CRYSTAL_COW_KING.get(), ChaojingniuwangRenderer::new);
        // 重晶兔王：已转正，GeckoLib heavy_crystal_rabbit_king 模型（idle/walk/attack/skill_slam/skill_enrage 动画）
        event.registerEntityRenderer(ModEntities.HEAVY_CRYSTAL_RABBIT_KING.get(), HeavyCrystalRabbitKingRenderer::new);
        // 复晶天青蟒：已转正，GeckoLib compound_azure_python 模型（idle/walk/attack/bite/kiss/tail_sweep 动画）
        event.registerEntityRenderer(ModEntities.COMPOUND_AZURE_PYTHON.get(), CompoundAzurePythonRenderer::new);
        // 清洁长命夔：已转正，GeckoLib qingjiechangmingkui 模型（idle/walk/attack/roar/stomp/triple_bite 动画）
        event.registerEntityRenderer(ModEntities.CLEANSING_LONGEVITY_KUI.get(), QingjiechangmingkuiRenderer::new);
        // 化灵米：GeckoLib hualingmi 模型（animation.hualingmi.idle/fly/attack 动画），常驻悬浮飞行近战
        event.registerEntityRenderer(ModEntities.HUALING_RICE.get(), HualingmiRenderer::new);
        // 最懒终末龙：GeckoLib 宗罪龙模型（idle/flying 动画），其余六个 Boss 仍用 Steve 占位
        event.registerEntityRenderer(ModEntities.LAZIEST_FINAL_DRAGON.get(), LaziestFinalDragonRenderer::new);
        // 第八宗罪龙：最终 Boss，GeckoLib 宗罪龙模型（451 方块/2048 贴图，六动画复用最懒终末龙）
        event.registerEntityRenderer(ModEntities.DIBAZONGZUILONG.get(), DibazongzuilongRenderer::new);
        // 最懒光束：纯视觉光束载体（紫色实心光束，从龙嘴渲染到锁定玩家）
        event.registerEntityRenderer(ModEntities.DRAGON_BEAM.get(), DragonBeamRenderer::new);
        // 盗米者：GeckoLib daomizhe 狼人模型（animation.daomizhe.idle/walk/attack 动画），偷米扑击近战
        event.registerEntityRenderer(ModEntities.RICE_STEALER.get(), DaomizheRenderer::new);
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
        // 没米入：GeckoLib meimiru 模型（animation.idle/walk/attack 动画，末影暗紫+米白+金稻穗贴图），渲染放大 1.15
        event.registerEntityRenderer(ModEntities.MOMI_ENTRANT.get(), MeimiruRenderer::new);
        // 爆烈米灵：GeckoLib baoliemiling 模型（animation.baoliemiling.idle/walk/attack 动画，白米怒脸+金米棒贴图），烈焰人式飞行敌对
        event.registerEntityRenderer(ModEntities.EXPLOSIVE_RICE_SPIRIT.get(), BaoliemilingRenderer::new);
        // 雕米骷髅：GeckoLib diaomikulou 模型（animation.diaomikulou.idle/walk/attack 动画，米白骨+金稻穗腰带+黑布裙+骨角爪+魂火眼贴图）
        event.registerEntityRenderer(ModEntities.CARVED_RICE_SKELETON.get(), DiaomikulouRenderer::new);
        // 米莱马：GeckoLib milaima 模型（animation.milaima.idle/walk/attack 动画，米浆凝胶+米粒+酱油眼+稻绿嫩芽贴图），
        // 史莱姆型敌对生物，渲染放大 1.3 对齐 1.02×1.02 碰撞箱
        event.registerEntityRenderer(ModEntities.MILAI_HORSE.get(), MilaimaRenderer::new);
        // 米哼猪尸：GeckoLib mihengzhushi 模型（animation.mihengzhushi.idle/walk/attack 动画），陆地敌对亡灵
        event.registerEntityRenderer(ModEntities.MIHENG_PIG_CORPSE.get(), MihengPigCorpseRenderer::new);
        // 米魂：GeckoLib mihun 模型（animation.mihun.idle/walk/attack 动画），敌对飞行魂体
        event.registerEntityRenderer(ModEntities.RICE_SOUL.get(), RiceSoulRenderer::new);
        // 污棘花：GeckoLib wujihua 模型（animation.wujihua.idle/attack 动画），扎根不动敌对植物
        event.registerEntityRenderer(ModEntities.BLIGHTED_THORN_FLOWER.get(), WujihuaRenderer::new);
        // 混沌虚无触：GeckoLib hundunbukemingzhuangzhimijiexuwuchu 模型（idle/attack/attack_spit 动画），
        // 扎根不动触手巢 Boss（触手抽击+张口喷毒），渲染放大 1.5 倍
        event.registerEntityRenderer(ModEntities.CHAOTIC_VOID_TOUCHER.get(), ChaoticVoidToucherRenderer::new);
        // 哮噬米狼鹫：GeckoLib xiaoshimilangjiu 模型（idle/walk/attack_bite/attack_dive 动画），陆空敌对狼鹫
        event.registerEntityRenderer(ModEntities.RICE_WOLF_VULTURE.get(), RiceWolfVultureRenderer::new);
        // 堕落米凤：GeckoLib duoluomifeng 模型（animation.idle/walk/attack/flying 动画），幻翼式巡航俯冲+落地休息
        event.registerEntityRenderer(ModEntities.FALLEN_RICE_PHOENIX.get(), FallenRicePhoenixRenderer::new);
        // 浅染鲛：GeckoLib qianranjiao 模型（idle/walk/attack 动画），水生敌对鲸类，纵向摆尾巡游+突进啃咬
        event.registerEntityRenderer(ModEntities.PALE_MERMAID.get(), QianranjiaoRenderer::new);
        // 望米伤鲛：GeckoLib wangmishangjiao 巨型古鲸模型（idle/walk/attack 动画），深海巨兽冲刺啃咬
        event.registerEntityRenderer(ModEntities.WANGMI_WOUNDED_MERMAID.get(), WangmishangjiaoRenderer::new);
        // 米蜃：GeckoLib mishen 爬虫模型（idle/walk/attack 动画），陆地敌对蠹虫式贴地疾冲大颚啃咬
        event.registerEntityRenderer(ModEntities.RICE_MIRAGE.get(), MishenRenderer::new);
        // 懒精灵：GeckoLib lanjingling 小人型模型（idle/walk/attack 动画），陆地敌对哥布林式碎步扑击撕咬
        event.registerEntityRenderer(ModEntities.LAZY_ELF.get(), LazyElfRenderer::new);
        // 界天米：GeckoLib jietianmi 独眼飞行模型（idle/walk/attack 动画），守卫者式凝视蓄力激光，渲染放大 1.2
        event.registerEntityRenderer(ModEntities.JIETIAN_RICE.get(), JietianmiRenderer::new);

        // 煮锅系列五方块：GeckoLib geo 模型（无动画，静态渲染）。
        // 体型按注册顺序递减（参考堕落十字架的做法，在 BER 内等比缩放收进一格）：
        // 巨无霸大大大煮锅 = 16px = 占满一整个方块，其余依次缩小，最小的 6px 仍清晰可见。
        event.registerBlockEntityRenderer(ModBlockEntities.SMALL_COOKING_POT.get(),
                ctx -> new CookingPotGeoBlockRenderer<>(
                        new ResourceLocation(MimiWorld.MODID, "small_cooking_pot"), 6.0F / 12.8F));
        event.registerBlockEntityRenderer(ModBlockEntities.COOKING_POT.get(),
                ctx -> new CookingPotGeoBlockRenderer<>(
                        new ResourceLocation(MimiWorld.MODID, "cooking_pot"), 8.0F / 16.64F));
        event.registerBlockEntityRenderer(ModBlockEntities.LARGE_COOKING_POT.get(),
                ctx -> new CookingPotGeoBlockRenderer<>(
                        new ResourceLocation(MimiWorld.MODID, "large_cooking_pot"), 10.5F / 21.632F));
        event.registerBlockEntityRenderer(ModBlockEntities.EXTRA_LARGE_COOKING_POT.get(),
                ctx -> new CookingPotGeoBlockRenderer<>(
                        new ResourceLocation(MimiWorld.MODID, "extra_large_cooking_pot"), 13.0F / 28.16F));
        event.registerBlockEntityRenderer(ModBlockEntities.JUMBO_MEGA_COOKING_POT.get(),
                ctx -> new CookingPotGeoBlockRenderer<>(
                        new ResourceLocation(MimiWorld.MODID, "jumbo_mega_cooking_pot"), 16.0F / 36.608F));
        // 铁锅：GeckoLib 平底锅 geo 模型（模型本身按 16px 内设计，1:1 渲染）
        event.registerBlockEntityRenderer(ModBlockEntities.IRON_WOK.get(),
                ctx -> new CookingPotGeoBlockRenderer<>(
                        new ResourceLocation(MimiWorld.MODID, "iron_wok"), 1.0F));
        // 米米猴头：GeckoLib 头颅摆件方块（head+crown 静态模型，渲染器内缩放摆正贴地居中）
        event.registerBlockEntityRenderer(ModBlockEntities.MIMI_MONKEY_HEAD.get(),
                ctx -> new MimiMonkeyHeadBlockRenderer<>(ctx));
        // 饭晶虎王头：GeckoLib 头颅摆件方块（head+双耳+冠+下颌静态模型）
        event.registerBlockEntityRenderer(ModBlockEntities.RICE_CRYSTAL_TIGER_KING_HEAD.get(),
                ctx -> new RiceCrystalTigerKingHeadBlockRenderer<>(ctx));
        // 超晶牛头：GeckoLib 头颅摆件方块（head+双角+双耳静态模型）
        event.registerBlockEntityRenderer(ModBlockEntities.SUPER_CRYSTAL_OX_HEAD.get(),
                ctx -> new SuperCrystalOxHeadBlockRenderer<>(ctx));
        // 重晶兔王头：GeckoLib 头颅摆件方块（head+长双耳静态模型）
        event.registerBlockEntityRenderer(ModBlockEntities.HEAVY_CRYSTAL_RABBIT_KING_HEAD.get(),
                ctx -> new HeavyCrystalRabbitKingHeadBlockRenderer<>(ctx));
        // 复晶天青蟒头：GeckoLib 头颅摆件方块（head+下颌+舌+角静态模型）
        event.registerBlockEntityRenderer(ModBlockEntities.COMPOUND_CRYSTAL_AZURE_PYTHON_HEAD.get(),
                ctx -> new CompoundCrystalAzurePythonHeadBlockRenderer<>(ctx));
        // 清洁长命夔头：GeckoLib 头颅摆件方块（head+长颌静态模型）
        event.registerBlockEntityRenderer(ModBlockEntities.PURIFIED_LONGEVITY_KUI_HEAD.get(),
                ctx -> new PurifiedLongevityKuiHeadBlockRenderer<>(ctx));
        // 最懒终末龙头：GeckoLib 头颅摆件方块（Promax 龙头+下颌静态模型）
        event.registerBlockEntityRenderer(ModBlockEntities.LAZIEST_END_DRAGON_HEAD.get(),
                ctx -> new LaziestEndDragonHeadBlockRenderer<>(ctx));
        // 第八宗罪龙头：GeckoLib 头颅摆件方块（龙头+下颌静态模型，渲染器内 16/150 缩放收进一格贴地居中）
        event.registerBlockEntityRenderer(ModBlockEntities.DIBAZONGZUI_HEAD.get(),
                ctx -> new DibazongzuiHeadBlockRenderer<>(ctx));
        // 堕落十字架：GeckoLib 华丽十字架方块（基座+紫黑十字+金纹+双面宝石静态模型，渲染器内 16/27 缩放收进一格贴地居中）
        event.registerBlockEntityRenderer(ModBlockEntities.FALLEN_CROSS.get(),
                ctx -> new FallenCrossBlockRenderer<>(ctx));
        // 米米传送器：方块 JSON 模型照常渲染，BER 仅在插眼后叠加正上方悬浮缓慢旋转的米米之眼
        event.registerBlockEntityRenderer(ModBlockEntities.MIMI_TELEPORTER.get(),
                MimiTeleporterBlockRenderer::new);
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
