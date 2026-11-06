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
 * 米米圣盾（Mimi Shield）：Curios「圣遗物」槽位饰品。
 *
 * <p>佩戴期间最大生命值 +5；受到任意伤害后获得 10% 最大生命值的额外生命值（HUD 显示为黄心），
 * 额外生命值总额最多累计到最大生命值的 40%（判定见 {@link com.mimiworld.mimiworld.event.ModEffectEvents}）。</p>
 */
public class MimiShieldItem extends Item implements ICurioItem {

    private static final double HEALTH_BONUS = 5.0D;

    public MimiShieldItem(Properties properties) {
        super(properties);
    }

    @Override
    public Multimap<Attribute, AttributeModifier> getAttributeModifiers(
            SlotContext slotContext, UUID uuid, ItemStack stack) {
        Multimap<Attribute, AttributeModifier> modifiers = HashMultimap.create();
        modifiers.put(Attributes.MAX_HEALTH, new AttributeModifier(uuid,
                "mimiworld.mimi_shield.max_health", HEALTH_BONUS,
                AttributeModifier.Operation.ADDITION));
        return modifiers;
    }

    @Override
    public List<Component> getAttributesTooltip(List<Component> tooltips, ItemStack stack) {
        tooltips.clear();
        tooltips.add(Component.empty());
        tooltips.add(Component.translatable("curios.modifiers.sacred_relic")
                .withStyle(ChatFormatting.GOLD));
        tooltips.add(Component.translatable("tooltip.mimiworld.mimi_shield.health")
                .withStyle(ChatFormatting.BLUE));
        tooltips.add(Component.translatable("tooltip.mimiworld.mimi_shield.heal")
                .withStyle(ChatFormatting.BLUE));
        tooltips.add(Component.translatable("tooltip.mimiworld.mimi_shield.threshold")
                .withStyle(ChatFormatting.BLUE));
        return tooltips;
    }
}
