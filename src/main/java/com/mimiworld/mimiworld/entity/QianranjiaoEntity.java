package com.mimiworld.mimiworld.entity;

import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RandomSwimmingGoal;
import net.minecraft.world.entity.ai.goal.TryFindWaterGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.navigation.AmphibiousPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

/**
 * 浅染鲛（注册 id = pale_mermaid）——水生敌对鲸类（虎鲸+抹香鲸意象），约玩家两倍大，
 * 在水中巡游、发现玩家后加速突进啃咬；搁浅时会扑腾着跳回水里。
 *
 * <p>GeckoLib 资源（Blockbench 导出）：
 * geo/qianranjiao.geo.json（geometry.qianranjiao，12 骨骼）、
 * textures/entity/qianranjiao.png（128×256）、
 * animations/qianranjiao.animation.json（animation.qianranjiao.idle/.walk/.attack）。</p>
 *
 * <p>60 心、啃咬 10 伤害；水下用 {@link AmphibiousPathNavigation}（水陆两栖寻路，
 * 搁浅时也能寻回水中），离水不会窒息但无法灵活移动，只会像鱼一样扑腾。</p>
 */
public class QianranjiaoEntity extends Monster implements GeoEntity {

    /** 同步数据：啃咬动画剩余 tick（服务端咬中时置 22 并逐 tick 递减，自动同步到客户端）。 */
    private static final EntityDataAccessor<Integer> DATA_ATTACK_ANIM_TICKS =
            SynchedEntityData.defineId(QianranjiaoEntity.class, EntityDataSerializers.INT);

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    private static final RawAnimation ANIM_IDLE =
            RawAnimation.begin().thenLoop("animation.qianranjiao.idle");
    private static final RawAnimation ANIM_WALK =
            RawAnimation.begin().thenLoop("animation.qianranjiao.walk");
    private static final RawAnimation ANIM_ATTACK =
            RawAnimation.begin().thenPlay("animation.qianranjiao.attack");

    /** attack 动画时长 1.1 秒 = 22 tick */
    private static final int ATTACK_ANIMATION_TICKS = 22;

    public QianranjiaoEntity(EntityType<? extends QianranjiaoEntity> type, Level level) {
        super(type, level);
    }

    /** 属性：60 心 = 120 血、啃咬 10、水中巡游速度 1.2、追击 32、半击退抗性。 */
    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 120.0D)
                .add(Attributes.MOVEMENT_SPEED, 1.2D)
                .add(Attributes.ATTACK_DAMAGE, 10.0D)
                .add(Attributes.FOLLOW_RANGE, 32.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.5D);
    }

    @Override
    protected void registerGoals() {
        // 搁浅时优先寻路回水中
        this.goalSelector.addGoal(0, new TryFindWaterGoal(this));
        // 发现目标后加速突进，1.25 倍巡游速度
        this.goalSelector.addGoal(1, new MeleeAttackGoal(this, 1.25D, true));
        // 无目标时随机水中巡游
        this.goalSelector.addGoal(4, new RandomSwimmingGoal(this, 1.0D, 30));
        this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 12.0F));
        this.goalSelector.addGoal(7, new RandomLookAroundGoal(this));

        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
    }

    /** 两栖寻路：水中可全向游（含上下层），搁浅时也能在陆地寻回水中。 */
    @Override
    protected PathNavigation createNavigation(Level level) {
        return new AmphibiousPathNavigation(this, level);
    }

    /** 水生：水下不会溺水。 */
    @Override
    public boolean canBreatheUnderwater() {
        return true;
    }

    /** 不会被水流推动。 */
    @Override
    public boolean isPushedByFluid() {
        return false;
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
        // 搁浅扑腾：在陆地上隔几拍朝面朝方向蹦一下（同原版鱼离水逻辑）
        if (!this.isInWater() && this.onGround() && this.getRandom().nextFloat() < 0.12F) {
            Vec3 facing = this.getLookAngle();
            this.setDeltaMovement(this.getDeltaMovement()
                    .add(facing.x * 0.18D, 0.38D, facing.z * 0.18D));
            this.hasImpulse = true;
        }
    }

    @Override
    public boolean doHurtTarget(Entity target) {
        if (super.doHurtTarget(target)) {
            this.entityData.set(DATA_ATTACK_ANIM_TICKS, ATTACK_ANIMATION_TICKS);
            this.playSound(SoundEvents.DOLPHIN_ATTACK, 1.0F, 0.8F);
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

    private PlayState handleAnimState(AnimationState<QianranjiaoEntity> state) {
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

    // -------------------- 音效：海豚音（低沉播放，贴近凶猛鲸类） --------------------

    @Override
    protected SoundEvent getAmbientSound() {
        return this.isInWater() ? SoundEvents.DOLPHIN_AMBIENT_WATER : SoundEvents.DOLPHIN_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.DOLPHIN_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.DOLPHIN_DEATH;
    }
}
