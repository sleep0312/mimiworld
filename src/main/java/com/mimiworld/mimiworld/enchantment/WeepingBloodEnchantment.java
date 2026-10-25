package com.mimiworld.mimiworld.enchantment;

import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;

/**
 * 泣血（weeping_blood）—— 仅可附魔在剑类武器上的自定义附魔。
 *
 * <p>效果：持有附魔剑的实体造成近战伤害时，按最终伤害值回复自身生命，
 * 1 级回复 6%，每提升 1 级额外增加 2%（I 级 6%、II 级 8%、III 级 10%……）。
 * 实际回复逻辑由 {@link com.mimiworld.mimiworld.event.ModEnchantmentEvents}
 * 在 Forge LivingDamageEvent 中结算。</p>
 */
public class WeepingBloodEnchantment extends Enchantment {

    public WeepingBloodEnchantment() {
        super(Rarity.RARE, EnchantmentCategory.WEAPON, new EquipmentSlot[]{EquipmentSlot.MAINHAND});
    }

    /**
     * 仅允许剑类武器与书本：书本用于承载附魔书（与原版附魔一致，可在附魔台刷出对应等级的泣血书），
     * 最终可附魔的实际装备仍限定为剑类。
     * 本模组的剑均继承 {@link SwordItem}，其余剑以原版 swords 物品标签兜底。
     */
    @Override
    public boolean canEnchant(ItemStack stack) {
        return stack.is(Items.BOOK) || stack.getItem() instanceof SwordItem || stack.is(ItemTags.SWORDS);
    }

    /** 附魔台只接受剑类武器或书本。 */
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
