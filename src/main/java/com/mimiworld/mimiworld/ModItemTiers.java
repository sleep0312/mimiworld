package com.mimiworld.mimiworld;

import net.minecraft.world.item.Tier;
import net.minecraft.world.item.crafting.Ingredient;

/**
 * 米米世界自定义工具/武器材料等级（编码 48、85-129 的剑与弓引用本类）。
 *
 * <p>等级链（从低到高）：米木（原版木制级别）&lt; 饭晶 &lt; 超结晶 &lt; 饭晶合金锭
 * &lt; 超重结晶锭 &lt; 复合米饭锭 &lt; 清洁复合锭 &lt; 超懒合金锭。</p>
 *
 * <p>数值逐档递增（耐久/挖掘速度/攻击加成/挖掘等级/附魔能力），修复材料全部用本模组已有物品：</p>
 * <ul>
 *   <li>饭晶 → 16 号结晶米</li>
 *   <li>超结晶 → 51 号超结晶</li>
 *   <li>饭晶合金 → 35 号饭晶合金锭</li>
 *   <li>超重结晶 → 55 号超重结晶</li>
 *   <li>复合米饭 → 60 号复合米饭锭</li>
 *   <li>清洁复合 → 75 号清洁复合锭</li>
 *   <li>超懒合金 → 80 号超懒合金锭</li>
 * </ul>
 */
public enum ModItemTiers implements Tier {
    /** 饭晶：约半档钻石，珍珠米白晶质（修复材料 16 号结晶米） */
    RICE_CRYSTAL(520, 7.0F, 2.5F, 2, 14, () -> Ingredient.of(ModItems.CRYSTALLIZED_RICE.get())),
    /** 超结晶：蓝晶含金核，钻石级（修复材料 51 号超结晶） */
    SUPER_CRYSTAL(900, 7.5F, 3.0F, 3, 16, () -> Ingredient.of(ModItems.SUPER_CRYSTAL.get())),
    /** 饭晶合金：与 36 号饭晶锤一致的钻石级（耐久 1561 / 速度 8 / 伤害 3 / 等级 3，修复材料 35 号饭晶合金锭） */
    RICE_CRYSTAL_ALLOY(1561, 8.0F, 3.0F, 3, 15, () -> Ingredient.of(ModItems.RICE_CRYSTAL_ALLOY_INGOT.get())),
    /** 超重结晶：慢速重武器路线，攻击加成最高之一（修复材料 55 号超重结晶） */
    SUPER_HEAVY_CRYSTAL(1900, 8.0F, 4.0F, 3, 12, () -> Ingredient.of(ModItems.SUPER_HEAVY_CRYSTAL.get())),
    /** 复合米饭：超钻石级（修复材料 60 号复合米饭锭） */
    COMPOUND_RICE_MEAL(2200, 9.0F, 4.0F, 4, 18, () -> Ingredient.of(ModItems.COMPOUND_RICE_MEAL_INGOT.get())),
    /** 清洁复合：更高一档（修复材料 75 号清洁复合锭） */
    CLEANSING_COMPOUND(2600, 9.5F, 4.5F, 4, 20, () -> Ingredient.of(ModItems.CLEANSING_COMPOUND_INGOT.get())),
    /** 超懒合金：终极材料（修复材料 80 号超懒合金锭） */
    SUPER_LAZY_ALLOY(3200, 10.0F, 5.0F, 4, 22, () -> Ingredient.of(ModItems.SUPER_LAZY_ALLOY_INGOT.get()));

    private final int uses;
    private final float speed;
    private final float attackDamageBonus;
    private final int level;
    private final int enchantmentValue;
    private final java.util.function.Supplier<Ingredient> repairIngredient;

    ModItemTiers(int uses, float speed, float attackDamageBonus, int level, int enchantmentValue,
                 java.util.function.Supplier<Ingredient> repairIngredient) {
        this.uses = uses;
        this.speed = speed;
        this.attackDamageBonus = attackDamageBonus;
        this.level = level;
        this.enchantmentValue = enchantmentValue;
        this.repairIngredient = repairIngredient;
    }

    @Override
    public int getUses() {
        return uses;
    }

    @Override
    public float getSpeed() {
        return speed;
    }

    @Override
    public float getAttackDamageBonus() {
        return attackDamageBonus;
    }

    @Override
    public int getLevel() {
        return level;
    }

    @Override
    public int getEnchantmentValue() {
        return enchantmentValue;
    }

    @Override
    public Ingredient getRepairIngredient() {
        // 运行期才调用，此时 ModItems 已完成注册
        return repairIngredient.get();
    }
}
