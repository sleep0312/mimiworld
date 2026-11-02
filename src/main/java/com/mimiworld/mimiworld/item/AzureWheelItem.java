package com.mimiworld.mimiworld.item;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import top.theillusivec4.curios.api.type.capability.ICurioItem;

import java.util.List;

/**
 * 天青轮盘（Azure Wheel）：Curios「圣遗物」槽位饰品。
 *
 * <p>佩戴期间：玩家攻击命中时，50% 概率使本次伤害翻倍，50% 概率使本次伤害减半
 * （判定逻辑见 {@link com.mimiworld.mimiworld.event.ModEffectEvents}）。</p>
 */
public class AzureWheelItem extends Item implements ICurioItem {

    public AzureWheelItem(Properties properties) {
        super(properties);
    }

    @Override
    public List<Component> getAttributesTooltip(List<Component> tooltips, ItemStack stack) {
        tooltips.clear();
        tooltips.add(Component.empty());
        tooltips.add(Component.translatable("curios.modifiers.sacred_relic")
                .withStyle(ChatFormatting.GOLD));
        tooltips.add(Component.translatable("tooltip.mimiworld.azure_wheel.double")
                .withStyle(ChatFormatting.BLUE));
        tooltips.add(Component.translatable("tooltip.mimiworld.azure_wheel.half")
                .withStyle(ChatFormatting.BLUE));
        return tooltips;
    }
}
