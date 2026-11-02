package com.mimiworld.mimiworld.item;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.type.capability.ICurioItem;

import java.util.List;

/**
 * 上古毛皮（Ancient Pelt）：Curios「圣遗物」槽位饰品。
 *
 * <p>佩戴期间：</p>
 * <ul>
 *   <li>抢夺 +2；</li>
 *   <li>持续获得「易怒」效果，初始 I 级；</li>
 *   <li>每次易怒的 25% 增伤触发时等级 +1（最高 V 级）；10 秒未触发则降回 I 级。</li>
 * </ul>
 *
 * <p>易怒的维持与升降级逻辑由 {@link com.mimiworld.mimiworld.event.ModEffectEvents} 统一处理。</p>
 */
public class AncientPeltItem extends Item implements ICurioItem {

    public AncientPeltItem(Properties properties) {
        super(properties);
    }

    @Override
    public int getLootingLevel(SlotContext slotContext, DamageSource source,
                               LivingEntity target, int baseLooting, ItemStack stack) {
        return 2;
    }

    @Override
    public List<Component> getAttributesTooltip(List<Component> tooltips, ItemStack stack) {
        tooltips.clear();
        tooltips.add(Component.empty());
        tooltips.add(Component.translatable("curios.modifiers.sacred_relic")
                .withStyle(ChatFormatting.GOLD));
        tooltips.add(Component.translatable("tooltip.mimiworld.ancient_pelt.looting")
                .withStyle(ChatFormatting.BLUE));
        tooltips.add(Component.translatable("tooltip.mimiworld.ancient_pelt.irritable")
                .withStyle(ChatFormatting.BLUE));
        tooltips.add(Component.translatable("tooltip.mimiworld.ancient_pelt.escalate")
                .withStyle(ChatFormatting.BLUE));
        tooltips.add(Component.translatable("tooltip.mimiworld.ancient_pelt.decay")
                .withStyle(ChatFormatting.BLUE));
        return tooltips;
    }
}
