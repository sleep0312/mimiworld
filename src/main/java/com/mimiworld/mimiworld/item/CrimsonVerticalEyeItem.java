package com.mimiworld.mimiworld.item;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import top.theillusivec4.curios.api.type.capability.ICurioItem;

import java.util.List;

/**
 * 猩红竖眼（Crimson Vertical Eye）：Curios「圣遗物」槽位饰品。
 *
 * <p>佩戴期间：</p>
 * <ul>
 *   <li>以玩家为中心 21×21×21 范围内的怪物获得发光效果（见 CrimsonEyeEvents）；</li>
 *   <li>按「自定义」按键可传送到最近的发光怪物身后并面向它，路径被方块遮挡时不生效。</li>
 * </ul>
 */
public class CrimsonVerticalEyeItem extends Item implements ICurioItem {

    public CrimsonVerticalEyeItem(Properties properties) {
        super(properties);
    }

    @Override
    public List<Component> getAttributesTooltip(List<Component> tooltips, ItemStack stack) {
        tooltips.clear();
        tooltips.add(Component.empty());
        tooltips.add(Component.translatable("curios.modifiers.sacred_relic")
                .withStyle(ChatFormatting.GOLD));
        tooltips.add(Component.translatable("tooltip.mimiworld.crimson_vertical_eye.glow")
                .withStyle(ChatFormatting.BLUE));
        tooltips.add(Component.translatable("tooltip.mimiworld.crimson_vertical_eye.teleport")
                .withStyle(ChatFormatting.BLUE));
        tooltips.add(Component.translatable("tooltip.mimiworld.crimson_vertical_eye.wall")
                .withStyle(ChatFormatting.BLUE));
        return tooltips;
    }
}
