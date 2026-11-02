package com.mimiworld.mimiworld.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;

/**
 * 易怒（irritable）—— 正面药水效果。
 *
 * <p>该效果本身不直接做任何事，实际逻辑由
 * {@link com.mimiworld.mimiworld.event.ModEffectEvents} 在 LivingHurtEvent 中处理：</p>
 * <ul>
 *   <li>受到任意伤害时，开始/刷新 1 秒的「易怒」窗口；</li>
 *   <li>窗口内的攻击有 25% 概率使本次伤害增加 5%×等级（I 级 +5%、II 级 +10%……），
 *       等级上限 5 级（+25%）。</li>
 * </ul>
 */
public class IrritableEffect extends MobEffect {

    public IrritableEffect(MobEffectCategory category, int color) {
        super(category, color);
    }
}
