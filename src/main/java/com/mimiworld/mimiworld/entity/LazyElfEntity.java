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
 * 懒精灵（注册 id = lazy_elf）——陆地敌对小人型生物（神话地精/哥布林意象），
 * 歪戴稻草金帽、身披米袋破袍，平时佝偻着背碎步游荡，发现玩家后蹲身蓄力、
 * 猛扑上来双爪挥击并张口撕咬。
 *
 * <p>GeckoLib 资源（Blockbench 导出）：
 * geo/lanjingling.geo.json（geometry.lanjingling，16 骨骼）、
 * textures/entity/lanjingling.png（128×128）、
 * animations/lanjingling.animation.json（animation.lanjingling.idle/.walk/.attack，
 * 另有 attack_throw 投掷动画备用）。</p>
 *
 * <p>400 心、撕咬 100；佝偻小人造型，碰撞箱 0.6×1.2。</p>
 */
public class LazyElfEntity extends Monster implements GeoEntity {

    /** 同步数据：扑击动画剩余 tick（服务端咬中时置 20 并逐 tick 递减，自动同步到客户端）。 */
    private static final EntityDataAccessor<Integer> DATA_ATTACK_ANIM_TICKS =
            SynchedEntityData.defineId(LazyElfEntity.class, EntityDataSerializers.INT);

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    private static final RawAnimation ANIM_IDLE =
            RawAnimation.begin().thenLoop("animation.lanjingling.idle");
    private static final RawAnimation ANIM_WALK =
            RawAnimation.begin().thenLoop("animation.lanjingling.walk");
    private static final RawAnimation ANIM_ATTACK =
            RawAnimation.begin().thenPlay("animation.lanjingling.attack");

    /** attack 动画时长 1 秒 = 20 tick */
    private static final int ATTACK_ANIMATION_TICKS = 20;

    public LazyElfEntity(EntityType<? extends LazyElfEntity> type, Level level) {
        super(type, level);
    }

    /** 属性：400 心 = 800 血、撕咬 100、移速 0.3、追击 32。 */
    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 800.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.3D)
                .add(Attributes.ATTACK_DAMAGE, 100.0D)
                .add(Attributes.FOLLOW_RANGE, 32.0D);
    }

    @Override
    protected void registerGoals() {
        // 哥布林式碎步快步扑击，1.2 倍追击速度
        this.goalSelector.addGoal(1, new MeleeAttackGoal(this, 1.2D, false));
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
            this.playSound(SoundEvents.PILLAGER_AMBIENT, 1.0F, 1.2F);
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

    private PlayState handleAnimState(AnimationState<LazyElfEntity> state) {
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

    // -------------------- 音效：地精式粗嘎低吼（袭击者音效升调） --------------------

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.PILLAGER_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.PILLAGER_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.PILLAGER_DEATH;
    }

    @Override
    public float getVoicePitch() {
        return 1.2F;
    }
}
