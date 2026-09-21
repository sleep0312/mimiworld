package com.mimiworld.mimiworld;

import com.mimiworld.mimiworld.block.ModBlocks;
import com.mimiworld.mimiworld.block.entity.ModBlockEntities;
import com.mimiworld.mimiworld.entity.ModEntities;
import com.mimiworld.mimiworld.menu.ModMenuTypes;
import com.mimiworld.mimiworld.fluid.ModFluids;
import com.mojang.logging.LogUtils;
import software.bernie.geckolib.GeckoLib;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

// 米米世界主类，MODID 与 META-INF/mods.toml 中的条目一致
@Mod(MimiWorld.MODID)
public class MimiWorld
{
    public static final String MODID = "mimiworld";
    private static final Logger LOGGER = LogUtils.getLogger();

    public MimiWorld(FMLJavaModLoadingContext context)
    {
        IEventBus modEventBus = context.getModEventBus();

        // GeckoLib 初始化：内部完成网络注册与客户端渲染/重载监听等事件总线订阅，
        // 是后续自定义 Geo 生物、立体 Geo 盔甲、动画方块/物品的必要前置（有 hasInitialized 防重入）
        GeckoLib.initialize();

        // 挂载各注册器到 mod 事件总线
        ModBlocks.register(modEventBus);
        ModItems.register(modEventBus);
        ModFluids.register(modEventBus); // 米汤流体类型/源流体/流动流体/流体方块
        ModCreativeTabs.register(modEventBus);
        ModEntities.register(modEventBus);
        ModBlockEntities.register(modEventBus); // 造米台方块实体类型
        ModMenuTypes.register(modEventBus);     // 造米台 GUI 菜单类型
        // 米树 configured_feature 是动态注册表内容，1.20.1 只能用数据包 JSON 注册（data/mimiworld/worldgen/configured_feature/rice_tree.json），无 DeferredRegister

        // commonSetup 主线程阶段注册米桶↔空炼药锅交互（需等物品/方块注册表填充完成）
        modEventBus.addListener(this::commonSetup);

        // 注册到 Forge 事件总线，供后续服务端/游戏事件监听使用
        MinecraftForge.EVENT_BUS.register(this);

        LOGGER.info("MimiWorld mod loaded");
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(ModFluids::registerCauldronInteractions);
        // 工作方块（锤米台/压饭机）即时合成配方，需在注册表冻结后注册
        event.enqueueWork(ModMachineRecipes::register);
    }
}
