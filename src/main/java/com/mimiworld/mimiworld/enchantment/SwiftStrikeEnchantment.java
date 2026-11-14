package com.mimiworld.mimiworld.enchantment;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;

/**
 * 迅击（swift_strike）—— 近战武器附魔，最高 5 级。
 *
 * <p>效果：造成伤害时，把 {@code 附魔等级 × 当前移动速度值} 计入乘法区，
 * 与其它百分比加成求和后统一乘以 {@code 1 + 合计}。
 * 以玩家默认移动速度 0.1 为例：I 级 +10%、II 级 +20%、III 级 +30%、IV 级 +40%、V 级 +50%；
 * 速度被药水或其它效果提升后百分比随之提高。</p>
 *
 * <p>实际倍率结算由 {@link com.mimiworld.mimiworld.event.DamagePipeline}
 * 在阶段 1（LivingHurtEvent，护甲前）的乘法区中处理。</p>
 */
public class SwiftStrikeEnchantment extends Enchantment {

    public SwiftStrikeEnchantment() {
        super(Rarity.RARE, EnchantmentCategory.WEAPON, new EquipmentSlot[]{EquipmentSlot.MAINHAND});
    }

    /** 书本用于承载附魔书，其余为本类附魔支持的近战武器（剑/斧/三叉戟）。 */
    @Override
    public boolean canEnchant(ItemStack stack) {
        return stack.is(Items.BOOK) || EnchantmentCategory.WEAPON.canEnchant(stack.getItem());
    }

    @Override
    public boolean canApplyAtEnchantingTable(ItemStack stack) {
        return canEnchant(stack);
    }

    @Override
    public int getMaxLevel() {
        return 5;
    }

    @Override
    public int getMinCost(int level) {
        return 5 + (level - 1) * 8;
    }

    @Override
    public int getMaxCost(int level) {
        return getMinCost(level) + 20;
    }
}
