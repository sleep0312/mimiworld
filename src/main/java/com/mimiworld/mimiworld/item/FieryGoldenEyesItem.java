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
 *   <li>持续获得夜视效果（每 tick 刷新 12 秒时长，等效永久，脱下后清除残留）；</li>
 *   <li>免疫失明与黑暗：佩戴时立即清除已有失明/黑暗，之后由
 *       {@code MobEffectEvent.Applicable} 拦截新的施加（见 ModEffectEvents）。</li>
 * </ul>
 */
public class FieryGoldenEyesItem extends Item implements ICurioItem {

    /** 夜视刷新时长（tick，12 秒）；逐 tick 刷新所致实际为持续生效。 */
    private static final int EFFECT_DURATION = 240;

    public FieryGoldenEyesItem(Properties properties) {
        super(properties);
    }

    @Override
    public void curioTick(SlotContext slotContext, ItemStack stack) {
        LivingEntity entity = slotContext.entity();
        if (entity == null || entity.level().isClientSide) {
            return;
        }
        // 环境光、无粒子、显示图标
        entity.addEffect(new MobEffectInstance(
                MobEffects.NIGHT_VISION, EFFECT_DURATION, 0, true, false, true));
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
