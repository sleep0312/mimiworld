package com.mimiworld.mimiworld.item;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.type.capability.ICurioItem;

import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * 启航之书（Book of Departure）：神遗之书槽位饰品。
 *
 * <p>佩戴期间：</p>
 * <ul>
 *   <li>持续获得抗性提升 II、跳跃提升 I、生命恢复 I（每秒刷新，等效永久，脱下清除残留）；</li>
 *   <li>时运 +3、抢夺 +3（由 Curios 接入方块掉落与 {@code LootingLevelEvent}）；</li>
 *   <li>最大生命值 +25%、攻击力 +20%（Curios 属性修饰符自动增删）。</li>
 * </ul>
 */
public class BookOfDepartureItem extends Item implements ICurioItem {

    /**
     * 药水效果刷新时长（tick，12 秒）；逐 tick 刷新所致实际为持续生效。
     *
     * <p>必须大于 {@code ModArmorSetBonusEvents} 的 220 tick 清理阈值：该处理器会把
     * 剩余时长 ≤220 且非盔甲提供的跳跃提升/生命恢复当作残留效果移除，
     * 用 240 可避免启航之书的效果被它误删。</p>
     */
    private static final int EFFECT_DURATION = 240;

    /** 本饰品提供的全部药水效果类型，用于脱下后清理残留。 */
    private static final Set<MobEffect> GRANTED_EFFECTS = Set.of(
            MobEffects.DAMAGE_RESISTANCE,
            MobEffects.JUMP,
            MobEffects.REGENERATION);

    public BookOfDepartureItem(Properties properties) {
        super(properties);
    }

    @Override
    public void curioTick(SlotContext slotContext, ItemStack stack) {
        LivingEntity entity = slotContext.entity();
        if (entity == null || entity.level().isClientSide) {
            return;
        }
        entity.addEffect(new MobEffectInstance(
                MobEffects.DAMAGE_RESISTANCE, EFFECT_DURATION, 1, true, true, true));
        entity.addEffect(new MobEffectInstance(
                MobEffects.JUMP, EFFECT_DURATION, 0, true, true, true));
        entity.addEffect(new MobEffectInstance(
                MobEffects.REGENERATION, EFFECT_DURATION, 0, true, true, true));
    }

    @Override
    public void onUnequip(SlotContext slotContext, ItemStack newStack, ItemStack stack) {
        LivingEntity entity = slotContext.entity();
        if (entity == null || entity.level().isClientSide) {
            return;
        }
        // 仅清除本饰品灌注的短时残留，避免误删玩家自饮的长时药水
        for (MobEffect effect : GRANTED_EFFECTS) {
            MobEffectInstance instance = entity.getEffect(effect);
            if (instance != null && instance.getDuration() <= EFFECT_DURATION) {
                entity.removeEffect(effect);
            }
        }
    }

    @Override
    public Multimap<Attribute, AttributeModifier> getAttributeModifiers(
            SlotContext slotContext, UUID uuid, ItemStack stack) {
        Multimap<Attribute, AttributeModifier> modifiers = HashMultimap.create();
        modifiers.put(Attributes.MAX_HEALTH, new AttributeModifier(uuid,
                "mimiworld.book_of_departure.max_health", 0.25D,
                AttributeModifier.Operation.MULTIPLY_TOTAL));
        modifiers.put(Attributes.ATTACK_DAMAGE, new AttributeModifier(uuid,
                "mimiworld.book_of_departure.attack_damage", 0.20D,
                AttributeModifier.Operation.MULTIPLY_TOTAL));
        return modifiers;
    }

    @Override
    public int getFortuneLevel(SlotContext slotContext, LootContext lootContext, ItemStack stack) {
        return 3;
    }

    @Override
    public int getLootingLevel(SlotContext slotContext, DamageSource source,
                               LivingEntity target, int baseLooting, ItemStack stack) {
        return 3;
    }

    /**
     * 把药水与时运/抢夺说明并入 Curios 的「佩戴于神遗之书时：」属性区块，
     * 以蓝色文字显示，不再单独占用物品 lore 的白色说明。
     */
    @Override
    public List<Component> getAttributesTooltip(List<Component> tooltips, ItemStack stack) {
        tooltips.add(Component.translatable("tooltip.mimiworld.book_of_departure.effects")
                .withStyle(ChatFormatting.BLUE));
        tooltips.add(Component.translatable("tooltip.mimiworld.book_of_departure.fortune")
                .withStyle(ChatFormatting.BLUE));
        tooltips.add(Component.translatable("tooltip.mimiworld.book_of_departure.looting")
                .withStyle(ChatFormatting.BLUE));
        return tooltips;
    }
}
