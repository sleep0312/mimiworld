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
 * 饭晶虎王（Rice Crystal Tiger King，注册 id = rice_crystal_tiger_king）——直立型敌对陆地 Boss。
 *
 * <p>模型/贴图/动画资源（均由 Blockbench 导出）：
 * geo/fanjinghuwang.geo.json、textures/entity/fanjinghuwang.png、
 * animations/fanjinghuwang.animation.json
 * （animation.fanjinghuwang.idle / walk / attack / feast / slam）。
 * 模型直立约 47px（角尖）≈2.9 格，碰撞箱 1.2×2.9，火焰免疫（沿用 Boss 设置）。</p>
 *
 * <p>三种攻击方式：</p>
 * <ol>
 *   <li>普通近战：MeleeAttackGoal 驱动，造成 8 点伤害，attack 动画 0.8 秒（16 tick）。</li>
 *   <li>【大吃一顿】（feast）：动画 1.5 秒（30 tick），第 12 tick（0.6 秒扑咬帧）
 *       对前方 2×1 范围（左右各 1 格、纵深 1 格）的实体造成 20 点撕咬伤害，冷却 3 秒（60 tick）。</li>
 *   <li>【对爪重击】（slam）：动画 1.6 秒（32 tick），第 16 tick（0.78 秒砸地帧）
 *       双爪砸向地面前方 2×2 范围（左右各 1 格、纵深 2 格），造成 25 点伤害与冲击，
 *       冷却 6 秒（120 tick）。</li>
 * </ol>
 *
 * <p>技能期间通过 Goal 互斥标志（MOVE+LOOK）锁定移动与普攻；动画优先级
 * feast / slam &gt; attack &gt; walk &gt; idle。240 血、移速 0.32、护甲 15、韧性 8、
 * 60% 击退抗性，音效沿用原版狼（低吼/哀鸣）与劫兽（撕咬/咆哮/砸地）。</p>
 */
public class FanjinghuwangEntity extends Monster implements GeoEntity {

    // -------------------- 同步动画状态（服务端递减，自动同步客户端） --------------------

    private static final EntityDataAccessor<Integer> DATA_ATTACK_ANIM_TICKS =
            SynchedEntityData.defineId(FanjinghuwangEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_FEAST_ANIM_TICKS =
            SynchedEntityData.defineId(FanjinghuwangEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_SLAM_ANIM_TICKS =
            SynchedEntityData.defineId(FanjinghuwangEntity.class, EntityDataSerializers.INT);

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    private static final RawAnimation ANIM_IDLE = RawAnimation.begin().thenLoop("animation.fanjinghuwang.idle");
    private static final RawAnimation ANIM_WALK = RawAnimation.begin().thenLoop("animation.fanjinghuwang.walk");
    private static final RawAnimation ANIM_ATTACK = RawAnimation.begin().thenPlay("animation.fanjinghuwang.attack");
    private static final RawAnimation ANIM_FEAST = RawAnimation.begin().thenPlay("animation.fanjinghuwang.feast");
    private static final RawAnimation ANIM_SLAM = RawAnimation.begin().thenPlay("animation.fanjinghuwang.slam");

    /** attack 动画 0.8 秒 = 16 tick */
    private static final int ATTACK_ANIMATION_TICKS = 16;

    /** feast 动画 1.5 秒 = 30 tick */
    private static final int FEAST_ANIMATION_TICKS = 30;
    /** 撕咬伤害结算时刻：动画 0.6 秒扑咬帧 = 第 12 tick */
    private static final int FEAST_HIT_TICK = 12;
    /** 大吃一顿冷却：3 秒 = 60 tick */
    private static final int FEAST_COOLDOWN_TICKS = 60;
    /** 大吃一顿伤害 */
    private static final float FEAST_DAMAGE = 20.0F;
    /** 判定区：左右各 1 格（总宽 2），纵深 1 格（前方 2×1） */
    private static final double FEAST_HALF_WIDTH = 1.0D;
    private static final double FEAST_DEPTH = 1.0D;

    /** slam 动画 1.6 秒 = 32 tick */
    private static final int SLAM_ANIMATION_TICKS = 32;
    /** 对爪重击伤害结算时刻：动画 0.78 秒砸地帧 = 第 16 tick */
    private static final int SLAM_HIT_TICK = 16;
    /** 对爪重击冷却：6 秒 = 120 tick */
    private static final int SLAM_COOLDOWN_TICKS = 120;
    /** 对爪重击伤害 */
    private static final float SLAM_DAMAGE = 25.0F;
    /** 判定区：左右各 1 格（总宽 2），纵深 2 格（前方 2×2） */
    private static final double SLAM_HALF_WIDTH = 1.0D;
    private static final double SLAM_DEPTH = 2.0D;

    /** 首次技能前的准备时间，避免出生瞬间立刻出招 */
    private static final int INITIAL_FEAST_DELAY = 40;
    private static final int INITIAL_SLAM_DELAY = 80;

    // 技能冷却（仅服务端计时）
    private int feastCooldown = INITIAL_FEAST_DELAY;
    private int slamCooldown = INITIAL_SLAM_DELAY;

    public FanjinghuwangEntity(EntityType<? extends FanjinghuwangEntity> type, Level level) {
        super(type, level);
    }

    /**
     * 属性：240 血、移速 0.32、普攻 8、追击 36、护甲 15、护甲韧性 8、60% 击退抗性。
     */
    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 240.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.32D)
                .add(Attributes.ATTACK_DAMAGE, 8.0D)
                .add(Attributes.FOLLOW_RANGE, 36.0D)
                .add(Attributes.ARMOR, 15.0D)
                .add(Attributes.ARMOR_TOUGHNESS, 8.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.6D);
    }

