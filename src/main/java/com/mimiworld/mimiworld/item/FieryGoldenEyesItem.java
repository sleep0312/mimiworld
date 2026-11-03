package com.mimiworld.mimiworld.item;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.type.capability.ICurioItem;

import java.util.List;

/**
 * 火眼金睛（Fiery Golden Eyes）：圣遗物槽位饰品。
 *
 * <p>佩戴期间：</p>
 * <ul>
 *   <li>持续获得夜视效果（8 秒时长，每 5 秒刷新一次，脱下后清除残留）；</li>
 *   <li>免疫失明与黑暗：佩戴时立即清除已有失明/黑暗，之后由
 *       {@code MobEffectEvent.Applicable} 拦截新的施加（见 ModEffectEvents）。</li>
 * </ul>
 */
public class FieryGoldenEyesItem extends Item implements ICurioItem {

    /** 夜视刷新时长（tick，8 秒）与刷新阈值（剩余 ≤3 秒时重置，即每 5 秒一次）。 */
    private static final int EFFECT_DURATION = 160;
    private static final int EFFECT_REFRESH_REMAINING = 60;

    public FieryGoldenEyesItem(Properties properties) {
        super(properties);
    }

    @Override
    public void curioTick(SlotContext slotContext, ItemStack stack) {
        LivingEntity entity = slotContext.entity();
        if (entity == null || entity.level().isClientSide) {
            return;
        }
        // 环境光、无粒子、显示图标；非 ambient 避免被盔甲效果的脱下清理误删
        MobEffectInstance current = entity.getEffect(MobEffects.NIGHT_VISION);
        if (current == null
                || (current.getAmplifier() == 0 && current.getDuration() <= EFFECT_REFRESH_REMAINING)) {
            entity.addEffect(new MobEffectInstance(
                    MobEffects.NIGHT_VISION, EFFECT_DURATION, 0, false, false, true));
        }
    }

    @Override
    public void onEquip(SlotContext slotContext, ItemStack prevStack, ItemStack stack) {
        LivingEntity entity = slotContext.entity();
        if (entity == null || entity.level().isClientSide) {
            return;
        }
        // 佩戴瞬间清除身上已有的失明/黑暗
        entity.removeEffect(MobEffects.BLINDNESS);
        entity.removeEffect(MobEffects.DARKNESS);
    }

    @Override
    public void onUnequip(SlotContext slotContext, ItemStack newStack, ItemStack stack) {
        LivingEntity entity = slotContext.entity();
        if (entity == null || entity.level().isClientSide) {
            return;
        }
        // 仅清除本饰品灌注的短时残留，避免误删玩家自饮的长时夜视药水
        MobEffectInstance nightVision = entity.getEffect(MobEffects.NIGHT_VISION);
        if (nightVision != null && nightVision.getDuration() <= EFFECT_DURATION) {
            entity.removeEffect(MobEffects.NIGHT_VISION);
        }
    }

    /**
     * 把效果说明并入 Curios 的「佩戴于圣遗物时：」属性区块，以蓝色文字显示。
     */
    @Override
    public List<Component> getAttributesTooltip(List<Component> tooltips, ItemStack stack) {
        tooltips.add(Component.translatable("tooltip.mimiworld.fiery_golden_eyes.night_vision")
                .withStyle(ChatFormatting.BLUE));
        tooltips.add(Component.translatable("tooltip.mimiworld.fiery_golden_eyes.immunity")
                .withStyle(ChatFormatting.BLUE));
        return tooltips;
    }
}
