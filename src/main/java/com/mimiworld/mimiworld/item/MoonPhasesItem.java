package com.mimiworld.mimiworld.item;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.type.capability.ICurioItem;

import java.util.List;
import java.util.UUID;

/**
 * 月有圆缺（Moon Phases）：Curios「圣遗物」槽位饰品。
 *
 * <p>佩戴期间：</p>
 * <ul>
 *   <li>暴击率 +30%：若安装 AttributesLib 则加在 {@code attributeslib:crit_chance} 属性上
 *       （属性面板会显示 5% → 35%）；未安装时由 ModEffectEvents 以事件概率转为暴击；</li>
 *   <li>攻击力 +10（本类提供的属性修饰符）；</li>
 *   <li>受到的总伤害 +25%；</li>
 *   <li>5 格内的怪物有 20% 概率额外造成 50% 伤害（后两项见 ModEffectEvents）。</li>
 * </ul>
 */
public class MoonPhasesItem extends Item implements ICurioItem {

    /** AttributesLib 提供的暴击率属性（未安装该模组时注册表中不存在）。 */
    private static final ResourceLocation CRIT_CHANCE_ID =
            new ResourceLocation("attributeslib", "crit_chance");

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
        // 暴击率属性：基础 5%，+30% 后为 35%（与 AttributesLib 的暴击判定共用同一属性）
        Attribute critChance = ForgeRegistries.ATTRIBUTES.getValue(CRIT_CHANCE_ID);
        if (critChance != null) {
            modifiers.put(critChance, new AttributeModifier(uuid,
                    "mimiworld.moon_phases.crit_chance", 0.30D,
                    AttributeModifier.Operation.ADDITION));
        }
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
