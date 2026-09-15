package com.mimiworld.mimiworld.entity;

import com.mimiworld.mimiworld.ModItems;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;

/**
 * 熟米团投掷物实体。命中生物时造成 2.5 点伤害，并施加缓慢 III、失明 II 各 10 秒；命中后消失。
 */
public class CookedRiceBallProjectile extends ThrowableItemProjectile {

    public CookedRiceBallProjectile(EntityType<? extends CookedRiceBallProjectile> type, Level level) {
        super(type, level);
    }

    public CookedRiceBallProjectile(EntityType<? extends CookedRiceBallProjectile> type, LivingEntity shooter, Level level) {
        super(type, shooter, level);
    }

    @Override
    protected Item getDefaultItem() {
        return ModItems.COOKED_RICE_BALL.get();
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        super.onHitEntity(result);
        Entity target = result.getEntity();
        // 被击中者受到 2.5 点（1.25 颗心）伤害
        target.hurt(this.damageSources().thrown(this, this.getOwner()), 2.5F);
        if (target instanceof LivingEntity living) {
            living.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 200, 2)); // 缓慢 III 10s
            living.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 200, 1));          // 失明 II 10s
        }
        if (!this.level().isClientSide) {
            this.discard();
        }
    }
}
