package com.mimiworld.mimiworld.item;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import top.theillusivec4.curios.api.type.capability.ICurioItem;

import java.util.List;

/**
 * 虎王之蔑（Tiger King's Disdain）：圣遗物槽位饰品。
 *
 * <p>佩戴期间，玩家受到的任意伤害不会立即结算，而是由
 * {@code ModEffectEvents} 拆成 5 次、每次原伤害的 20%、每 1 秒结算一次；
 * 多次伤害各自独立计时。</p>
 */
public class TigerKingDisdainItem extends Item implements ICurioItem {

    public TigerKingDisdainItem(Properties properties) {
        super(properties);
    }

    /**
     * 用蓝色说明行重建「佩戴于圣遗物时：」区块（本饰品不提供属性修饰符）。
     */
    @Override
    public List<Component> getAttributesTooltip(List<Component> tooltips, ItemStack stack) {
        tooltips.clear();
        tooltips.add(Component.empty());
        tooltips.add(Component.translatable("curios.modifiers.sacred_relic")
                .withStyle(ChatFormatting.GOLD));
        tooltips.add(Component.translatable("tooltip.mimiworld.tiger_king_disdain.installments")
                .withStyle(ChatFormatting.BLUE));
        tooltips.add(Component.translatable("tooltip.mimiworld.tiger_king_disdain.interval")
                .withStyle(ChatFormatting.BLUE));
        return tooltips;
    }
}
