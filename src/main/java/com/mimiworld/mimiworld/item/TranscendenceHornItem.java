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
 * 超越之号角（Horn of Transcendence）：Curios「圣遗物」槽位饰品。
 *
 * <p>佩戴期间：护甲值 +10、最大生命值 +10、攻击速度 +30%。</p>
 */
public class TranscendenceHornItem extends Item implements ICurioItem {

    public TranscendenceHornItem(Properties properties) {
        super(properties);
    }

    @Override
    public Multimap<Attribute, AttributeModifier> getAttributeModifiers(
            SlotContext slotContext, UUID uuid, ItemStack stack) {
        Multimap<Attribute, AttributeModifier> modifiers = HashMultimap.create();
        modifiers.put(Attributes.ARMOR, new AttributeModifier(uuid,
                "mimiworld.transcendence_horn.armor", 10.0D,
                AttributeModifier.Operation.ADDITION));
        modifiers.put(Attributes.MAX_HEALTH, new AttributeModifier(uuid,
                "mimiworld.transcendence_horn.max_health", 10.0D,
                AttributeModifier.Operation.ADDITION));
        modifiers.put(Attributes.ATTACK_SPEED, new AttributeModifier(uuid,
                "mimiworld.transcendence_horn.attack_speed", 0.30D,
                AttributeModifier.Operation.MULTIPLY_TOTAL));
        return modifiers;
    }

    @Override
    public List<Component> getAttributesTooltip(List<Component> tooltips, ItemStack stack) {
        tooltips.clear();
        tooltips.add(Component.empty());
        tooltips.add(Component.translatable("curios.modifiers.sacred_relic")
                .withStyle(ChatFormatting.GOLD));
        tooltips.add(Component.translatable("tooltip.mimiworld.transcendence_horn.armor")
                .withStyle(ChatFormatting.BLUE));
        tooltips.add(Component.translatable("tooltip.mimiworld.transcendence_horn.max_health")
                .withStyle(ChatFormatting.BLUE));
        tooltips.add(Component.translatable("tooltip.mimiworld.transcendence_horn.attack_speed")
                .withStyle(ChatFormatting.BLUE));
        return tooltips;
    }
}
