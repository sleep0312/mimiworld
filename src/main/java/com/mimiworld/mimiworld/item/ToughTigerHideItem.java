package com.mimiworld.mimiworld.item;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import top.theillusivec4.curios.api.type.capability.ICurioItem;

import java.util.List;

/**
 * 坚韧虎皮（Tough Tiger Hide）：圣遗物槽位饰品。
 *
 * <p>佩戴期间：护甲值 +12；随后按总护甲值 ×10% 提升攻击力，再扣除等量护甲值；
 * 护甲值发生任何变化时自动重新计算（逻辑见 ModEffectEvents）。</p>
 */
public class ToughTigerHideItem extends Item implements ICurioItem {

    public ToughTigerHideItem(Properties properties) {
        super(properties);
    }

    /**
     * 用蓝色说明行重建「佩戴于圣遗物时：」区块（属性由事件动态维护）。
     */
    @Override
    public List<Component> getAttributesTooltip(List<Component> tooltips, ItemStack stack) {
        tooltips.clear();
        tooltips.add(Component.empty());
        tooltips.add(Component.translatable("curios.modifiers.sacred_relic")
                .withStyle(ChatFormatting.GOLD));
        tooltips.add(Component.translatable("tooltip.mimiworld.tough_tiger_hide.armor")
                .withStyle(ChatFormatting.BLUE));
        tooltips.add(Component.translatable("tooltip.mimiworld.tough_tiger_hide.attack")
                .withStyle(ChatFormatting.BLUE));
        tooltips.add(Component.translatable("tooltip.mimiworld.tough_tiger_hide.penalty")
                .withStyle(ChatFormatting.BLUE));
        return tooltips;
    }
}
