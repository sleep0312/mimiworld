package com.mimiworld.mimiworld.datagen;

import com.mimiworld.mimiworld.MimiWorld;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.minecraftforge.data.event.GatherDataEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * 数据生成器入口：仅在运行 runData（GatherDataEvent）时触发，不影响正常游戏加载。
 * 生成物输出到 src/generated/resources，已由 build.gradle 配置进主资源源集。
 */
@Mod.EventBusSubscriber(modid = MimiWorld.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class DataGenerators {

    @SubscribeEvent
    public static void gatherData(GatherDataEvent event) {
        DataGenerator generator = event.getGenerator();
        PackOutput packOutput = generator.getPackOutput();

        // 服务端数据（配方、实体战利品表等）
        if (event.includeServer()) {
            generator.addProvider(true, new ModRecipeProvider(packOutput));
            generator.addProvider(true, new ModLootTableProvider(packOutput));
            generator.addProvider(true, new ModItemTagsProvider(packOutput, event.getLookupProvider(),
                    event.getExistingFileHelper()));
        }
    }
}
