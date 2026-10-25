package com.mimiworld.mimiworld.enchantment;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;

/**
 * 完美无缺（flawless）—— 可附魔到任意物品上，使带耐久的物品不再消耗耐久。
 *
 * <p>最高 1 级，只有存在与否的区别。实际拦截逻辑由
 * {@link com.mimiworld.mimiworld.mixin.ItemStackMixin} 在 {@code ItemStack#hurt}
 * 入口处直接返回「未损坏」，因此工具、武器、盔甲、盾牌、鞘翅、钓鱼竿等
 * 所有走原版耐久扣除流程的物品都不会掉耐久。</p>
 */
public class FlawlessEnchantment extends Enchantment {

    public FlawlessEnchantment() {
        super(Rarity.VERY_RARE, EnchantmentCategory.BREAKABLE, new EquipmentSlot[]{
                EquipmentSlot.MAINHAND, EquipmentSlot.OFFHAND,
                EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET
        });
    }

    /** 任意物品均可附魔；没有耐久的物品不会消耗耐久，因此该附魔对其不产生实际效果。 */
    @Override
    public boolean canEnchant(ItemStack stack) {
        return true;
    }

    @Override
    public boolean canApplyAtEnchantingTable(ItemStack stack) {
        return canEnchant(stack);
    }

    @Override
    public int getMaxLevel() {
        return 1;
    }

    @Override
    public int getMinCost(int level) {
        return 20;
    }

    @Override
    public int getMaxCost(int level) {
        return 50;
    }
}
