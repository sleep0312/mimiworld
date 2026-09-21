package com.mimiworld.mimiworld.entity;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;

/**
 * 米米世界通用「占位生物」实体。
 *
 * <p>用户实体编号 1-30 中，除 {@link MicroDuckEntity}（盗米者）与
 * {@link StickyRiceSpiderEntity}（黏米之蛛）外，其余 28 个实体的专属 AI/模型
 * 尚未设计，先用本类统一占位，保证实体可注册、可 /summon、可挂载战利品表。</p>
 *
 * <p>每个实体的实际血量/攻击力来自 {@link ModEntities#SPECS}，
 * 在 EntityAttributeCreationEvent 中按编号单独 put，故本类不写死属性。</p>
 *
 * <p>AI 按注册时的 MobCategory 自适应：MONSTER（敌对，用户编号 21-30）
 * 带近战攻击与主动索敌；CREATURE（友好，编号 1-20）仅游荡观望。</p>
 */
public class PlaceholderRiceMobEntity extends PathfinderMob {

    public PlaceholderRiceMobEntity(EntityType<? extends PathfinderMob> type, Level level) {
        super(type, level);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        if (this.getType().getCategory() == MobCategory.MONSTER) {
            // 敌对：近战（伤害取该实体 ATTACK_DAMAGE 属性）+ 反击 + 主动追击玩家
            this.goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.0D, true));
            this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
            this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
        }
        this.goalSelector.addGoal(6, new WaterAvoidingRandomStrollGoal(this, 1.0D));
        this.goalSelector.addGoal(7, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(8, new RandomLookAroundGoal(this));
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
