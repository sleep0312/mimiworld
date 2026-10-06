package com.mimiworld.mimiworld.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;

/**
 * 虚无触毒液投掷物：从巨口喷出的绿色毒团，按抛物线飞行（受重力）。
 *
 * <p>命中生物造成 6 点伤害并附加中毒 II 8 秒；命中方块溅起一滩史莱姆样毒浆粒子后消失。
 * 渲染由 ThrownItemRenderer 直接使用史莱姆球物品贴图，无需额外实体纹理。</p>
 */
public class VoidPoisonSpitEntity extends ThrowableItemProjectile {

    public VoidPoisonSpitEntity(EntityType<? extends VoidPoisonSpitEntity> type, Level level) {
        super(type, level);
    }

    public VoidPoisonSpitEntity(EntityType<? extends VoidPoisonSpitEntity> type, LivingEntity shooter, Level level) {
        super(type, shooter, level);
    }

    @Override
    protected Item getDefaultItem() {
        return Items.SLIME_BALL;
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        super.onHitEntity(result);
        Entity target = result.getEntity();
        target.hurt(this.damageSources().thrown(this, this.getOwner()), 6.0F);
        if (target instanceof LivingEntity living) {
            living.addEffect(new MobEffectInstance(MobEffects.POISON, 160, 1)); // 中毒 II 8 秒
        }
    }

    @Override
    protected void onHitBlock(net.minecraft.world.phys.BlockHitResult result) {
        super.onHitBlock(result);
        // 命中方块时溅起史莱姆方块样式的绿色碎粒（服务端发包，所有客户端可见）
        BlockPos pos = result.getBlockPos();
        this.level().levelEvent(null, 2002, pos,
                net.minecraft.world.level.block.Block.getId(Blocks.SLIME_BLOCK.defaultBlockState()));
    }

    @Override
    protected void onHit(HitResult result) {
        super.onHit(result);
        if (!this.level().isClientSide) {
            this.discard();
        }
    }
}
