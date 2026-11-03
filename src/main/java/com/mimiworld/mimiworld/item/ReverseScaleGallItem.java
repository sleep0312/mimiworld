package com.mimiworld.mimiworld.item;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.type.capability.ICurioItem;

import java.util.List;
import java.util.UUID;

/**
 * 逆鳞龙胆（Reverse Scale Gall）：Curios「圣遗物」槽位饰品。
 *
 * <p>佩戴期间：</p>
 * <ul>
 *   <li>持续获得「抗性提升 IV」；</li>
 *   <li>护甲值 +100%；</li>
 *   <li>单次受到的伤害不超过最大生命值的 20%（判定见 ModEffectEvents）。</li>
 * </ul>
 */
public class ReverseScaleGallItem extends Item implements ICurioItem {

    /** 抗性提升 IV 对应 amplifier 3；刷新时长 8 秒，剩余 ≤3 秒时重置（即每 5 秒一次）。 */
    private static final int RESISTANCE_AMPLIFIER = 3;
    private static final int EFFECT_DURATION_TICKS = 160;
    private static final int EFFECT_REFRESH_REMAINING = 60;

    public ReverseScaleGallItem(Properties properties) {
        super(properties);
    }

    @Override
    public Multimap<Attribute, AttributeModifier> getAttributeModifiers(
            SlotContext slotContext, UUID uuid, ItemStack stack) {
        Multimap<Attribute, AttributeModifier> modifiers = HashMultimap.create();
        modifiers.put(Attributes.ARMOR, new AttributeModifier(uuid,
                "mimiworld.reverse_scale_gall.armor", 1.0D,
                AttributeModifier.Operation.MULTIPLY_TOTAL));
        return modifiers;
    }

    @Override
    public void curioTick(SlotContext slotContext, ItemStack stack) {
        LivingEntity entity = slotContext.entity();
        if (entity == null || entity.level().isClientSide) {
            return;
        }
        // 非 ambient 施加，避免被盔甲效果的脱下清理误删；剩余 ≤3 秒时重置回 8 秒
        MobEffectInstance current = entity.getEffect(MobEffects.DAMAGE_RESISTANCE);
        if (current == null || current.getAmplifier() < RESISTANCE_AMPLIFIER
                || (current.getAmplifier() == RESISTANCE_AMPLIFIER
                    && current.getDuration() <= EFFECT_REFRESH_REMAINING)) {
            entity.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE,
                    EFFECT_DURATION_TICKS, RESISTANCE_AMPLIFIER, false, false, true));
        }
    }

    @Override
    public List<Component> getAttributesTooltip(List<Component> tooltips, ItemStack stack) {
        tooltips.clear();
        tooltips.add(Component.empty());
        tooltips.add(Component.translatable("curios.modifiers.sacred_relic")
                .withStyle(ChatFormatting.GOLD));
        tooltips.add(Component.translatable("tooltip.mimiworld.reverse_scale_gall.resistance")
                .withStyle(ChatFormatting.BLUE));
        tooltips.add(Component.translatable("tooltip.mimiworld.reverse_scale_gall.armor")
                .withStyle(ChatFormatting.BLUE));
        tooltips.add(Component.translatable("tooltip.mimiworld.reverse_scale_gall.damage_cap")
                .withStyle(ChatFormatting.BLUE));
        return tooltips;
    }
}
