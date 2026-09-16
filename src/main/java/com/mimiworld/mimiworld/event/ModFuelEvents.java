package com.mimiworld.mimiworld.event;

import com.mimiworld.mimiworld.MimiWorld;
import com.mimiworld.mimiworld.ModItems;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.event.furnace.FurnaceFuelBurnTimeEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * 燃料燃烧时间事件（Forge EVENT_BUS）。
 * 熔炉每烧炼 1 个物品需 200 tick；米原木/米木板作为燃料可烧 3 个物品 = 600 tick。
 * 这里显式覆盖原版日志类标签给出的默认值（原木/木板默认 300 tick）。
 * 该事件在 {@link ForgeEventFactory#getItemBurnTime} 中触发，同时适用于熔炉/高炉/烟熏炉的燃料判定。
 */
@Mod.EventBusSubscriber(modid = MimiWorld.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ModFuelEvents {

    /** 熔炉每烧炼 1 个物品 = 200 tick。 */
    private static final int TICKS_PER_ITEM = 200;

    @SubscribeEvent
    public static void onFuelBurnTime(FurnaceFuelBurnTimeEvent event) {
        ItemStack stack = event.getItemStack();
        // 米原木/米木板：可烧 3 个物品 = 600 tick
        if (stack.is(ModItems.RICE_LOG.get()) || stack.is(ModItems.RICE_PLANKS.get())) {
            event.setBurnTime(3 * TICKS_PER_ITEM);
        }
        // 米木棍：可烧 1.5 个物品 = 300 tick
        else if (stack.is(ModItems.RICE_STICK.get())) {
            event.setBurnTime((int) (1.5F * TICKS_PER_ITEM));
        }
        // 米树叶：可烧 0.5 个物品 = 100 tick（原版树叶不是燃料，此处显式赋予）
        else if (stack.is(ModItems.RICE_LEAVES.get())) {
            event.setBurnTime(TICKS_PER_ITEM / 2);
        }
        // 可燃米：强燃料，可烧 32 个物品 = 6400 tick（介于烈焰棒 2400t 与煤炭块 16000t 之间）
        else if (stack.is(ModItems.COMBUSTIBLE_RICE.get())) {
            event.setBurnTime(32 * TICKS_PER_ITEM);
        }
    }
}
