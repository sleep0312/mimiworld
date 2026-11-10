package com.mimiworld.mimiworld;

import net.minecraft.world.item.Tier;
import net.minecraft.world.item.crafting.Ingredient;

/**
 * 米米世界自定义工具/武器材料等级（本模组的剑与弓引用本类）。
 *
 * <p>等级链（从低到高）：米木（原版木制级别）&lt; 饭晶 &lt; 饭晶合金锭
 * &lt; 超重结晶锭 &lt; 复合米饭锭 &lt; 清洁复合锭 &lt; 超懒合金锭 &lt; 最懒天堕（堕落之心修复）。</p>
 *
 * <p>数值逐档递增（耐久/挖掘速度/攻击加成/挖掘等级/附魔能力），修复材料全部用本模组已有物品：</p>
 * <ul>
 *   <li>饭晶 → 结晶米</li>
 *   <li>饭晶合金 → 饭晶合金锭</li>
 *   <li>超重结晶 → 超重结晶</li>
 *   <li>复合米饭 → 复合米饭锭</li>
 *   <li>清洁复合 → 清洁复合锭</li>
 *   <li>超懒合金 → 超懒合金锭</li>
 * </ul>
 */
public enum ModItemTiers implements Tier {
    /** 饭晶：钻石级（耐久 1561 / 速度 7 / 伤害 3，剑总伤 7），珍珠米白晶质（修复材料结晶米） */
    RICE_CRYSTAL(1561, 7.0F, 3.0F, 2, 14, () -> Ingredient.of(ModItems.CRYSTALLIZED_RICE.get())),
    /** 饭晶合金：下界合金级强化的均衡档（耐久 2200 / 伤害 4.5，剑总伤 8.5，修复材料饭晶合金锭） */
    RICE_CRYSTAL_ALLOY(2200, 8.0F, 4.5F, 3, 15, () -> Ingredient.of(ModItems.RICE_CRYSTAL_ALLOY_INGOT.get())),
    /** 超重结晶：慢速重武器路线（耐久 2400 / 伤害 6，剑总伤 12、攻速 -2.8），修复材料超重结晶 */
    SUPER_HEAVY_CRYSTAL(2400, 8.0F, 6.0F, 3, 12, () -> Ingredient.of(ModItems.SUPER_HEAVY_CRYSTAL.get())),
    /** 复合米饭：均衡高伤档（耐久 2600 / 伤害 6.5，剑总伤 11.5，修复材料复合米饭锭） */
    COMPOUND_RICE_MEAL(2600, 9.0F, 6.5F, 4, 18, () -> Ingredient.of(ModItems.COMPOUND_RICE_MEAL_INGOT.get())),
    /** 清洁复合：更高一档（耐久 2800 / 伤害 7，剑总伤 12，修复材料清洁复合锭） */
    CLEANSING_COMPOUND(2800, 9.5F, 7.0F, 4, 20, () -> Ingredient.of(ModItems.CLEANSING_COMPOUND_INGOT.get())),
    /** 超懒合金：终极材料（耐久 3200 / 伤害 8，剑总伤 14，修复材料超懒合金锭） */
    SUPER_LAZY_ALLOY(3200, 10.0F, 8.0F, 4, 22, () -> Ingredient.of(ModItems.SUPER_LAZY_ALLOY_INGOT.get())),
    /** 最懒天堕：超懒合金之上的传说级（耐久 3800 / 伤害 8.5；最懒天堕镐；修复材料堕落之心） */
    LAZIEST_HEAVENFALL(3800, 11.0F, 8.5F, 4, 24, () -> Ingredient.of(ModItems.FALLEN_HEART.get())),
    /** 残堕：龙系之上的终末级（耐久 4200 / 伤害 10，剑总伤 16；修复材料终末龙逆鳞） */
    REMNANT_FALLEN(4200, 12.0F, 10.0F, 4, 26, () -> Ingredient.of(ModItems.END_DRAGON_REVERSE_SCALE.get()));

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
        ModConfig.WeaponStats stats = ModConfig.weapon(name().toLowerCase(java.util.Locale.ROOT));
        return stats == null ? uses : ModConfig.read(stats.uses());
    }

    @Override
    public float getSpeed() {
        ModConfig.WeaponStats stats = ModConfig.weapon(name().toLowerCase(java.util.Locale.ROOT));
        return stats == null ? speed : (float) (double) ModConfig.read(stats.miningSpeed());
    }

    @Override
    public float getAttackDamageBonus() {
        ModConfig.WeaponStats stats = ModConfig.weapon(name().toLowerCase(java.util.Locale.ROOT));
        return stats == null ? attackDamageBonus : (float) (double) ModConfig.read(stats.attackDamageBonus());
    }

    @Override
    public int getLevel() {
        return level;
    }

    @Override
    public int getEnchantmentValue() {
        ModConfig.WeaponStats stats = ModConfig.weapon(name().toLowerCase(java.util.Locale.ROOT));
        return stats == null ? enchantmentValue : ModConfig.read(stats.enchantmentValue());
    }

    @Override
    public Ingredient getRepairIngredient() {
        // 运行期才调用，此时 ModItems 已完成注册
        return repairIngredient.get();
    }

    // ===== 供 ModConfig 构建配置默认值使用的原始数值（避免默认值两处维护） =====

    int rawUses() {
        return uses;
    }

    float rawSpeed() {
        return speed;
    }

    float rawAttackDamageBonus() {
        return attackDamageBonus;
    }

    int rawEnchantmentValue() {
        return enchantmentValue;
    }
}
