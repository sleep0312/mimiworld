package com.mimiworld.mimiworld.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;

/**
 * 残堕化（remnant_corruption）—— 正面药水效果。
 *
 * <p>该效果本身不直接做任何事，实际的减伤逻辑由
 * {@link com.mimiworld.mimiworld.event.ModEffectEvents} 在 Forge LivingHurtEvent 中处理：
 * 受击者拥有此效果时，最终受到的伤害乘以 {@code 1 - 0.1 × (amplifier + 1)}，
 * 即每一级减少 10% 受到的伤害（I 级 -10%、II 级 -20%……），最低减到 0 点伤害。</p>
 */
public class RemnantCorruptionEffect extends MobEffect {

    public RemnantCorruptionEffect(MobEffectCategory category, int color) {
        super(category, color);
    }
}
