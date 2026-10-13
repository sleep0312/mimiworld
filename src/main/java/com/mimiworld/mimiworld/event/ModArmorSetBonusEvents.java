package com.mimiworld.mimiworld.event;

import com.mimiworld.mimiworld.MimiWorld;
import com.mimiworld.mimiworld.item.ArmorPieceEffects;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * 五套 GeckoLib 定制盔甲（T1饭晶合金→T5超懒合金）的「穿戴持续药水效果」处理器。
 *
 * <p>寿命：#服务端 {@code PlayerTickEvent}，逐 tick 检查玩家穿戴的盔甲单件效果：
 * <ul>
 *   <li><b>持续生效</b>：把仍在穿戴的效果刷新到固定时长（等效永久，放大器低时不覆盖玩家自带的高等级药水）。</li>
 *   <li><b>脱下清理</b>：对不再由某件盔甲提供、且剩余时长不超过刷新阈值（说明是残留的灌注效果
 *       而非玩家自行饮用的长时药水）的效果调用 {@code removeEffect} 移除以满足「脱下移除」。</li>
 * </ul>
 * +最大生命值不在此处理，由各 Item 的 {@code getAttributeModifiers} 合成 MAX_HEALTH 属性直接增删。
 */
@Mod.EventBusSubscriber(modid = MimiWorld.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ModArmorSetBonusEvents {

    private static final EquipmentSlot[] ARMOR_SLOTS =
            {EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET};

    /** 穿戴期间药水效果的刷新时长（tick，11 秒）。逐 tick 刷新所致实际为持续生效。 */
    private static final int EFFECT_DURATION = 220;

    /** 五套盔甲可能提供的全部药水效果类型，用于脱下后清理残留效果。 */
    private static final Set<MobEffect> ARMOR_EFFECT_TYPES = Set.of(
            MobEffects.REGENERATION,
            MobEffects.FIRE_RESISTANCE,
            MobEffects.MOVEMENT_SPEED,
            MobEffects.NIGHT_VISION,
            MobEffects.DAMAGE_BOOST,
            MobEffects.WATER_BREATHING,
            MobEffects.DIG_SPEED,
            MobEffects.JUMP);

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }
        Player player = event.player;
        if (player.level().isClientSide) {
            return;
        }

        // 统计当前穿戴的盔甲单件效果（同类型效果取最高等级）
        Map<MobEffect, Integer> desired = new HashMap<>();
        for (EquipmentSlot slot : ARMOR_SLOTS) {
            ItemStack stack = player.getItemBySlot(slot);
            Item item = stack.getItem();
            if (item instanceof ArmorPieceEffects piece) {
                Map<MobEffect, Integer> effects = piece.getWornEffects(slot);
                if (effects != null) {
                    effects.forEach((effect, amp) -> desired.merge(effect, amp, Math::max));
                }
            }
        }

        // 持续刷新穿戴中的效果（放大器不低于当前时刷新时长；比已生效低的不会覆盖玩家自带药水）
        desired.forEach((effect, amp) ->
                player.addEffect(new MobEffectInstance(effect, EFFECT_DURATION, amp, true, true, true)));

        // 脱下后移除残留的盔甲来源效果
        for (MobEffect effect : ARMOR_EFFECT_TYPES) {
            if (desired.containsKey(effect)) {
                continue;
            }
            MobEffectInstance instance = player.getEffect(effect);
            if (instance != null && instance.getDuration() <= EFFECT_DURATION) {
                player.removeEffect(effect);
            }
        }
    }
}