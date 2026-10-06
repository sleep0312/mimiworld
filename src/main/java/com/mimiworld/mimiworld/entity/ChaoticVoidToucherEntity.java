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
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
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

import java.util.EnumSet;

/**
 * 混沌不可名状之米界虚无触（注册 id = chaotic_void_toucher）——扎根在泥地里的触手巢 Boss。
 *
 * <p>GeckoLib 资源（Blockbench 导出）：
 * geo/hundunbukemingzhuangzhimijiexuwuchu.geo.json（38 骨骼：root 泥地基座 + maw 巨口
 * （颌骨 jaw_n/jaw_s）+ 三只眼眼柄 + 8 条链式分段触手，inner_l/inner_r 为高位打击触手）、
 * textures/entity/hundunbukemingzhuangzhimijiexuwuchu.png（128×128）、
 * animations/hundunbukemingzhuangzhimijiexuwuchu.animation.json
 * （idle 循环待机 / attack 1.2s 触手抽击 / attack_spit 1.6s 张口喷毒）。</p>
 *
 * <p>两种原地攻击（不会移动）：目标 3.5 格内时双高位触手内夹鞭打（50 伤害，attack 动画）；
 * 4~20 格且有视线时蓄力 0.9 秒（attack_spit 动画，喷吐瞬间与 0.9s 峰值对齐）从巨口吐出
 * {@link VoidPoisonSpitEntity} 毒液团（命中造成 6 伤 + 中毒 II 8 秒）。
 * 1500 血、满击退抗性；{@link #travel} 清零水平速度、不可被挤动。音效沿用监守者的深渊低吼、
 * 掠夺者兽的扑击声与羊驼喷吐声。</p>
 */
public class ChaoticVoidToucherEntity extends Monster implements GeoEntity {

    /** 同步数据：触手抽击动画剩余 tick（服务端命中时置 24，逐 tick 递减，自动同步客户端） */
    private static final EntityDataAccessor<Integer> DATA_MELEE_ANIM_TICKS =
            SynchedEntityData.defineId(ChaoticVoidToucherEntity.class, EntityDataSerializers.INT);
    /** 同步数据：喷毒动画剩余 tick（服务端开火时置 32） */
    private static final EntityDataAccessor<Integer> DATA_SPIT_ANIM_TICKS =
            SynchedEntityData.defineId(ChaoticVoidToucherEntity.class, EntityDataSerializers.INT);

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    private static final RawAnimation ANIM_IDLE =
            RawAnimation.begin().thenLoop("animation.hundunbukemingzhuangzhimijiexuwuchu.idle");
    private static final RawAnimation ANIM_MELEE =
            RawAnimation.begin().thenPlay("animation.hundunbukemingzhuangzhimijiexuwuchu.attack");
    private static final RawAnimation ANIM_SPIT =
            RawAnimation.begin().thenPlay("animation.hundunbukemingzhuangzhimijiexuwuchu.attack_spit");

    /** attack 动画时长 1.2 秒 = 24 tick */
    private static final int MELEE_ANIMATION_TICKS = 24;
    /** attack_spit 动画时长 1.6 秒 = 32 tick；喷吐峰值在 0.9 秒 = 18 tick */
    private static final int SPIT_ANIMATION_TICKS = 32;
    private static final int SPIT_WINDUP_TICKS = 18;

    /** 触手抽击距离平方：3.5 格（模型放大后触手前伸约 2 格 + 玩家身位） */
    private static final double MELEE_RANGE_SQR = 12.25D;
    /** 喷毒最近/最远距离平方 */
    private static final double SPIT_MIN_RANGE_SQR = 16.0D;
    private static final double SPIT_MAX_RANGE_SQR = 400.0D;

    /** 服务端临时状态：毒液上涌的蓄力倒计时，归零且目标仍有效时真正吐出；蓄力目标 */
    private int spitWindup;
    private LivingEntity spitTarget;

    public ChaoticVoidToucherEntity(EntityType<? extends ChaoticVoidToucherEntity> type, Level level) {
        super(type, level);
    }

