package com.mimiworld.mimiworld.item;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.List;
import java.util.function.Supplier;

/**
 * 带自定义食用效果的食物物品。
 *
 * <p>食用完成后，先由原版 {@link Item#finishUsingItem} 结算饱食度/饱和度与容器返还，
 * 再统一施加本类配置的效果列表（列表在 {@code ModItems} 中按「效果等级 + 持续时间」声明）：</p>
 * <ul>
 *   <li><b>普通持续效果</b>：以 {@link MobEffectInstance} 施加（带图标、可被牛奶清除）。</li>
 *   <li><b>创造飞行</b>：自定义效果 {@code creative_flight}，飞行能力由
 *       {@link com.mimiworld.mimiworld.event.ModEffectEvents} 逐 tick 授予，效果结束后撤销。</li>
 *   <li><b>瞬间类效果</b>（瞬间治疗/瞬间恢复 = {@link MobEffects#HEAL}）：食用瞬间直接结算治疗量。
 *       原版公式 {@code 4 << amplifier} 在 amplifier ≥ 30 时会因 int 溢出归零导致高等级完全失效，
 *       这里改用 long 位移并封顶，保证高等级仍是满血级治疗。</li>
 * </ul>
 */
public class EffectFoodItem extends Item {

    /** 瞬间治疗效果的上限（值本身已是「满血级」，封顶仅为避免溢出产生无意义的天文数字）。 */
    private static final long MAX_INSTANT_HEAL = 1_000_000L;

    /**
     * 持续效果放大器的安全上限。原版 {@code MobEffectInstance#save} 以 byte 存储放大器
     * （{@code putByte("Amplifier", (byte)amplifier)}），读取时为有符号 byte，因此放大器 &gt; 127
     * 在存档重载后会变成负数，增益反倒变成减益（如力量变成减攻击）。故封顶 127。
     */
    private static final int MAX_AMPLIFIER = 127;

    /**
     * 一条食用效果。
     *
     * @param effect        效果来源（用 Supplier 延迟解析，避免注册早期就取注册表对象）
     * @param amplifier     放大器（原版规则：1 级 = 0）
     * @param durationTicks 持续时间（tick）；瞬间类效果忽略该值
     */
    public record AppliedEffect(Supplier<MobEffect> effect, int amplifier, int durationTicks) {
    }

    private final List<AppliedEffect> effects;

    public EffectFoodItem(Properties properties, List<AppliedEffect> effects) {
        super(properties);
        this.effects = List.copyOf(effects);
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        ItemStack result = super.finishUsingItem(stack, level, entity);
        if (!level.isClientSide) {
            applyEffects(entity);
        }
        return result;
    }

    private void applyEffects(LivingEntity entity) {
        for (AppliedEffect applied : effects) {
            MobEffect effect = applied.effect().get();
            if (effect == MobEffects.HEAL) {
                long amount = 4L << Math.min(applied.amplifier(), 30);
                entity.heal((float) Math.min(amount, MAX_INSTANT_HEAL));
            } else if (effect.isInstantenous()) {
                effect.applyInstantenousEffect(entity, entity, entity, applied.amplifier(), 1.0D);
            } else {
                entity.addEffect(new MobEffectInstance(effect, applied.durationTicks(),
                        Math.min(applied.amplifier(), MAX_AMPLIFIER), false, true, true));
            }
        }
    }
}
