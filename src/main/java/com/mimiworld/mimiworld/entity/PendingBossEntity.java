package com.mimiworld.mimiworld.entity;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;

/**
 * 151-157 生物蛋绑定的「待定 Boss」占位实体。
 *
 * <p>当前七种 Boss（米米猴王/饭晶虎王/超晶牛王/重晶兔王/复晶天青蟒/清洁长命夔/最懒终末龙）
 * 的专属 AI、模型与属性尚未设计，先用本类统一占位，保证生物蛋可注册、可生成、不崩游戏。
 * 后续逐个实现真实实体类时，只需把 ModEntities 中对应 EntityType 的工厂从
 * {@code PendingBossEntity::new} 换成真实类构造引用，生物蛋无需改动。</p>
 */
public class PendingBossEntity extends Mob {

    public PendingBossEntity(EntityType<? extends Mob> type, Level level) {
        super(type, level);
    }

    /** 通用占位属性：100 血、常规移速、8 点攻击；真实 Boss 实现后由各自属性覆盖 */
    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 100.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.3D)
                .add(Attributes.ATTACK_DAMAGE, 8.0D)
                .add(Attributes.FOLLOW_RANGE, 32.0D)
                .add(Attributes.ARMOR, 4.0D);
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return null;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.GENERIC_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.GENERIC_DEATH;
    }
}
