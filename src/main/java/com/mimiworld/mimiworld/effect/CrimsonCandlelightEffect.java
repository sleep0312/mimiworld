package com.mimiworld.mimiworld.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;

/**
 * 腥红烛照（crimson_candlelight）—— 正面药水效果。
 *
 * <p>该效果本身不直接做任何事，实际的伤害倍率逻辑由
 * {@link com.mimiworld.mimiworld.event.ModEffectEvents} 在 Forge LivingHurtEvent 中处理：
 * 遍历所有有效伤害源，若造成伤害的实体拥有此效果，则将最终伤害乘以 (amplifier + 2)。</p>
 *
 * <p>amplifier 从 0 开始（原版规则），因此效果等级 → 倍率的对应关系：
 * Level I = amplifier 0 → ×2，Level II = amplifier 1 → ×3，Level III = amplifier 2 → ×4，以此类推。</p>
 */
public class CrimsonCandlelightEffect extends MobEffect {

    public CrimsonCandlelightEffect(MobEffectCategory category, int color) {
        super(category, color);
    }
}
