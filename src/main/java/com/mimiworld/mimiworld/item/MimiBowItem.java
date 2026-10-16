package com.mimiworld.mimiworld.item;

import com.mimiworld.mimiworld.entity.MimiArrowEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.Item;

/**
 * 米米世界弓物品：在 {@link BowItem} 基础上附带一个固定的伤害倍率。
 *
 * <p>发射时通过 {@link #customArrow} 把倍率写入生成的 {@link MimiArrowEntity}，
 * 命中伤害 = 箭的固定伤害 × 本弓倍率，从而与 8 种箭组合出各不相同的伤害。
 * 该倍率在箭命中时与满蓄力暴击叠加结算。</p>
 */
public class MimiBowItem extends BowItem {

    private final double damageMultiplier;

    /**
     * @param damageMultiplier 本弓的伤害倍率，最终伤害 = 箭基础伤害 × 该倍率
     */
    public MimiBowItem(double damageMultiplier, Item.Properties properties) {
        super(properties);
        this.damageMultiplier = damageMultiplier;
    }

    @Override
    public AbstractArrow customArrow(AbstractArrow arrow) {
        if (arrow instanceof MimiArrowEntity mimiArrow) {
            mimiArrow.setBowDamageMultiplier(this.damageMultiplier);
        }
        return super.customArrow(arrow);
    }
}
