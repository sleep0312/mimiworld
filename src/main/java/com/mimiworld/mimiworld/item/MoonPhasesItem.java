package com.mimiworld.mimiworld.item;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
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
 * 月有圆缺（Moon Phases）：Curios「圣遗物」槽位饰品。
 *
 * <p>佩戴期间：</p>
 * <ul>
 *   <li>暴击率 +30%（非暴击的近战攻击有 30% 概率转为暴击，见 ModEffectEvents）；</li>
 *   <li>攻击力 +10（本类提供的属性修饰符）；</li>
 *   <li>受到的总伤害 +25%；</li>
 *   <li>5 格内的怪物有 20% 概率额外造成 50% 伤害（后两项见 ModEffectEvents）。</li>
 * </ul>
 */
public class MoonPhasesItem extends Item implements ICurioItem {

    public MoonPhasesItem(Properties properties) {
        super(properties);
    }

    @Override
    public Multimap<Attribute, AttributeModifier> getAttributeModifiers(
            SlotContext slotContext, UUID uuid, ItemStack stack) {
        Multimap<Attribute, AttributeModifier> modifiers = HashMultimap.create();
        modifiers.put(Attributes.ATTACK_DAMAGE, new AttributeModifier(uuid,
                "mimiworld.moon_phases.attack_damage", 10.0D,
                AttributeModifier.Operation.ADDITION));
        return modifiers;
    }

    @Override
    public List<Component> getAttributesTooltip(List<Component> tooltips, ItemStack stack) {
        tooltips.clear();
        tooltips.add(Component.empty());
        tooltips.add(Component.translatable("curios.modifiers.sacred_relic")
                .withStyle(ChatFormatting.GOLD));
        tooltips.add(Component.translatable("tooltip.mimiworld.moon_phases.crit")
                .withStyle(ChatFormatting.BLUE));
        tooltips.add(Component.translatable("tooltip.mimiworld.moon_phases.attack")
                .withStyle(ChatFormatting.BLUE));
        tooltips.add(Component.translatable("tooltip.mimiworld.moon_phases.vulnerability")
                .withStyle(ChatFormatting.BLUE));
        tooltips.add(Component.translatable("tooltip.mimiworld.moon_phases.monster")
                .withStyle(ChatFormatting.BLUE));
        return tooltips;
    }
}
