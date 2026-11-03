package com.mimiworld.mimiworld.event;

import com.mimiworld.mimiworld.MimiWorld;
import com.mimiworld.mimiworld.ModItems;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import top.theillusivec4.curios.api.CuriosApi;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * 领域展开（Domain Expansion）：以佩戴者为中心 10×10×10 范围内的敌对生物护甲值 -50%。
 *
 * <p>实现方式：每个服务端 tick 刷新「佩戴该饰品的玩家」列表，敌对生物自身 tick 时做一次范围判定；
 * 进入范围时挂上减护甲的临时属性修饰符，离开范围（或玩家脱下饰品、离开世界）时移除，
 * 避免修饰符残留在怪身上。</p>
 */
@Mod.EventBusSubscriber(modid = MimiWorld.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class DomainExpansionEvents {

    /** 10×10×10 立方体即每轴 ±5 格。 */
    private static final double RANGE = 5.0D;

    private static final UUID ARMOR_MODIFIER_ID = UUID.fromString("6f3a2c1d-9b84-4c55-8d0e-7a1b2c3d4e5f");
    private static final AttributeModifier ARMOR_CUT = new AttributeModifier(
            ARMOR_MODIFIER_ID, "mimiworld.domain_expansion.armor", -0.5D,
            AttributeModifier.Operation.MULTIPLY_TOTAL);

    /** 当前佩戴「领域展开」的玩家，逐 tick 重新计算。 */
    private static final List<Player> ACTIVE_PLAYERS = new ArrayList<>();

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }
        ACTIVE_PLAYERS.clear();
        for (ServerPlayer player : event.getServer().getPlayerList().getPlayers()) {
            if (isEquipped(player)) {
                ACTIVE_PLAYERS.add(player);
            }
        }
    }

    @SubscribeEvent
    public static void onLivingTick(LivingEvent.LivingTickEvent event) {
        LivingEntity entity = event.getEntity();
        if (entity.level().isClientSide) {
            return;
        }
        AttributeInstance armor = entity.getAttribute(Attributes.ARMOR);
        if (armor == null) {
            return;
        }
        boolean active = !ACTIVE_PLAYERS.isEmpty() && isHostile(entity) && isInsideDomain(entity);
        boolean applied = armor.getModifier(ARMOR_MODIFIER_ID) != null;
        if (active) {
            if (!applied) {
                armor.addTransientModifier(ARMOR_CUT);
            }
        } else if (applied) {
            armor.removeModifier(ARMOR_MODIFIER_ID);
        }
    }

    /** 敌对生物判定：与原版/模组 Monster 一致的 Enemy 接口，或实体类别登记为 MONSTER。 */
    private static boolean isHostile(LivingEntity entity) {
        return entity.isAlive()
                && (entity instanceof Enemy || entity.getType().getCategory() == MobCategory.MONSTER);
    }

    private static boolean isInsideDomain(LivingEntity entity) {
        for (Player player : ACTIVE_PLAYERS) {
            if (player.level() == entity.level()
                    && Math.abs(entity.getX() - player.getX()) <= RANGE
                    && Math.abs(entity.getY() - player.getY()) <= RANGE
                    && Math.abs(entity.getZ() - player.getZ()) <= RANGE) {
                return true;
            }
        }
        return false;
    }

    private static boolean isEquipped(Player player) {
        return CuriosApi.getCuriosInventory(player).resolve()
                .map(handler -> handler.isEquipped(ModItems.DOMAIN_EXPANSION.get()))
                .orElse(false);
    }
}
