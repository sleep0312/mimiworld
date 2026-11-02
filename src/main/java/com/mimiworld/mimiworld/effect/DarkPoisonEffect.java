package com.mimiworld.mimiworld.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;

/**
 * 暗毒（dark_poison）—— 负面药水效果。
 *
 * <p>该效果本身不做任何事，实际逻辑由
 * {@link com.mimiworld.mimiworld.event.ModEffectEvents} 在 LivingHealEvent 中处理：
 * 携带者的一切生命恢复（自然回血、生命恢复效果、治疗药水等）只保留 25%，
 * 即恢复量 -75%。</p>
 */
public class DarkPoisonEffect extends MobEffect {

    public DarkPoisonEffect(MobEffectCategory category, int color) {
        super(category, color);
    }
}
