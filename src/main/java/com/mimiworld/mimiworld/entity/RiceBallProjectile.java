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
 * 米团投掷物实体。命中生物时造成 1 点伤害，并施加反胃 I、缓慢 I 各 10 秒；命中后消失。
 * 渲染由 ThrownItemRenderer 直接使用物品贴图，无需额外实体纹理。
 */
public class RiceBallProjectile extends ThrowableItemProjectile {

    public RiceBallProjectile(EntityType<? extends RiceBallProjectile> type, Level level) {
        super(type, level);
    }

    public RiceBallProjectile(EntityType<? extends RiceBallProjectile> type, LivingEntity shooter, Level level) {
        super(type, shooter, level);
    }

    @Override
    protected Item getDefaultItem() {
        return ModItems.RICE_BALL.get();
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        super.onHitEntity(result);
        Entity target = result.getEntity();
        // 被击中者受到 1 点（半颗心）伤害，伤害来源为该投掷物及其投掷者
        target.hurt(this.damageSources().thrown(this, this.getOwner()), 1.0F);
        if (target instanceof LivingEntity living) {
            living.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 200, 0));        // 反胃 I 10s
            living.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 200, 0)); // 缓慢 I 10s
        }
        if (!this.level().isClientSide) {
            this.discard();
        }
    }
}
