package com.mimiworld.mimiworld;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Items;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/**
 * 米米世界自定义创造模式标签页。
 * 标签页 ID：mimi（完整注册名 mimiworld:mimi）
 * 自动收录所有命名空间为 mimiworld 的已注册物品（方块物品 BlockItem 也在物品注册表中，会一并收录）。
 */
public class ModCreativeTabs {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MimiWorld.MODID);

    public static final RegistryObject<CreativeModeTab> MIMI_TAB = CREATIVE_MODE_TABS.register("mimi", () ->
            CreativeModeTab.builder()
                    // TODO: 图标物品待定，暂用原版草方块占位，确定后替换为对应 Item
                    .icon(() -> Items.GRASS_BLOCK.getDefaultInstance())
                    .title(Component.translatable("itemGroup.mimiworld.mimi"))
                    .displayItems((parameters, output) ->
                            // 自动添加本模组所有已注册物品/方块物品；
                            // placeholder_ 前缀为锁定编码用的占位物品（当前已无占位物），不显示
                            ForgeRegistries.ITEMS.getEntries().stream()
                                    .filter(entry -> entry.getKey().location().getNamespace().equals(MimiWorld.MODID))
                                    .filter(entry -> !entry.getKey().location().getPath().startsWith("placeholder_"))
                                    .forEach(entry -> output.accept(entry.getValue())))
                    .build());

    public static void register(IEventBus modEventBus) {
        CREATIVE_MODE_TABS.register(modEventBus);
    }
}
