package com.mimiworld.mimiworld.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;

/**
 * 蟒蛇缠绕（python_constriction）—— 复晶天青蟒施加的负面效果，兼具两种机制：
 *
 * <ol>
 *   <li><b>持续绞压伤害</b>：每秒钟造成 {@code 10 × (amplifier + 1)} 点魔法伤害
 *       （无视护甲，可致死）。即 I 级每秒 10 点、II 级每秒 20 点、III 级每秒 30 点，以此类推。</li>
 *   <li><b>弱点标记（易伤）</b>：由
 *       {@link com.mimiworld.mimiworld.event.ModEffectEvents} 在 LivingHurtEvent 中实现——
 *       拥有该效果的玩家攻击复晶天青蟒时，对其造成的伤害提升至原来的 150%。</li>
 * </ol>
 *
 * <p>两种机制互不冲突：伤害结算由本效果自身每 tick 完成（无伤害来源实体），
 * 易伤结算只在「玩家 → 蟒」的受伤事件中触发，因此不会产生循环加成。</p>
 */
public class PythonConstrictionEffect extends MobEffect {

    /** 每级每秒造成的基础伤害（amplifier 0 = I 级 → 10 点/秒）。 */
    private static final float DAMAGE_PER_LEVEL = 10.0F;

    public PythonConstrictionEffect(MobEffectCategory category, int color) {
        super(category, color);
    }

    /** 每 20 tick（1 秒）结算一次，与等级无关。 */
    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return duration > 0 && duration % 20 == 0;
    }

    @Override
    public void applyEffectTick(LivingEntity entity, int amplifier) {
        // 魔法伤害：无视护甲与抗性提升，无攻击者实体（不会触发易伤事件，也不会给蟒回血/触发荆棘）
        entity.hurt(entity.level().damageSources().magic(), DAMAGE_PER_LEVEL * (amplifier + 1));
    }
}
