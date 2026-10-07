package com.mimiworld.mimiworld.entity;

import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

/**
 * 米蜃（注册 id = rice_mirage）——陆地敌对爬虫（精致化蠹虫意象），
 * 贴地六足疾行、发现玩家后猛冲上来大颚啃咬。
 *
 * <p>GeckoLib 资源（Blockbench 导出）：
 * geo/mishen.geo.json（geometry.mishen，17 骨骼）、
 * textures/entity/mishen.png（64×64）、
 * animations/mishen.animation.json（animation.mishen.idle/.walk/.attack）。</p>
 *
 * <p>750 心、啃咬 400；贴地低伏，碰撞箱 1.0×0.8。</p>
 */
public class MishenEntity extends Monster implements GeoEntity {

    /** 同步数据：啃咬动画剩余 tick（服务端咬中时置 20 并逐 tick 递减，自动同步到客户端）。 */
    private static final EntityDataAccessor<Integer> DATA_ATTACK_ANIM_TICKS =
            SynchedEntityData.defineId(MishenEntity.class, EntityDataSerializers.INT);

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    private static final RawAnimation ANIM_IDLE =
            RawAnimation.begin().thenLoop("animation.mishen.idle");
    private static final RawAnimation ANIM_WALK =
            RawAnimation.begin().thenLoop("animation.mishen.walk");
    private static final RawAnimation ANIM_ATTACK =
            RawAnimation.begin().thenPlay("animation.mishen.attack");

    /** attack 动画时长 1 秒 = 20 tick */
    private static final int ATTACK_ANIMATION_TICKS = 20;

    public MishenEntity(EntityType<? extends MishenEntity> type, Level level) {
        super(type, level);
    }

    /** 属性：750 心 = 1500 血、啃咬 400、疾行 0.35、追击 32。 */
    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 1500.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.35D)
                .add(Attributes.ATTACK_DAMAGE, 400.0D)
                .add(Attributes.FOLLOW_RANGE, 32.0D);
    }

    @Override
    protected void registerGoals() {
        // 蠹虫式贴地猛冲啃咬，1.3 倍疾行速度
        this.goalSelector.addGoal(1, new MeleeAttackGoal(this, 1.3D, false));
        this.goalSelector.addGoal(3, new WaterAvoidingRandomStrollGoal(this, 0.9D));
        this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 10.0F));
        this.goalSelector.addGoal(7, new RandomLookAroundGoal(this));

        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (!this.level().isClientSide) {
            int ticks = this.entityData.get(DATA_ATTACK_ANIM_TICKS);
            if (ticks > 0) {
                this.entityData.set(DATA_ATTACK_ANIM_TICKS, ticks - 1);
            }
        }
    }

    @Override
    public boolean doHurtTarget(Entity target) {
        if (super.doHurtTarget(target)) {
            this.entityData.set(DATA_ATTACK_ANIM_TICKS, ATTACK_ANIMATION_TICKS);
            this.playSound(SoundEvents.SILVERFISH_AMBIENT, 1.0F, 0.9F);
            return true;
        }
        return false;
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_ATTACK_ANIM_TICKS, 0);
    }

    // -------------------- GeckoLib 动画 --------------------

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "main", 5, this::handleAnimState));
    }

    private PlayState handleAnimState(AnimationState<MishenEntity> state) {
        if (this.entityData.get(DATA_ATTACK_ANIM_TICKS) > 0) {
            state.setAndContinue(ANIM_ATTACK);
        } else {
            boolean moving = state.isMoving() || this.getDeltaMovement().horizontalDistanceSqr() > 1.0E-4D;
            state.setAndContinue(moving ? ANIM_WALK : ANIM_IDLE);
        }
        return PlayState.CONTINUE;
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }

    // -------------------- 音效：蠹虫沙沙声 --------------------

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.SILVERFISH_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.SILVERFISH_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.SILVERFISH_DEATH;
    }
}
