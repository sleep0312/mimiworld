package com.mimiworld.mimiworld.event;

import com.mimiworld.mimiworld.MimiWorld;
import com.mimiworld.mimiworld.effect.ModEffects;
import com.mimiworld.mimiworld.entity.CompoundAzurePythonEntity;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * 药水效果相关事件处理器（Forge EVENT_BUS）。
 *
 * <p>目前处理：</p>
 * <ul>
 *   <li>{@code crimson_candlelight}（腥红烛照）：LivingHurtEvent 中检查伤害源的攻击方是否拥有该效果，
 *       若有则将最终伤害乘以 (amplifier + 2)。覆盖近战、远程（弹射物追溯发射者）等攻击类伤害。</li>
 *   <li>{@code python_constriction}（蟒蛇缠绕）：拥有该效果的攻击方对复晶天青蟒造成的伤害 ×1.5。</li>
 * </ul>
 */
@Mod.EventBusSubscriber(modid = MimiWorld.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ModEffectEvents {

    /**
     * 腥红烛照伤害倍率处理。
     *
     * <p>Forge LivingHurtEvent 在原版 {@code LivingEntity#actuallyHurt} 里被触发，
     * 此时 {@code event.getAmount()} 是经过盔甲、抗性等修正后的最终伤害值。
     * 我们在这之上乘以倍率。</p>
     *
     * <p>攻击方追溯链：{@code source.getEntity()} 先拿到直接造成伤害的实体
     * （例如箭矢），如果是 {@link AbstractProjectile} 子类则进一步取 {@code getOwner()}
     * 追溯回发射者。</p>
     */
    @SubscribeEvent
    public static void onLivingHurt(LivingHurtEvent event) {
        // 只在服务端处理（LivingHurtEvent 可能在客户端也收到，但修改伤害只服务端有效）
        LivingEntity target = event.getEntity();
        if (target.level().isClientSide) {
            return;
        }

        DamageSource source = event.getSource();

        // 追溯攻击方：直接实体 → 若是投掷物则取其发射者
        Entity directEntity = source.getEntity();
        Entity attacker = directEntity;
        if (directEntity instanceof Projectile projectile) {
            attacker = projectile.getOwner();
        }

        if (!(attacker instanceof LivingEntity livingAttacker)) {
            return;
        }

        var effectInstance = livingAttacker.getEffect(ModEffects.CRIMSON_CANDLELIGHT.get());
        if (effectInstance == null) {
            return;
        }

        int amplifier = effectInstance.getAmplifier();
        float multiplier = amplifier + 2.0F; // amplifier=0 → ×2, amplifier=1 → ×3, ...
        float originalDamage = event.getAmount();
        float newDamage = originalDamage * multiplier;
        event.setAmount(newDamage);
    }

    /**
     * 蟒蛇缠绕易伤处理：攻击方带有 {@code python_constriction} 且受击者是复晶天青蟒时，
     * 最终伤害 ×1.5。与腥红烛照倍率独立（极端情况下两者相乘）。
     */
    @SubscribeEvent
    public static void onPythonConstriction(LivingHurtEvent event) {
        LivingEntity target = event.getEntity();
        if (target.level().isClientSide || !(target instanceof CompoundAzurePythonEntity)) {
            return;
        }

        DamageSource source = event.getSource();
        Entity directEntity = source.getEntity();
        Entity attacker = directEntity;
        if (directEntity instanceof Projectile projectile) {
            attacker = projectile.getOwner();
        }

        if (attacker instanceof LivingEntity livingAttacker
                && livingAttacker.hasEffect(ModEffects.PYTHON_CONSTRICTION.get())) {
            event.setAmount(event.getAmount() * 1.5F);
        }
    }
}