    /** 属性：1500 血、攻击 50（沿用占位数值）；移速 0、追击 36、满击退抗性（扎根）。 */
    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 1500.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.0D)
                .add(Attributes.ATTACK_DAMAGE, 50.0D)
                .add(Attributes.FOLLOW_RANGE, 36.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 1.0D);
    }

    @Override
    protected void registerGoals() {
        // 不注册任何移动类 Goal：近战抽击优先于远程喷毒（二者共用 LOOK 标志，天然互斥）
        this.goalSelector.addGoal(2, new TentacleStrikeGoal());
        this.goalSelector.addGoal(3, new PoisonSpitGoal());
        this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 12.0F));
        this.goalSelector.addGoal(7, new RandomLookAroundGoal(this));

        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    /** 扎根：永久清零水平速度，只让父类结算重力与落地碰撞，水流/冲击都无法把它带走。 */
    @Override
    public void travel(Vec3 travelVector) {
        Vec3 movement = this.getDeltaMovement();
        if (movement.x != 0.0D || movement.z != 0.0D) {
            this.setDeltaMovement(0.0D, movement.y, 0.0D);
        }
        super.travel(Vec3.ZERO);
    }

    /** 触手巢不可被其他实体挤动。 */
    @Override
    public boolean isPushable() {
        return false;
    }

    // -------------------- GeckoLib 动画 --------------------

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "main", 5, this::handleAnimState));
    }

    private PlayState handleAnimState(AnimationState<ChaoticVoidToucherEntity> state) {
        if (this.entityData.get(DATA_MELEE_ANIM_TICKS) > 0) {
            state.setAndContinue(ANIM_MELEE);
        } else if (this.entityData.get(DATA_SPIT_ANIM_TICKS) > 0) {
            state.setAndContinue(ANIM_SPIT);
        } else {
            state.setAndContinue(ANIM_IDLE);
        }
        return PlayState.CONTINUE;
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }

    /** 近战命中成功时播放触手抽击动画（doHurtTarget 由近战 AI 调用）。 */
    @Override
    public boolean doHurtTarget(Entity target) {
        if (super.doHurtTarget(target)) {
            this.entityData.set(DATA_MELEE_ANIM_TICKS, MELEE_ANIMATION_TICKS);
            this.playSound(SoundEvents.RAVAGER_ATTACK, 1.0F, 0.7F);
            return true;
        }
        return false;
    }

    /** 进入喷毒蓄力：播放喷吐动画并在 0.9 秒后的喷吐帧真正吐出毒液。 */
    private void startSpitWindup(LivingEntity target) {
        this.entityData.set(DATA_SPIT_ANIM_TICKS, SPIT_ANIMATION_TICKS);
        this.spitWindup = SPIT_WINDUP_TICKS;
        this.spitTarget = target;
    }

    private boolean isBusyWithAttack() {
        return this.entityData.get(DATA_MELEE_ANIM_TICKS) > 0
                || this.entityData.get(DATA_SPIT_ANIM_TICKS) > 0;
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (this.level().isClientSide) {
            return;
        }
        int melee = this.entityData.get(DATA_MELEE_ANIM_TICKS);
        if (melee > 0) {
            this.entityData.set(DATA_MELEE_ANIM_TICKS, melee - 1);
        }
        int spit = this.entityData.get(DATA_SPIT_ANIM_TICKS);
        if (spit > 0) {
            this.entityData.set(DATA_SPIT_ANIM_TICKS, spit - 1);
        }
        if (this.spitWindup > 0) {
            --this.spitWindup;
            if (this.spitWindup == 0) {
                LivingEntity target = this.spitTarget;
                this.spitTarget = null;
                // 蓄力完成时目标仍存活且仍有视线才吐，避免对着墙喷
                if (target != null && target.isAlive() && this.getSensing().hasLineOfSight(target)) {
                    this.firePoisonSpit(target);
                }
            }
        }
    }

    /** 从巨口高度朝目标抛出一枚毒液团（带重力的抛物线，略带散布）。 */
    private void firePoisonSpit(LivingEntity target) {
        VoidPoisonSpitEntity spit =
                new VoidPoisonSpitEntity(ModEntities.VOID_POISON_SPIT.get(), this, this.level());
        double startX = this.getX();
        double startY = this.getY() + 0.9D;
        double startZ = this.getZ();
        spit.setPos(startX, startY, startZ);
        double dx = target.getX() - startX;
        double dy = target.getY(0.5D) - startY;
        double dz = target.getZ() - startZ;
        double horiz = Math.sqrt(dx * dx + dz * dz);
        // 略抬高初速补偿重力下坠；速度 1.1、散布 1.5
        spit.shoot(dx, dy - horiz * 0.08D, dz, 1.1F, 1.5F);
        this.level().addFreshEntity(spit);
        this.playSound(SoundEvents.LLAMA_SPIT, 1.0F, 0.8F);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_MELEE_ANIM_TICKS, 0);
        this.entityData.define(DATA_SPIT_ANIM_TICKS, 0);
    }

    // -------------------- 音效：深渊低吼 + 喷吐 --------------------

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.WARDEN_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.WARDEN_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.WARDEN_DEATH;
    }

    @Override
    public int getAmbientSoundInterval() {
        return 240;
    }

    // -------------------- 攻击 AI --------------------

    /**
     * 触手抽击：面朝 3.5 格内的目标，每 50 tick 用触手鞭打一次（不位移）。
     */
    private class TentacleStrikeGoal extends Goal {
        private int cooldown;

        TentacleStrikeGoal() {
            this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            LivingEntity target = ChaoticVoidToucherEntity.this.getTarget();
            return target != null && target.isAlive()
                    && !ChaoticVoidToucherEntity.this.isBusyWithAttack();
        }

        @Override
        public boolean canContinueToUse() {
            LivingEntity target = ChaoticVoidToucherEntity.this.getTarget();
            return target != null && target.isAlive();
        }

        @Override
        public void tick() {
            --this.cooldown;
            LivingEntity target = ChaoticVoidToucherEntity.this.getTarget();
            if (target == null) {
                return;
            }
            ChaoticVoidToucherEntity.this.getLookControl().setLookAt(
                    target.getX(), target.getY(0.5D), target.getZ(), 10.0F, 10.0F);

            if (this.cooldown <= 0
                    && !ChaoticVoidToucherEntity.this.isBusyWithAttack()
                    && ChaoticVoidToucherEntity.this.distanceToSqr(target) <= MELEE_RANGE_SQR
                    && ChaoticVoidToucherEntity.this.getSensing().hasLineOfSight(target)) {
                if (ChaoticVoidToucherEntity.this.doHurtTarget(target)) {
                    this.cooldown = 50;
                } else {
                    this.cooldown = 10;
                }
            }
        }
    }

    /**
     * 喷毒：目标处于 4~20 格且有视线时，每 75 tick 进入一次蓄力，0.9 秒后的喷吐帧吐出毒液团。
     */
    private class PoisonSpitGoal extends Goal {
        private int cooldown;

        PoisonSpitGoal() {
            this.setFlags(EnumSet.of(Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            LivingEntity target = ChaoticVoidToucherEntity.this.getTarget();
            return target != null && target.isAlive()
                    && !ChaoticVoidToucherEntity.this.isBusyWithAttack();
        }

        @Override
        public boolean canContinueToUse() {
            LivingEntity target = ChaoticVoidToucherEntity.this.getTarget();
            return target != null && target.isAlive();
        }

        @Override
        public void tick() {
            --this.cooldown;
            LivingEntity target = ChaoticVoidToucherEntity.this.getTarget();
            if (target == null) {
                return;
            }
            ChaoticVoidToucherEntity.this.getLookControl().setLookAt(
                    target.getX(), target.getY(0.5D), target.getZ(), 10.0F, 10.0F);

            double distSqr = ChaoticVoidToucherEntity.this.distanceToSqr(target);
            if (this.cooldown <= 0
                    && !ChaoticVoidToucherEntity.this.isBusyWithAttack()
                    && distSqr >= SPIT_MIN_RANGE_SQR
                    && distSqr <= SPIT_MAX_RANGE_SQR
                    && ChaoticVoidToucherEntity.this.getSensing().hasLineOfSight(target)) {
                ChaoticVoidToucherEntity.this.startSpitWindup(target);
                this.cooldown = 75;
            }
        }
    }
}
