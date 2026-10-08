package com.mimiworld.mimiworld.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
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
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
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
import java.util.List;

/**
 * 米米猴王（Mimi Monkey King，注册 id = mimi_monkey_king）——手持金箍棒的敌对陆地 Boss。
 *
 * <p>模型/贴图/动画资源（均由 Blockbench 导出）：
 * geo/mimihouwang.geo.json、textures/entity/mimihouwang.png、
 * animations/mimihouwang.animation.json
 * （animation.mimihouwang.idle / walk / attack / attack_slam / throw）。
 * 碰撞箱 0.7×1.9，火焰免疫（沿用 registerBoss 的 Boss 设置）。</p>
 *
 * <p>三种攻击方式：</p>
 * <ol>
 *   <li>普通近战：MeleeAttackGoal 驱动，造成 5 点伤害，attack 动画 0.8 秒（16 tick）。</li>
 *   <li>劈棍（attack_slam）：动画 1.4 秒（28 tick），第 18 tick（0.9 秒砸地帧）
 *       对前方 3×3 范围内的实体造成 7 点伤害，冷却 5.5 秒（110 tick）。</li>
 *   <li>掷棍（throw）：动画 2.0 秒（40 tick），第 10 tick（0.5 秒出手帧）生成
 *       {@link ThrownMonkeyStaffEntity}，直线飞行 1 秒后自动追踪飞回，
 *       对路径上的实体造成 20 点伤害，冷却 10 秒（200 tick）。</li>
 * </ol>
 *
 * <p>技能期间通过 Goal 互斥标志（MOVE+LOOK）锁定移动与普攻；动画优先级
 * throw &gt; attack_slam &gt; attack &gt; walk &gt; idle。150 血、移速 0.3、护甲 15、
 * 50% 击退抗性，音效沿用原版鹦鹉（猴子观感）。</p>
 */
public class MimiMonkeyKingEntity extends Monster implements GeoEntity {

    // -------------------- 同步动画状态（服务端递减，自动同步客户端） --------------------

    private static final EntityDataAccessor<Integer> DATA_ATTACK_ANIM_TICKS =
            SynchedEntityData.defineId(MimiMonkeyKingEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_SLAM_ANIM_TICKS =
            SynchedEntityData.defineId(MimiMonkeyKingEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_THROW_ANIM_TICKS =
            SynchedEntityData.defineId(MimiMonkeyKingEntity.class, EntityDataSerializers.INT);
    /** 金箍棒已脱手：为 true 时隐藏模型上的 staff 骨骼（真实棒由投掷物实体渲染） */
    private static final EntityDataAccessor<Boolean> DATA_STAFF_OUT =
            SynchedEntityData.defineId(MimiMonkeyKingEntity.class, EntityDataSerializers.BOOLEAN);

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    private static final RawAnimation ANIM_IDLE = RawAnimation.begin().thenLoop("animation.mimihouwang.idle");
    private static final RawAnimation ANIM_WALK = RawAnimation.begin().thenLoop("animation.mimihouwang.walk");
    private static final RawAnimation ANIM_ATTACK = RawAnimation.begin().thenPlay("animation.mimihouwang.attack");
    private static final RawAnimation ANIM_SLAM = RawAnimation.begin().thenPlay("animation.mimihouwang.attack_slam");
    private static final RawAnimation ANIM_THROW = RawAnimation.begin().thenPlay("animation.mimihouwang.throw");

    /** attack 动画 0.8 秒 = 16 tick */
    private static final int ATTACK_ANIMATION_TICKS = 16;
    /** attack_slam 动画 1.4 秒 = 28 tick */
    private static final int SLAM_ANIMATION_TICKS = 28;
    /** 劈棍伤害结算时刻：动画 0.9 秒砸地帧 = 第 18 tick */
    private static final int SLAM_HIT_TICK = 18;
    /** 劈棍冷却：5.5 秒 = 110 tick */
    private static final int SLAM_COOLDOWN_TICKS = 110;
    /** 劈棍伤害 */
    private static final float SLAM_DAMAGE = 7.0F;
    /** throw 动画 2.0 秒 = 40 tick */
    private static final int THROW_ANIMATION_TICKS = 40;
    /** 金箍棒出手时刻：动画 0.5 秒 = 第 10 tick */
    private static final int THROW_RELEASE_TICK = 10;
    /** 掷棍冷却：10 秒 = 200 tick */
    private static final int THROW_COOLDOWN_TICKS = 200;
    /** 首次技能前的准备时间，避免出生瞬间立刻出招 */
    private static final int INITIAL_SLAM_DELAY = 100;
    private static final int INITIAL_THROW_DELAY = 50;

    // 技能冷却（仅服务端计时）
    private int slamCooldown = INITIAL_SLAM_DELAY;
    private int throwCooldown = INITIAL_THROW_DELAY;

    public MimiMonkeyKingEntity(EntityType<? extends MimiMonkeyKingEntity> type, Level level) {
        super(type, level);
    }

    /**
     * 属性：150 血、移速 0.3、近战攻击 5（普攻伤害）、追击 32、护甲 15、50% 击退抗性。
     */
    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 150.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.3D)
                .add(Attributes.ATTACK_DAMAGE, 5.0D)
                .add(Attributes.FOLLOW_RANGE, 32.0D)
                .add(Attributes.ARMOR, 15.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.5D);
    }

