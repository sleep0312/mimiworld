package com.mimiworld.mimiworld.event;

import com.mimiworld.mimiworld.MimiWorld;
import com.mimiworld.mimiworld.enchantment.ModEnchantments;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * 自定义附魔的运行时事件接线（Forge EVENT_BUS）。
 *
 * <p>泣血（weeping_blood）：在 {@link LivingDamageEvent} 中读取攻击方主手/副手武器上的附魔等级，
 * 按本次实际结算伤害的百分比回复攻击方生命——
 * 1 级 6%，每提升 1 级额外 +2%（I 级 6%、II 级 8%、III 级 10%、IV 级 12%、V 级 14%）。</p>
 */
@Mod.EventBusSubscriber(modid = MimiWorld.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ModEnchantmentEvents {

    @SubscribeEvent
    public static void onLivingDamage(LivingDamageEvent event) {
        // 只在服务端处理（事件在客户端也可能收到，但治疗只应服务端生效）
        LivingEntity target = event.getEntity();
        if (target.level().isClientSide) {
            return;
        }

        LivingEntity attacker = resolveAttacker(event.getSource());
        if (attacker == null || event.getAmount() <= 0.0F) {
            return;
        }

        int level = getWeepingBloodLevel(attacker);
        if (level <= 0) {
            return;
        }

        // 1 级 6%，之后每级 +2%
        float healAmount = event.getAmount() * (0.06F + 0.02F * (level - 1));
        attacker.heal(healAmount);
    }

    /** 追溯攻击方：直接实体 → 若是弹射物则取其发射者。 */
    private static LivingEntity resolveAttacker(DamageSource source) {
        Entity entity = source.getEntity();
        if (entity instanceof Projectile projectile) {
            entity = projectile.getOwner();
        }
        return entity instanceof LivingEntity living ? living : null;
    }

    /** 读取攻击方手持武器上的泣血等级（优先主手，主手没有时再看副手）。 */
    private static int getWeepingBloodLevel(LivingEntity attacker) {
        int mainHand = EnchantmentHelper.getItemEnchantmentLevel(
                ModEnchantments.WEEPING_BLOOD.get(), attacker.getMainHandItem());
        if (mainHand > 0) {
            return mainHand;
        }
        return EnchantmentHelper.getItemEnchantmentLevel(
                ModEnchantments.WEEPING_BLOOD.get(), attacker.getOffhandItem());
    }
}
