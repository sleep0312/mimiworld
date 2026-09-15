package com.mimiworld.mimiworld;

import com.mimiworld.mimiworld.block.ModBlocks;
import com.mimiworld.mimiworld.entity.ModEntities;
import com.mojang.logging.LogUtils;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
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

        // 挂载各注册器到 mod 事件总线
        ModBlocks.register(modEventBus);
        ModItems.register(modEventBus);
        ModCreativeTabs.register(modEventBus);
        ModEntities.register(modEventBus);
        // 米树 configured_feature 是动态注册表内容，1.20.1 只能用数据包 JSON 注册（data/mimiworld/worldgen/configured_feature/rice_tree.json），无 DeferredRegister

        // 注册到 Forge 事件总线，供后续服务端/游戏事件监听使用
        MinecraftForge.EVENT_BUS.register(this);

        LOGGER.info("MimiWorld mod loaded");
    }
}
