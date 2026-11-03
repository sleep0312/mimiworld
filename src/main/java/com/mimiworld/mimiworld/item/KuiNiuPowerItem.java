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
 * 夔牛之力（Kui Niu Power）：Curios「圣遗物」槽位饰品。
 *
 * <p>佩戴期间：攻击无视无敌帧（判定见 {@link com.mimiworld.mimiworld.event.ModEffectEvents}），
 * 攻击速度 +1、攻击力 +20%、护甲值 +50%。</p>
 */
public class KuiNiuPowerItem extends Item implements ICurioItem {

    public KuiNiuPowerItem(Properties properties) {
        super(properties);
    }

    @Override
    public Multimap<Attribute, AttributeModifier> getAttributeModifiers(
            SlotContext slotContext, UUID uuid, ItemStack stack) {
        Multimap<Attribute, AttributeModifier> modifiers = HashMultimap.create();
        modifiers.put(Attributes.ATTACK_SPEED, new AttributeModifier(uuid,
                "mimiworld.kui_niu_power.attack_speed", 1.0D,
                AttributeModifier.Operation.ADDITION));
        modifiers.put(Attributes.ATTACK_DAMAGE, new AttributeModifier(uuid,
                "mimiworld.kui_niu_power.attack_damage", 0.2D,
                AttributeModifier.Operation.MULTIPLY_TOTAL));
        modifiers.put(Attributes.ARMOR, new AttributeModifier(uuid,
                "mimiworld.kui_niu_power.armor", 0.5D,
                AttributeModifier.Operation.MULTIPLY_TOTAL));
        return modifiers;
    }

    @Override
    public List<Component> getAttributesTooltip(List<Component> tooltips, ItemStack stack) {
        tooltips.clear();
        tooltips.add(Component.empty());
        tooltips.add(Component.translatable("curios.modifiers.sacred_relic")
                .withStyle(ChatFormatting.GOLD));
        tooltips.add(Component.translatable("tooltip.mimiworld.kui_niu_power.no_invulnerability")
                .withStyle(ChatFormatting.BLUE));
        tooltips.add(Component.translatable("tooltip.mimiworld.kui_niu_power.attack_speed")
                .withStyle(ChatFormatting.BLUE));
        tooltips.add(Component.translatable("tooltip.mimiworld.kui_niu_power.attack_damage")
                .withStyle(ChatFormatting.BLUE));
        tooltips.add(Component.translatable("tooltip.mimiworld.kui_niu_power.armor")
                .withStyle(ChatFormatting.BLUE));
        return tooltips;
    }
}
