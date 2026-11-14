package com.mimiworld.mimiworld.item;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import top.theillusivec4.curios.api.type.capability.ICurioItem;

import java.util.List;

/**
 * 双倍快乐（Double Happiness）：Curios「圣遗物」槽位饰品。
 *
 * <p>佩戴期间：玩家主手与副手持有同一种物品时（只比较物品本身，忽略 NBT／耐久／附魔等），
 * 造成的伤害 ×2（判定逻辑见 {@link com.mimiworld.mimiworld.event.ModEffectEvents}）。</p>
 */
public class DoubleHappinessItem extends Item implements ICurioItem {

    public DoubleHappinessItem(Properties properties) {
        super(properties);
    }

    @Override
    public List<Component> getAttributesTooltip(List<Component> tooltips, ItemStack stack) {
        tooltips.clear();
        tooltips.add(Component.empty());
        tooltips.add(Component.translatable("curios.modifiers.sacred_relic")
                .withStyle(ChatFormatting.GOLD));
        tooltips.add(Component.translatable("tooltip.mimiworld.double_happiness.double")
                .withStyle(ChatFormatting.BLUE));
        return tooltips;
    }
}
