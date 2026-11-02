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
 * 暗灵毒牙（Dark Spirit Fang）：Curios「圣遗物」槽位饰品。
 *
 * <p>佩戴期间：攻击力 +25%；玩家攻击命中时，使目标获得 3 秒「暗毒」效果
 * （施加逻辑见 {@link com.mimiworld.mimiworld.event.ModEffectEvents}）。</p>
 */
public class DarkSpiritFangItem extends Item implements ICurioItem {

    public DarkSpiritFangItem(Properties properties) {
        super(properties);
    }

    @Override
    public Multimap<Attribute, AttributeModifier> getAttributeModifiers(
            SlotContext slotContext, UUID uuid, ItemStack stack) {
        Multimap<Attribute, AttributeModifier> modifiers = HashMultimap.create();
        modifiers.put(Attributes.ATTACK_DAMAGE, new AttributeModifier(uuid,
                "mimiworld.dark_spirit_fang.attack_damage", 0.25D,
                AttributeModifier.Operation.MULTIPLY_TOTAL));
        return modifiers;
    }

    @Override
    public List<Component> getAttributesTooltip(List<Component> tooltips, ItemStack stack) {
        tooltips.clear();
        tooltips.add(Component.empty());
        tooltips.add(Component.translatable("curios.modifiers.sacred_relic")
                .withStyle(ChatFormatting.GOLD));
        tooltips.add(Component.translatable("tooltip.mimiworld.dark_spirit_fang.attack")
                .withStyle(ChatFormatting.BLUE));
        tooltips.add(Component.translatable("tooltip.mimiworld.dark_spirit_fang.poison")
                .withStyle(ChatFormatting.BLUE));
        return tooltips;
    }
}