    /**
     * AI：0 漂浮；1 扔棒（中远距离优先）；2 劈地（近身范围）；3 普攻追击；
     * 5 避水游荡；6 看玩家；7 张望。三个攻击 Goal 共用 MOVE+LOOK 标志，天然互斥，
     * 技能释放期间移动与普攻都会被锁住。
     */
    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new ThrowStaffGoal());
        this.goalSelector.addGoal(2, new SlamGoal());
        this.goalSelector.addGoal(3, new MeleeAttackGoal(this, 1.0D, false));
        this.goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 0.8D));
        this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(7, new RandomLookAroundGoal(this));

        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    // -------------------- GeckoLib 动画 --------------------

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "main", 5, this::handleAnimState));
    }

    private PlayState handleAnimState(AnimationState<MimiMonkeyKingEntity> state) {
        if (this.entityData.get(DATA_THROW_ANIM_TICKS) > 0) {
            state.setAndContinue(ANIM_THROW);
        } else if (this.entityData.get(DATA_SLAM_ANIM_TICKS) > 0) {
            state.setAndContinue(ANIM_SLAM);
        } else if (this.entityData.get(DATA_ATTACK_ANIM_TICKS) > 0) {
            state.setAndContinue(ANIM_ATTACK);
        } else if (state.isMoving()) {
            state.setAndContinue(ANIM_WALK);
        } else {
            state.setAndContinue(ANIM_IDLE);
        }
        return PlayState.CONTINUE;
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }

    public boolean isStaffOut() {
        return this.entityData.get(DATA_STAFF_OUT);
    }

    /**
     * 是否正在释放技能（劈棍/掷棍）。注意：普攻动画不计入——普攻每 20 tick 一轮、
     * 动画占 16 tick，若普攻也算 casting，技能每轮只剩 4 tick 起手窗口，会几乎放不出来。
     */
    private boolean isCasting() {
        return this.entityData.get(DATA_THROW_ANIM_TICKS) > 0
                || this.entityData.get(DATA_SLAM_ANIM_TICKS) > 0;
    }

    // -------------------- 攻击逻辑 --------------------

    /** 普攻命中时触发 attack 动画（服务端结算成功一次，数据自动同步） */
    @Override
    public boolean doHurtTarget(Entity target) {
        if (super.doHurtTarget(target)) {
            this.entityData.set(DATA_ATTACK_ANIM_TICKS, ATTACK_ANIMATION_TICKS);
            return true;
        }
        return false;
    }

    /**
     * 劈棍结算（attack_slam 第 18 tick = 0.9 秒砸地帧）。
     *
     * <p>命中帧先把身体朝向硬吸附到当前目标，保证视觉砸棍方向与判定区一致；
     * 判定区为前方 3×3（纵深 0~3 格、左右各 1.5 格），用目标 AABB 中心点并外扩
     * 实体体积补偿，避免「脚部单点」擦边漏判；垂直方向按 AABB 高度区间相交判断，
     * 站立与跳起的目标都能命中。命中造成 7 点伤害与向外冲击。</p>
     */
    private void performSlam() {
        LivingEntity lockedTarget = this.getTarget();
        float slamYaw = this.getYRot();
        if (lockedTarget != null) {
            slamYaw = (float) Math.toDegrees(Math.atan2(
                    -(lockedTarget.getX() - this.getX()),
                    lockedTarget.getZ() - this.getZ()));
            // 硬吸附身体与头部朝向，消除 LookControl 缓转导致的方向偏差
            this.setYRot(slamYaw);
            this.yBodyRot = slamYaw;
            this.setYHeadRot(slamYaw);
        }

        Vec3 base = this.position();
        Vec3 facing = Vec3.directionFromRotation(0.0F, slamYaw);
        Vec3 right = new Vec3(-facing.z, 0.0D, facing.x);

        // 体积补偿：生物 AABB 半宽约 0.3~0.5，边界外扩 0.6 格，擦边也算命中
        final double halfWidth = 1.5D;
        final double depth = 3.0D;
        final double margin = 0.6D;

        AABB searchArea = this.getBoundingBox().inflate(5.0D);
        List<LivingEntity> targets = this.level().getEntitiesOfClass(LivingEntity.class, searchArea,
                target -> target != this && target.isAlive() && !target.isAlliedTo(this));
        for (LivingEntity target : targets) {
            // 水平用 AABB 中心（不是脚部单点），垂直用高度区间相交
            Vec3 center = target.getBoundingBox().getCenter();
            double relX = center.x - base.x;
            double relZ = center.z - base.z;
            double forward = relX * facing.x + relZ * facing.z;
            double side = relX * right.x + relZ * right.z;
            boolean horizontalHit = forward >= -margin && forward <= depth + margin
                    && Math.abs(side) <= halfWidth + margin;
            boolean verticalHit = target.getBoundingBox().maxY >= base.y - 0.5D
                    && target.getBoundingBox().minY <= base.y + 3.0D;
            if (horizontalHit && verticalHit) {
                target.hurt(this.damageSources().mobAttack(this), SLAM_DAMAGE);
                // 背离猴王的水平冲击，强化砸棍命中感
                double pushX = target.getX() - this.getX();
                double pushZ = target.getZ() - this.getZ();
                double pushLen = Math.sqrt(pushX * pushX + pushZ * pushZ);
                if (pushLen > 1.0E-4D) {
                    target.push(pushX / pushLen * 0.55D, 0.25D, pushZ / pushLen * 0.55D);
                }
            }
        }

        this.playSound(SoundEvents.GENERIC_EXPLODE, 0.7F, 1.3F);
        if (this.level() instanceof net.minecraft.server.level.ServerLevel serverLevel) {
            // 粒子铺满前方 3×3：纵深 3 排 × 横向 3 列
            for (int d = 0; d < 3; d++) {
                for (int s = -1; s <= 1; s++) {
                    double dist = 0.7D + d * 1.05D;
                    double cx = base.x + facing.x * dist + right.x * s * 1.05D
                            + (this.random.nextDouble() - 0.5D) * 0.5D;
                    double cz = base.z + facing.z * dist + right.z * s * 1.05D
                            + (this.random.nextDouble() - 0.5D) * 0.5D;
                    serverLevel.sendParticles(ParticleTypes.CLOUD,
                            cx, base.y + 0.15D, cz, 5, 0.2D, 0.08D, 0.2D, 0.02D);
                    if (d == 1 && s == 0) {
                        serverLevel.sendParticles(ParticleTypes.EXPLOSION,
                                cx, base.y + 0.2D, cz, 1, 0.1D, 0.05D, 0.1D, 0.0D);
                    }
                }
            }
        }
    }

    /** 第 10 tick 出手：在胸前生成金箍棒投掷物，朝目标眼部直线飞出，并隐藏本体棒骨骼 */
    private void launchStaffAt(LivingEntity target) {
        ThrownMonkeyStaffEntity staff = new ThrownMonkeyStaffEntity(
                ModEntities.THROWN_MONKEY_STAFF.get(), this, this.level());
        staff.setPos(this.getX(), this.getY() + 1.35D, this.getZ());
        staff.setYRot(this.getYRot());
        staff.launchTowards(target.getEyePosition());
        this.level().addFreshEntity(staff);
        this.entityData.set(DATA_STAFF_OUT, true);
        this.playSound(SoundEvents.TRIDENT_THROW, 1.0F, 0.85F + this.random.nextFloat() * 0.3F);
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (!this.level().isClientSide) {
            if (this.slamCooldown > 0) {
                this.slamCooldown--;
            }
            if (this.throwCooldown > 0) {
                this.throwCooldown--;
            }
            int attackTicks = this.entityData.get(DATA_ATTACK_ANIM_TICKS);
            if (attackTicks > 0) {
                this.entityData.set(DATA_ATTACK_ANIM_TICKS, attackTicks - 1);
            }
            int slamTicks = this.entityData.get(DATA_SLAM_ANIM_TICKS);
            if (slamTicks > 0) {
                this.entityData.set(DATA_SLAM_ANIM_TICKS, slamTicks - 1);
            }
            int throwTicks = this.entityData.get(DATA_THROW_ANIM_TICKS);
            if (throwTicks > 0) {
                this.entityData.set(DATA_THROW_ANIM_TICKS, throwTicks - 1);
                if (throwTicks - 1 == 0) {
                    // 兜底：扔棒动画结束时恢复手中棒（正常在 Goal.stop 已复位）
                    this.entityData.set(DATA_STAFF_OUT, false);
                }
            }
        }
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_ATTACK_ANIM_TICKS, 0);
        this.entityData.define(DATA_SLAM_ANIM_TICKS, 0);
        this.entityData.define(DATA_THROW_ANIM_TICKS, 0);
        this.entityData.define(DATA_STAFF_OUT, false);
    }

    // -------------------- 技能 Goal --------------------

    /**
     * 劈棍 Goal：目标进入 4 格内且有视线时释放，前 18 tick 蓄力举棍，
     * 第 18 tick 结算前方 3×3 范围 7 点伤害，随后收招；冷却 110 tick。
     */
    private class SlamGoal extends Goal {
        private int runTicks;

        SlamGoal() {
            this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            LivingEntity target = MimiMonkeyKingEntity.this.getTarget();
            return MimiMonkeyKingEntity.this.slamCooldown <= 0
                    && !MimiMonkeyKingEntity.this.isCasting()
                    && target != null && target.isAlive()
                    && MimiMonkeyKingEntity.this.distanceToSqr(target) <= 16.0D
                    && MimiMonkeyKingEntity.this.hasLineOfSight(target);
        }

        @Override
        public boolean canContinueToUse() {
            LivingEntity target = MimiMonkeyKingEntity.this.getTarget();
            return this.runTicks < SLAM_ANIMATION_TICKS && target != null && target.isAlive();
        }

        @Override
        public boolean requiresUpdateEveryTick() {
            return true;
        }

        @Override
        public void start() {
            this.runTicks = 0;
            MimiMonkeyKingEntity.this.getNavigation().stop();
            MimiMonkeyKingEntity.this.entityData.set(DATA_SLAM_ANIM_TICKS, SLAM_ANIMATION_TICKS);
        }

        @Override
        public void tick() {
            this.runTicks++;
            MimiMonkeyKingEntity.this.getNavigation().stop();
            LivingEntity target = MimiMonkeyKingEntity.this.getTarget();
            if (target != null) {
                MimiMonkeyKingEntity.this.getLookControl().setLookAt(target, 30.0F, 30.0F);
            }
            if (this.runTicks == SLAM_HIT_TICK) {
                MimiMonkeyKingEntity.this.performSlam();
            }
        }

        @Override
        public void stop() {
            this.runTicks = 0;
            MimiMonkeyKingEntity.this.slamCooldown = SLAM_COOLDOWN_TICKS;
        }
    }

    /**
     * 掷棍 Goal：目标在 3~16 格且有视线时释放，第 10 tick（动画 0.5 秒出手帧）
     * 生成飞行金箍棒（1 秒后飞回，路径 20 伤害），整段动画期间锁定移动；冷却 200 tick。
     */
    private class ThrowStaffGoal extends Goal {
        private int runTicks;

        ThrowStaffGoal() {
            this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            LivingEntity target = MimiMonkeyKingEntity.this.getTarget();
            if (MimiMonkeyKingEntity.this.throwCooldown > 0
                    || MimiMonkeyKingEntity.this.isCasting()
                    || target == null || !target.isAlive()
                    || !MimiMonkeyKingEntity.this.hasLineOfSight(target)) {
                return false;
            }
            double distSqr = MimiMonkeyKingEntity.this.distanceToSqr(target);
            return distSqr >= 9.0D && distSqr <= 256.0D;
        }

        @Override
        public boolean canContinueToUse() {
            LivingEntity target = MimiMonkeyKingEntity.this.getTarget();
            return this.runTicks < THROW_ANIMATION_TICKS && target != null && target.isAlive();
        }

        @Override
        public boolean requiresUpdateEveryTick() {
            return true;
        }

        @Override
        public void start() {
            this.runTicks = 0;
            MimiMonkeyKingEntity.this.getNavigation().stop();
            MimiMonkeyKingEntity.this.entityData.set(DATA_THROW_ANIM_TICKS, THROW_ANIMATION_TICKS);
        }

        @Override
        public void tick() {
            this.runTicks++;
            MimiMonkeyKingEntity.this.getNavigation().stop();
            LivingEntity target = MimiMonkeyKingEntity.this.getTarget();
            if (target != null) {
                MimiMonkeyKingEntity.this.getLookControl().setLookAt(target, 30.0F, 30.0F);
            }
            if (this.runTicks == THROW_RELEASE_TICK && target != null) {
                MimiMonkeyKingEntity.this.launchStaffAt(target);
            }
        }

        @Override
        public void stop() {
            this.runTicks = 0;
            MimiMonkeyKingEntity.this.throwCooldown = THROW_COOLDOWN_TICKS;
            MimiMonkeyKingEntity.this.entityData.set(DATA_STAFF_OUT, false);
        }
    }

    // -------------------- 音效：沿用原版鹦鹉（猴子观感） --------------------

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.PARROT_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.PARROT_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.PARROT_DEATH;
    }

    @Override
    protected void playStepSound(BlockPos pos, BlockState state) {
        this.playSound(SoundEvents.PARROT_FLY, 0.06F, 1.4F);
    }
}
