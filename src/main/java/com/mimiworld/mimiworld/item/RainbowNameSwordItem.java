package com.mimiworld.mimiworld.item;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tier;

/**
 * 「超懒神剑」系列：真正的剑类武器（可近战、带耐久与附魔，走剑的横扫/暴击判定），
 * 同时保留 {@link RainbowNameItem} 的逐字动态彩虹名称。
 */
public class RainbowNameSwordItem extends SwordItem {

    public RainbowNameSwordItem(Tier tier, int attackDamageModifier, float attackSpeedModifier, Properties properties) {
        super(tier, attackDamageModifier, attackSpeedModifier, properties);
    }

    @Override
    public Component getName(ItemStack stack) {
        return RainbowNameItem.rainbow(Component.translatable(getDescriptionId(stack)).getString());
    }
}
