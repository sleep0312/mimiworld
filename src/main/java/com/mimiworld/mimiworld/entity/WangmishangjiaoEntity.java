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
 * 望米伤鲛（注册 id = wangmi_wounded_mermaid）——水生敌对巨型古鲸，约玩家六倍大，
 * 陈米重甲+双前掠长牙（右侧折断）造型；在深海中缓慢巡游，发现玩家后巨兽冲刺前冲，
 * 巨口猛张一口咬下并咬住甩头。搁浅时会沉重地扑腾着跳回水里。
 *
 * <p>GeckoLib 资源（Blockbench 导出）：
 * geo/wangmishangjiao.geo.json（geometry.wangmishangjiao，11 骨骼）、
 * textures/entity/wangmishangjiao.png（512×1024）、
 * animations/wangmishangjiao.animation.json（animation.wangmishangjiao.idle/.walk/.attack）。</p>
 *
 * <p>1000 心、啃咬 300 伤害；水下用 {@link AmphibiousPathNavigation}（水陆两栖寻路，
 * 搁浅时也能寻回水中），离水不会窒息但身躯沉重，只能笨拙扑腾。</p>
 */
public class WangmishangjiaoEntity extends Monster implements GeoEntity {

    /** 同步数据：啃咬动画剩余 tick（服务端咬中时置 26 并逐 tick 递减，自动同步到客户端）。 */
    private static final EntityDataAccessor<Integer> DATA_ATTACK_ANIM_TICKS =
            SynchedEntityData.defineId(WangmishangjiaoEntity.class, EntityDataSerializers.INT);

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    private static final RawAnimation ANIM_IDLE =
            RawAnimation.begin().thenLoop("animation.wangmishangjiao.idle");
    private static final RawAnimation ANIM_WALK =
            RawAnimation.begin().thenLoop("animation.wangmishangjiao.walk");
    private static final RawAnimation ANIM_ATTACK =
            RawAnimation.begin().thenPlay("animation.wangmishangjiao.attack");

    /** attack 动画时长 1.3 秒 = 26 tick */
    private static final int ATTACK_ANIMATION_TICKS = 26;

    public WangmishangjiaoEntity(EntityType<? extends WangmishangjiaoEntity> type, Level level) {
        super(type, level);
    }

    /** 属性：1000 心 = 2000 血、啃咬 300、巨兽缓慢巡游速度 1.0、追击 48、高击退抗性。 */
    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 2000.0D)
                .add(Attributes.MOVEMENT_SPEED, 1.0D)
                .add(Attributes.ATTACK_DAMAGE, 300.0D)
                .add(Attributes.FOLLOW_RANGE, 48.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.8D);
    }

    @Override
    protected void registerGoals() {
        // 搁浅时优先寻路回水中
        this.goalSelector.addGoal(0, new TryFindWaterGoal(this));
        // 发现目标后巨兽冲刺，1.5 倍巡游速度
        this.goalSelector.addGoal(1, new MeleeAttackGoal(this, 1.5D, true));
        // 无目标时缓慢水中巡游
        this.goalSelector.addGoal(4, new RandomSwimmingGoal(this, 0.9D, 40));
        this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 16.0F));
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
        // 搁浅扑腾：身躯沉重，在陆地上隔几拍费劲儿朝面朝方向蹦一下（同原版鱼离水逻辑）
        if (!this.isInWater() && this.onGround() && this.getRandom().nextFloat() < 0.08F) {
            Vec3 facing = this.getLookAngle();
            this.setDeltaMovement(this.getDeltaMovement()
                    .add(facing.x * 0.22D, 0.45D, facing.z * 0.22D));
            this.hasImpulse = true;
        }
    }

    @Override
    public boolean doHurtTarget(Entity target) {
        if (super.doHurtTarget(target)) {
            this.entityData.set(DATA_ATTACK_ANIM_TICKS, ATTACK_ANIMATION_TICKS);
            this.playSound(SoundEvents.DOLPHIN_ATTACK, 1.4F, 0.55F);
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

    private PlayState handleAnimState(AnimationState<WangmishangjiaoEntity> state) {
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

    // -------------------- 音效：低沉海豚音（慢速播放，贴近巨型古鲸） --------------------

    @Override
    protected SoundEvent getAmbientSound() {
        return this.isInWater() ? SoundEvents.DOLPHIN_AMBIENT_WATER : SoundEvents.DOLPHIN_AMBIENT;
    }

    @Override
    protected float getSoundVolume() {
        return 1.6F;
    }

    @Override
    public float getVoicePitch() {
        return this.isInWater() ? 0.6F : 0.7F;
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
