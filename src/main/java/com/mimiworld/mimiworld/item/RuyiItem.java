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
import net.minecraftforge.common.ForgeMod;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.type.capability.ICurioItem;

import java.util.List;
import java.util.UUID;

/**
 * 如意（Ruyi）：圣遗物槽位饰品。
 *
 * <p>佩戴期间由 Curios 属性系统提供：攻击距离（{@code forge:entity_reach}）+5、
 * 攻击伤害（{@code minecraft:generic.attack_damage}）+5；脱下后属性自动移除。</p>
 *
 * <p>Curios 会自动把属性修饰符翻译成蓝色“+5 实体范围”等文本，为避免与
 * “攻击距离 +5”表述不一致，这里重写属性提示区块，统一用自定义蓝字展示。</p>
 */
public class RuyiItem extends Item implements ICurioItem {

    private static final double ATTACK_REACH_BONUS = 5.0D;
    private static final double ATTACK_DAMAGE_BONUS = 5.0D;

    public RuyiItem(Properties properties) {
        super(properties);
    }

    @Override
    public Multimap<Attribute, AttributeModifier> getAttributeModifiers(
            SlotContext slotContext, UUID uuid, ItemStack stack) {
        Multimap<Attribute, AttributeModifier> modifiers = HashMultimap.create();
        modifiers.put(ForgeMod.ENTITY_REACH.get(), new AttributeModifier(uuid,
                "mimiworld.ruyi.attack_reach", ATTACK_REACH_BONUS,
                AttributeModifier.Operation.ADDITION));
        modifiers.put(Attributes.ATTACK_DAMAGE, new AttributeModifier(uuid,
                "mimiworld.ruyi.attack_damage", ATTACK_DAMAGE_BONUS,
                AttributeModifier.Operation.ADDITION));
        return modifiers;
    }

    /**
     * 用自定义蓝字替换 Curios 自动生成的属性行，统一显示「攻击距离 +5 / 攻击伤害 +5」。
     */
    @Override
    public List<Component> getAttributesTooltip(List<Component> tooltips, ItemStack stack) {
        tooltips.clear();
        tooltips.add(Component.empty());
        tooltips.add(Component.translatable("curios.modifiers.sacred_relic")
                .withStyle(ChatFormatting.GOLD));
        tooltips.add(Component.translatable("tooltip.mimiworld.ruyi.attack_reach")
                .withStyle(ChatFormatting.BLUE));
        tooltips.add(Component.translatable("tooltip.mimiworld.ruyi.attack_damage")
                .withStyle(ChatFormatting.BLUE));
        return tooltips;
    }
}
