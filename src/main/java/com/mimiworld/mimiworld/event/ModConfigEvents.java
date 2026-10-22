package com.mimiworld.mimiworld.event;

import com.mimiworld.mimiworld.MimiWorld;
import com.mimiworld.mimiworld.ModConfig;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * 数值配置的运行时接线（Forge EVENT_BUS）。
 *
 * <ul>
 *   <li>实体加入世界时，把 {@code [mobs.*]} 的数值写入其基础属性；</li>
 *   <li>{@code [combat]} 的攻防倍率在 LivingHurtEvent 中结算，只作用于本模组生物；</li>
 *   <li>{@code [drops]} 的掉落倍率在 LivingDropsEvent 中按物品堆叠数量结算。</li>
 * </ul>
 */
@Mod.EventBusSubscriber(modid = MimiWorld.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ModConfigEvents {

    @SubscribeEvent
    public static void onEntityJoinLevel(EntityJoinLevelEvent event) {
        if (event.getLevel().isClientSide()) {
            return;
        }
        if (event.getEntity() instanceof Mob mob) {
            ModConfig.applyMobStats(mob);
        }
    }

    @SubscribeEvent
    public static void onLivingHurt(LivingHurtEvent event) {
        LivingEntity target = event.getEntity();
        if (target.level().isClientSide) {
            return;
        }

        double multiplier = 1.0D;
        // 追溯攻击方：直接实体 -> 投掷物发射者
        Entity attacker = event.getSource().getEntity();
        if (attacker instanceof Projectile projectile) {
            attacker = projectile.getOwner();
        }
        if (attacker instanceof LivingEntity livingAttacker && isModMob(livingAttacker)) {
            multiplier *= ModConfig.mobDamageMultiplier();
        }
        if (isModMob(target)) {
            multiplier *= ModConfig.mobDamageTakenMultiplier();
        }

        if (multiplier != 1.0D) {
            event.setAmount((float) (event.getAmount() * multiplier));
        }
    }

    @SubscribeEvent
    public static void onLivingDrops(LivingDropsEvent event) {
        LivingEntity entity = event.getEntity();
        if (entity.level().isClientSide || !isModMob(entity)) {
            return;
        }
        double multiplier = ModConfig.lootMultiplier();
        if (multiplier == 1.0D) {
            return;
        }
        for (ItemEntity drop : event.getDrops()) {
            ItemStack stack = drop.getItem();
            int count = (int) Math.round(stack.getCount() * multiplier);
            if (count <= 0) {
                drop.discard();
            } else {
                stack.setCount(Math.min(count, stack.getMaxStackSize()));
            }
        }
    }

    private static boolean isModMob(Entity entity) {
        var key = BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType());
        return key != null && MimiWorld.MODID.equals(key.getNamespace());
    }
}
