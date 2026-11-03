package com.mimiworld.mimiworld.item;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import top.theillusivec4.curios.api.type.capability.ICurioItem;

import java.util.List;

/**
 * 领域展开（Domain Expansion）：Curios「圣遗物」槽位饰品。
 *
 * <p>佩戴期间，以玩家为中心 10×10×10 范围内的敌对生物护甲值 -50%，
 * 具体判定见 {@link com.mimiworld.mimiworld.event.DomainExpansionEvents}。</p>
 */
public class DomainExpansionItem extends Item implements ICurioItem {

    public DomainExpansionItem(Properties properties) {
        super(properties);
    }

    @Override
    public List<Component> getAttributesTooltip(List<Component> tooltips, ItemStack stack) {
        tooltips.clear();
        tooltips.add(Component.empty());
        tooltips.add(Component.translatable("curios.modifiers.sacred_relic")
                .withStyle(ChatFormatting.GOLD));
        tooltips.add(Component.translatable("tooltip.mimiworld.domain_expansion.armor")
                .withStyle(ChatFormatting.BLUE));
        return tooltips;
    }
}
