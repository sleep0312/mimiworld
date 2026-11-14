package com.mimiworld.mimiworld.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;

/**
 * 心肺复苏（cardiopulmonary_resuscitation）—— 正面药水效果，仅 1 级。
 *
 * <p>每秒恢复 100 点生命值（受最大生命值上限约束，不会溢出；
 * 走 {@code LivingEntity#heal} 流程，因此会被「暗毒」等降低治疗量的效果影响）。</p>
 */
public class CardiopulmonaryResuscitationEffect extends MobEffect {

    /** 每次结算恢复的生命值。 */
    private static final float HEAL_PER_SECOND = 100.0F;

    public CardiopulmonaryResuscitationEffect(MobEffectCategory category, int color) {
        super(category, color);
    }

    /** 每 20 tick（1 秒）结算一次；效果只有 1 级，忽略 amplifier。 */
    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return duration > 0 && duration % 20 == 0;
    }

    @Override
    public void applyEffectTick(LivingEntity entity, int amplifier) {
        entity.heal(HEAL_PER_SECOND);
    }
}
