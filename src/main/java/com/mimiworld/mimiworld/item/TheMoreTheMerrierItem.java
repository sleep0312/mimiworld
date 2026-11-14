package com.mimiworld.mimiworld.item;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import top.theillusivec4.curios.api.type.capability.ICurioItem;

import java.util.List;

/**
 * 多多益善（The More the Merrier）：Curios「圣遗物」槽位饰品。
 *
 * <p>佩戴期间：玩家造成伤害时，统计自身周围 10×10 水平范围（不限高度）内的怪物数量 x，
 * 最终伤害乘以 {@code 1 + 0.1 × x}，x 最高按 20 计算（即最多 +200%，×3）。
 * 判定逻辑见 {@link com.mimiworld.mimiworld.event.ModEffectEvents}。</p>
 */
public class TheMoreTheMerrierItem extends Item implements ICurioItem {

    public TheMoreTheMerrierItem(Properties properties) {
        super(properties);
    }

    @Override
    public List<Component> getAttributesTooltip(List<Component> tooltips, ItemStack stack) {
        tooltips.clear();
        tooltips.add(Component.empty());
        tooltips.add(Component.translatable("curios.modifiers.sacred_relic")
                .withStyle(ChatFormatting.GOLD));
        tooltips.add(Component.translatable("tooltip.mimiworld.the_more_the_merrier.count")
                .withStyle(ChatFormatting.BLUE));
        tooltips.add(Component.translatable("tooltip.mimiworld.the_more_the_merrier.cap")
                .withStyle(ChatFormatting.BLUE));
        return tooltips;
    }
}