    /**
     * AI：0 漂浮；1 大吃一顿（近身短 CD 撕咬）；2 对爪重击（近身重砸）；3 普攻追击；
     * 5 避水游荡；6 看玩家；7 张望。三个攻击 Goal 共用 MOVE+LOOK 标志，天然互斥，
     * 技能释放期间移动与普攻都会被锁住。
     */
    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new FeastGoal());
        this.goalSelector.addGoal(2, new SlamGoal());
        this.goalSelector.addGoal(3, new MeleeAttackGoal(this, 1.0D, false));
        this.goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 0.8D));
        this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 10.0F));
        this.goalSelector.addGoal(7, new RandomLookAroundGoal(this));

        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    // -------------------- GeckoLib 动画 --------------------

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "main", 5, this::handleAnimState));
    }

    private PlayState handleAnimState(AnimationState<FanjinghuwangEntity> state) {
        if (this.entityData.get(DATA_FEAST_ANIM_TICKS) > 0) {
            state.setAndContinue(ANIM_FEAST);
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

    /**
     * 是否正在释放技能（大吃一顿/对爪重击）。普攻动画不计入——普攻每 20 tick 一轮、
     * 动画占 16 tick，若普攻也算 casting，技能起手窗口会被严重压缩。
     */
    private boolean isCasting() {
        return this.entityData.get(DATA_FEAST_ANIM_TICKS) > 0
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
     * 前方扇形/矩形区域伤害结算（大吃一顿 2×1、对爪重击 2×2 共用）。
     *
     * <p>先把身体朝向硬吸附到当前目标，保证视觉出招方向与判定区一致；
     * 用目标 AABB 中心点并外扩体积补偿，避免「脚部单点」擦边漏判；
     * 垂直方向按 AABB 高度区间相交判断，站立与跳起的目标都能命中。</p>
     *
     * @param halfWidth 判定区左右半宽（格）
     * @param depth     判定区前方纵深（格）
     * @param damage    伤害点数
     * @param push      命中目标的向外冲击强度
     */
    private void performFrontalStrike(double halfWidth, double depth, float damage, double push) {
        LivingEntity lockedTarget = this.getTarget();
        float strikeYaw = this.getYRot();
        if (lockedTarget != null) {
            strikeYaw = (float) Math.toDegrees(Math.atan2(
                    -(lockedTarget.getX() - this.getX()),
                    lockedTarget.getZ() - this.getZ()));
            // 硬吸附身体与头部朝向，消除 LookControl 缓转导致的方向偏差
            this.setYRot(strikeYaw);
            this.yBodyRot = strikeYaw;
            this.setYHeadRot(strikeYaw);
        }

        Vec3 base = this.position();
        Vec3 facing = Vec3.directionFromRotation(0.0F, strikeYaw);
        Vec3 right = new Vec3(-facing.z, 0.0D, facing.x);

        // 体积补偿：生物 AABB 半宽约 0.3~0.5，边界外扩 0.6 格，擦边也算命中
        final double margin = 0.6D;

        AABB searchArea = this.getBoundingBox().inflate(5.0D);
        List<LivingEntity> targets = this.level().getEntitiesOfClass(LivingEntity.class, searchArea,
                target -> target != this && target.isAlive() && !target.isAlliedTo(this));
        for (LivingEntity target : targets) {
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
                target.hurt(this.damageSources().mobAttack(this), damage);
                // 背离虎王的水平冲击，强化命中感
                double pushX = target.getX() - this.getX();
                double pushZ = target.getZ() - this.getZ();
                double pushLen = Math.sqrt(pushX * pushX + pushZ * pushZ);
                if (pushLen > 1.0E-4D) {
                    target.push(pushX / pushLen * push, 0.25D, pushZ / pushLen * push);
                }
            }
        }
    }

    /** 【大吃一顿】第 12 tick 扑咬结算：前方 2×1 造成 20 点撕咬伤害。 */
    private void performFeast() {
        this.performFrontalStrike(FEAST_HALF_WIDTH, FEAST_DEPTH, FEAST_DAMAGE, 0.35D);
        this.playSound(SoundEvents.RAVAGER_ATTACK, 1.0F, 0.85F + this.random.nextFloat() * 0.2F);
        if (this.level() instanceof net.minecraft.server.level.ServerLevel serverLevel) {
            // 撕咬爪痕粒子
            for (int i = 0; i < 14; i++) {
                double dist = 0.6D + this.random.nextDouble() * 1.1D;
                double side = (this.random.nextDouble() - 0.5D) * 1.8D;
                Vec3 facing = Vec3.directionFromRotation(0.0F, this.getYRot());
                Vec3 right = new Vec3(-facing.z, 0.0D, facing.x);
                double cx = this.getX() + facing.x * dist + right.x * side;
                double cz = this.getZ() + facing.z * dist + right.z * side;
                serverLevel.sendParticles(ParticleTypes.CRIT,
                        cx, this.getY() + 1.1D + this.random.nextDouble() * 1.2D, cz,
                        2, 0.15D, 0.2D, 0.15D, 0.0D);
            }
        }
    }

    /** 【对爪重击】第 16 tick 砸地结算：前方 2×2 造成 25 点伤害 + 震地粒子。 */
    private void performSlam() {
        this.performFrontalStrike(SLAM_HALF_WIDTH, SLAM_DEPTH, SLAM_DAMAGE, 0.7D);
        this.playSound(SoundEvents.GENERIC_EXPLODE, 0.9F, 0.75F + this.random.nextFloat() * 0.2F);
        if (this.level() instanceof net.minecraft.server.level.ServerLevel serverLevel) {
            Vec3 base = this.position();
            Vec3 facing = Vec3.directionFromRotation(0.0F, this.getYRot());
            Vec3 right = new Vec3(-facing.z, 0.0D, facing.x);
            // 尘云铺满前方 2×2：纵深 2 排 × 横向 3 列
            for (int d = 0; d < 2; d++) {
                for (int s = -1; s <= 1; s++) {
                    double dist = 0.7D + d * 1.05D;
                    double cx = base.x + facing.x * dist + right.x * s * 0.9D
                            + (this.random.nextDouble() - 0.5D) * 0.5D;
                    double cz = base.z + facing.z * dist + right.z * s * 0.9D
                            + (this.random.nextDouble() - 0.5D) * 0.5D;
                    serverLevel.sendParticles(ParticleTypes.CLOUD,
                            cx, base.y + 0.15D, cz, 6, 0.2D, 0.08D, 0.2D, 0.03D);
                    if (d == 0 && s == 0) {
                        serverLevel.sendParticles(ParticleTypes.EXPLOSION,
                                cx, base.y + 0.2D, cz, 1, 0.1D, 0.05D, 0.1D, 0.0D);
                    }
                }
            }
        }
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (!this.level().isClientSide) {
            if (this.feastCooldown > 0) {
                this.feastCooldown--;
            }
            if (this.slamCooldown > 0) {
                this.slamCooldown--;
            }
            int attackTicks = this.entityData.get(DATA_ATTACK_ANIM_TICKS);
            if (attackTicks > 0) {
                this.entityData.set(DATA_ATTACK_ANIM_TICKS, attackTicks - 1);
            }
            int feastTicks = this.entityData.get(DATA_FEAST_ANIM_TICKS);
            if (feastTicks > 0) {
                this.entityData.set(DATA_FEAST_ANIM_TICKS, feastTicks - 1);
            }
            int slamTicks = this.entityData.get(DATA_SLAM_ANIM_TICKS);
            if (slamTicks > 0) {
                this.entityData.set(DATA_SLAM_ANIM_TICKS, slamTicks - 1);
            }
        }
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_ATTACK_ANIM_TICKS, 0);
        this.entityData.define(DATA_FEAST_ANIM_TICKS, 0);
        this.entityData.define(DATA_SLAM_ANIM_TICKS, 0);
    }

    // -------------------- 技能 Goal --------------------

    /**
     * 【大吃一顿】Goal：目标进入 3.5 格内且有视线时释放，前 12 tick 仰身张口，
     * 第 12 tick 结算前方 2×1 范围 20 点撕咬伤害，随后咬住甩头撕扯再回位；冷却 60 tick。
     */
    private class FeastGoal extends Goal {
        private int runTicks;

        FeastGoal() {
            this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            LivingEntity target = FanjinghuwangEntity.this.getTarget();
            return FanjinghuwangEntity.this.feastCooldown <= 0
                    && !FanjinghuwangEntity.this.isCasting()
                    && target != null && target.isAlive()
                    && FanjinghuwangEntity.this.distanceToSqr(target) <= 12.25D
                    && FanjinghuwangEntity.this.hasLineOfSight(target);
        }

        @Override
        public boolean canContinueToUse() {
            LivingEntity target = FanjinghuwangEntity.this.getTarget();
            return this.runTicks < FEAST_ANIMATION_TICKS && target != null && target.isAlive();
        }

        @Override
        public boolean requiresUpdateEveryTick() {
            return true;
        }

        @Override
        public void start() {
            this.runTicks = 0;
            FanjinghuwangEntity.this.getNavigation().stop();
            FanjinghuwangEntity.this.entityData.set(DATA_FEAST_ANIM_TICKS, FEAST_ANIMATION_TICKS);
            FanjinghuwangEntity.this.playSound(SoundEvents.WOLF_GROWL, 1.2F, 0.7F);
        }

        @Override
        public void tick() {
            this.runTicks++;
            FanjinghuwangEntity.this.getNavigation().stop();
            LivingEntity target = FanjinghuwangEntity.this.getTarget();
            if (target != null) {
                FanjinghuwangEntity.this.getLookControl().setLookAt(target, 30.0F, 30.0F);
            }
            if (this.runTicks == FEAST_HIT_TICK) {
                FanjinghuwangEntity.this.performFeast();
            }
        }

        @Override
        public void stop() {
            this.runTicks = 0;
            FanjinghuwangEntity.this.feastCooldown = FEAST_COOLDOWN_TICKS;
        }
    }

    /**
     * 【对爪重击】Goal：目标进入 4 格内且有视线时释放，前 16 tick 举双爪过顶蓄力咆哮，
     * 第 16 tick 双爪砸地，结算前方 2×2 范围 25 点伤害，随后落地余震起身；冷却 120 tick。
     */
    private class SlamGoal extends Goal {
        private int runTicks;

        SlamGoal() {
            this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            LivingEntity target = FanjinghuwangEntity.this.getTarget();
            return FanjinghuwangEntity.this.slamCooldown <= 0
                    && !FanjinghuwangEntity.this.isCasting()
                    && target != null && target.isAlive()
                    && FanjinghuwangEntity.this.distanceToSqr(target) <= 16.0D
                    && FanjinghuwangEntity.this.hasLineOfSight(target);
        }

        @Override
        public boolean canContinueToUse() {
            LivingEntity target = FanjinghuwangEntity.this.getTarget();
            return this.runTicks < SLAM_ANIMATION_TICKS && target != null && target.isAlive();
        }

        @Override
        public boolean requiresUpdateEveryTick() {
            return true;
        }

        @Override
        public void start() {
            this.runTicks = 0;
            FanjinghuwangEntity.this.getNavigation().stop();
            FanjinghuwangEntity.this.entityData.set(DATA_SLAM_ANIM_TICKS, SLAM_ANIMATION_TICKS);
            FanjinghuwangEntity.this.playSound(SoundEvents.RAVAGER_ROAR, 1.2F, 0.7F);
        }

        @Override
        public void tick() {
            this.runTicks++;
            FanjinghuwangEntity.this.getNavigation().stop();
            LivingEntity target = FanjinghuwangEntity.this.getTarget();
            if (target != null) {
                FanjinghuwangEntity.this.getLookControl().setLookAt(target, 30.0F, 30.0F);
            }
            if (this.runTicks == SLAM_HIT_TICK) {
                FanjinghuwangEntity.this.performSlam();
            }
        }

        @Override
        public void stop() {
            this.runTicks = 0;
            FanjinghuwangEntity.this.slamCooldown = SLAM_COOLDOWN_TICKS;
        }
    }

    // -------------------- 音效：狼（低吼/哀鸣）+ 劫兽（重步） --------------------

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.WOLF_GROWL;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.WOLF_WHINE;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.WOLF_DEATH;
    }

    @Override
    protected void playStepSound(BlockPos pos, BlockState state) {
        this.playSound(SoundEvents.IRON_GOLEM_STEP, 0.15F, 0.8F);
    }
}
