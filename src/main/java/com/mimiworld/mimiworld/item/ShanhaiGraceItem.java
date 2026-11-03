package com.mimiworld.mimiworld.item;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import top.theillusivec4.curios.api.type.capability.ICurioItem;

import java.util.List;

/**
 * 山海恩泽（Shanhai Grace）：Curios「圣遗物」槽位饰品。
 *
 * <p>佩戴期间持续获得「跳跃提升 V」（由 {@link com.mimiworld.mimiworld.event.ModEffectEvents}
 * 的 PlayerTickEvent 统一施加）；玩家的近战伤害随下落高度增加，每下落 1 格伤害 +1%。</p>
 */
public class ShanhaiGraceItem extends Item implements ICurioItem {

    public ShanhaiGraceItem(Properties properties) {
        super(properties);
    }

    @Override
    public List<Component> getAttributesTooltip(List<Component> tooltips, ItemStack stack) {
        tooltips.clear();
        tooltips.add(Component.empty());
        tooltips.add(Component.translatable("curios.modifiers.sacred_relic")
                .withStyle(ChatFormatting.GOLD));
        tooltips.add(Component.translatable("tooltip.mimiworld.shanhai_grace.jump")
                .withStyle(ChatFormatting.BLUE));
        tooltips.add(Component.translatable("tooltip.mimiworld.shanhai_grace.fall")
                .withStyle(ChatFormatting.BLUE));
        return tooltips;
    }
}
