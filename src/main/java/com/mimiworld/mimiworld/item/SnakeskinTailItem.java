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
 * 蛇皮驺尾（Snakeskin Tail）：Curios「圣遗物」槽位饰品。
 *
 * <p>佩戴期间：移动速度 +0.1（属性原值，玩家基础为 0.1）、护甲值 +5。</p>
 */
public class SnakeskinTailItem extends Item implements ICurioItem {

    public SnakeskinTailItem(Properties properties) {
        super(properties);
    }

    @Override
    public Multimap<Attribute, AttributeModifier> getAttributeModifiers(
            SlotContext slotContext, UUID uuid, ItemStack stack) {
        Multimap<Attribute, AttributeModifier> modifiers = HashMultimap.create();
        modifiers.put(Attributes.MOVEMENT_SPEED, new AttributeModifier(uuid,
                "mimiworld.snakeskin_tail.movement_speed", 0.1D,
                AttributeModifier.Operation.ADDITION));
        modifiers.put(Attributes.ARMOR, new AttributeModifier(uuid,
                "mimiworld.snakeskin_tail.armor", 5.0D,
                AttributeModifier.Operation.ADDITION));
        return modifiers;
    }

    @Override
    public List<Component> getAttributesTooltip(List<Component> tooltips, ItemStack stack) {
        tooltips.clear();
        tooltips.add(Component.empty());
        tooltips.add(Component.translatable("curios.modifiers.sacred_relic")
                .withStyle(ChatFormatting.GOLD));
        tooltips.add(Component.translatable("tooltip.mimiworld.snakeskin_tail.movement_speed")
                .withStyle(ChatFormatting.BLUE));
        tooltips.add(Component.translatable("tooltip.mimiworld.snakeskin_tail.armor")
                .withStyle(ChatFormatting.BLUE));
        return tooltips;
    }
}
