package com.mimiworld.mimiworld.item;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.UUID;

/**
 * 五套 GeckoLib 定制盔甲（T1饭晶合金→T5超懒合金）的「单件穿戴效果」契约。
 *
 * <p>每套盔甲四件在穿戴时提供：</p>
 * <ul>
 *   <li>{@link #getMaxHealthBonus}：直接加最大生命值（通过盔甲 Item 的
 *       {@code getAttributeModifiers} 合成一个 MAX_HEALTH 属性加持，随穿/脱由原版自动增删）。</li>
 *   <li>{@link #getWornEffects}：穿戴期间持续生效的药水效果（由
 *       {@code ModArmorSetBonusEvents} 逐 tick 刷新，脱下后清理残留）。</li>
 *   <li>{@link #grantsFlight}：穿戴时是否给予创造飞行能力（mayfly，由
 *       {@code ModArmorSetBonusEvents} 逐 tick 维持/脱下撤销）。</li>
 * </ul>
 */
public interface ArmorPieceEffects {

    /** 指定槽位穿戴时提供的最大生命值加成（生命值，0 表示无加成）。 */
    float getMaxHealthBonus(EquipmentSlot slot);

    /** 指定槽位穿戴时的持续药水效果（效果 -> 放大器，1级=0；无效果返回空 Map）。 */
    Map<MobEffect, Integer> getWornEffects(EquipmentSlot slot);

    /** 指定槽位穿戴时是否给予创造飞行能力（mayfly）。 */
    default boolean grantsFlight(EquipmentSlot slot) {
        return false;
    }

    /**
     * 把原盔甲守卫/韧性等属性表与本件的最大生命值加成合并，供盔甲 Item 的
     * {@code getAttributeModifiers} 直接返回，实现 +生命值随穿/脱自动增删。
     *
     * <p>仅当查询槽位 {@code slot} 与本件盔甲自身的装备槽 {@code armorSlot} 一致时才追加生命值，
     * 避免手持/放错槽位时也显示或生效生命加成（原版盔甲属性本就只在对应装备槽生效）。</p>
     *
     * @param delegate  原 {@code super.getAttributeModifiers(slot, stack)} 的属性表
     * @param slot      当前被查询的装备槽
     * @param armorSlot 本件盔甲自身的装备槽（即 {@code ArmorItem#getSlot()}）
     * @param itemName  用于生成稳定 UUID 的唯一标识（如 "rice_crystal_alloy"）
     */
    default ImmutableMultimap<Attribute, AttributeModifier> withMaxHealthBonus(
            Multimap<Attribute, AttributeModifier> delegate, EquipmentSlot slot,
            EquipmentSlot armorSlot, String itemName) {
        if (slot != armorSlot) {
            return ImmutableMultimap.copyOf(delegate);
        }
        float hp = getMaxHealthBonus(slot);
        if (hp <= 0.0F) {
            return ImmutableMultimap.copyOf(delegate);
        }
        ImmutableMultimap.Builder<Attribute, AttributeModifier> builder = ImmutableMultimap.builder();
        builder.putAll(delegate);
        builder.put(Attributes.MAX_HEALTH, new AttributeModifier(
                UUID.nameUUIDFromBytes((itemName + ".max_health." + slot.name())
                        .getBytes(StandardCharsets.UTF_8)),
                itemName + ".max_health",
                hp,
                AttributeModifier.Operation.ADDITION));
        return builder.build();
    }
}