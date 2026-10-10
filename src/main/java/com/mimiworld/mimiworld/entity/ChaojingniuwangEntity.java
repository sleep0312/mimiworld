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
import net.minecraft.world.entity.MoverType;
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
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * 超晶牛王（Super Crystal Cow King，注册 id = super_crystal_cow_king）——壮硕大体型持斧陆地 Boss。
 *
 * <p>模型/贴图/动画资源（均由 Blockbench 导出）：
 * geo/super_crystal_cow_king.geo.json、textures/entity/super_crystal_cow_king.png、
 * animations/super_crystal_cow_king.animation.json
 * （animation.super_crystal_cow_king.idle / walk / attack / axe_combo / charge_start / charge）。
 * 模型直立约 41px（角尖）≈2.6 格、肩甲宽约 24px，碰撞箱 1.5×2.6，火焰免疫。
 * 巨斧以 45° 斜擎在右臂前上方。</p>
 *
 * <p>三种攻击方式：</p>
 * <ol>
 *   <li>普通近战：MeleeAttackGoal 驱动，造成 12 点伤害，attack 动画 1.0 秒（20 tick）。</li>
 *   <li>【猛牛冲撞】（charge_start + charge）：先低头蓄力 0.6 秒（12 tick），
 *       随后沿锁定方向强制直线冲撞 <b>10 格</b>才会停下（撞墙卡死除外），
 *       冲撞速度约 0.95 格/tick，沿途被撞实体各造成 20 点伤害与强击退，冷却 30 秒（600 tick）。</li>
 *   <li>【大斧一挥】（axe_combo）：动画 2.7 秒（54 tick），左右挥动三次巨斧，
 *       第 0.4/1.15/2.15 秒（第 8/23/43 tick）依次造成 10/20/30 点伤害，
 *       前两扫覆盖前方 2.2×2.2 横扫区，终结重劈覆盖前方 1.4×3.2 纵深区，冷却 45 秒（900 tick）。</li>
 * </ol>
 *
 * <p>技能期间通过 Goal 互斥标志（MOVE+LOOK）锁定移动与普攻；动画优先级
 * 冲撞蓄力/冲撞 &gt; 三段斧 &gt; 普攻 &gt; 行走 &gt; 待机。300 血、移速 0.3、护甲 14、
 * 韧性 8、80% 击退抗性，音效沿用原版劫兽（咆哮/撞击/受伤/死亡）与铁傀儡（重步）。</p>
 */
public class ChaojingniuwangEntity extends Monster implements GeoEntity {

    // -------------------- 同步动画状态（服务端递减，自动同步客户端） --------------------

    private static final EntityDataAccessor<Integer> DATA_ATTACK_ANIM_TICKS =
            SynchedEntityData.defineId(ChaojingniuwangEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_COMBO_ANIM_TICKS =
            SynchedEntityData.defineId(ChaojingniuwangEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_CHARGE_WINDUP_TICKS =
            SynchedEntityData.defineId(ChaojingniuwangEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_CHARGE_RUN_TICKS =
            SynchedEntityData.defineId(ChaojingniuwangEntity.class, EntityDataSerializers.INT);

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    private static final RawAnimation ANIM_IDLE =
            RawAnimation.begin().thenLoop("animation.super_crystal_cow_king.idle");
    private static final RawAnimation ANIM_WALK =
            RawAnimation.begin().thenLoop("animation.super_crystal_cow_king.walk");
    private static final RawAnimation ANIM_ATTACK =
            RawAnimation.begin().thenPlay("animation.super_crystal_cow_king.attack");
    private static final RawAnimation ANIM_COMBO =
            RawAnimation.begin().thenPlay("animation.super_crystal_cow_king.axe_combo");
    private static final RawAnimation ANIM_CHARGE_START =
            RawAnimation.begin().thenPlay("animation.super_crystal_cow_king.charge_start");
    private static final RawAnimation ANIM_CHARGE_RUN =
            RawAnimation.begin().thenLoop("animation.super_crystal_cow_king.charge");

    /** 普攻 attack 动画 1.0 秒 = 20 tick */
    private static final int ATTACK_ANIMATION_TICKS = 20;

    /** 大斧一挥 axe_combo 动画 2.7 秒 = 54 tick */
    private static final int COMBO_ANIMATION_TICKS = 54;
    /** 三次挥斧伤害结算时刻：动画 0.4/1.15/2.15 秒 = 第 8/23/43 tick */
    private static final int[] COMBO_HIT_TICKS = {8, 23, 43};
    /** 三次挥斧伤害：横扫 10、反扫 20、终结重劈 30 */
    private static final float[] COMBO_DAMAGE = {10.0F, 20.0F, 30.0F};
    /** 大斧一挥冷却：45 秒 = 900 tick */
    private static final int COMBO_COOLDOWN_TICKS = 900;
    /** 前两扫判定区：左右半宽 2.2、纵深 2.2（前方 4.4×2.2 横扫区） */
    private static final double SWEEP_HALF_WIDTH = 2.2D;
    private static final double SWEEP_DEPTH = 2.2D;
    /** 终结重劈判定区：左右半宽 1.4、纵深 3.2（前方 2.8×3.2 纵深区） */
    private static final double CHOP_HALF_WIDTH = 1.4D;
    private static final double CHOP_DEPTH = 3.2D;

    /** 猛牛冲撞蓄力 charge_start 动画 0.6 秒 = 12 tick */
    private static final int CHARGE_WINDUP_TICKS = 12;
    /** 冲撞阶段安全上限（≈4 秒），正常冲满 10 格只需约 11 tick */
    private static final int CHARGE_RUN_SAFETY_TICKS = 80;
    /** 冲撞必须移动的总距离：10 格 */
    private static final double CHARGE_DISTANCE = 10.0D;
    /** 冲撞速度：约 0.95 格/tick（≈19 格/秒，远快于常规移动） */
    private static final double CHARGE_SPEED = 0.95D;
    /** 连续 8 tick 几乎没有位移视为撞墙卡死，强制结束（避免永久锁死） */
    private static final int CHARGE_STUCK_LIMIT = 8;
    /** 冲撞伤害 */
    private static final float CHARGE_DAMAGE = 20.0F;
    /** 猛牛冲撞冷却：30 秒 = 600 tick */
    private static final int CHARGE_COOLDOWN_TICKS = 600;
    /** 冲撞释放距离：3~16 格（近身交给普攻/三段斧） */
    private static final double CHARGE_MIN_DISTANCE_SQR = 9.0D;
    private static final double CHARGE_MAX_DISTANCE_SQR = 256.0D;

    /** 首次技能前的准备时间，避免出生瞬间立刻出招 */
    private static final int INITIAL_COMBO_DELAY = 80;
    private static final int INITIAL_CHARGE_DELAY = 60;

    // 技能冷却（仅服务端计时）
    private int comboCooldown = INITIAL_COMBO_DELAY;
    private int chargeCooldown = INITIAL_CHARGE_DELAY;

    public ChaojingniuwangEntity(EntityType<? extends ChaojingniuwangEntity> type, Level level) {
        super(type, level);
    }

    /**
     * 属性：300 血、移速 0.3、普攻 12、追击 40、护甲 14、护甲韧性 8、80% 击退抗性。
     */
    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 300.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.30D)
                .add(Attributes.ATTACK_DAMAGE, 12.0D)
                .add(Attributes.FOLLOW_RANGE, 40.0D)
                .add(Attributes.ARMOR, 14.0D)
                .add(Attributes.ARMOR_TOUGHNESS, 8.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.8D);
    }

    /**
     * AI：0 漂浮；1 猛牛冲撞（中远距离起手）；2 大斧一挥（近身三段）；3 普攻追击；
     * 5 避水游荡；6 看玩家；7 张望。三个攻击 Goal 共用 MOVE+LOOK 标志，天然互斥，
     * 技能释放期间移动与普攻都会被锁住。
     */
    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new ChargeGoal());
        this.goalSelector.addGoal(2, new AxeComboGoal());
        this.goalSelector.addGoal(3, new MeleeAttackGoal(this, 1.0D, false));
        this.goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 0.8D));
        this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 12.0F));
        this.goalSelector.addGoal(7, new RandomLookAroundGoal(this));

        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    // -------------------- GeckoLib 动画 --------------------

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "main", 5, this::handleAnimState));
    }

    private PlayState handleAnimState(AnimationState<ChaojingniuwangEntity> state) {
        if (this.entityData.get(DATA_CHARGE_WINDUP_TICKS) > 0) {
            state.setAndContinue(ANIM_CHARGE_START);
        } else if (this.entityData.get(DATA_CHARGE_RUN_TICKS) > 0) {
            state.setAndContinue(ANIM_CHARGE_RUN);
        } else if (this.entityData.get(DATA_COMBO_ANIM_TICKS) > 0) {
            state.setAndContinue(ANIM_COMBO);
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

    /** 是否正在释放技能（冲撞蓄力/冲撞中/三段斧）。普攻动画不计入。 */
    private boolean isCasting() {
        return this.entityData.get(DATA_CHARGE_WINDUP_TICKS) > 0
                || this.entityData.get(DATA_CHARGE_RUN_TICKS) > 0
                || this.entityData.get(DATA_COMBO_ANIM_TICKS) > 0;
    }

    // -------------------- 攻击逻辑 --------------------

    /** 普攻命中时触发 attack 动画（服务端结算成功一次，数据自动同步） */
    @Override
    public boolean doHurtTarget(Entity target) {
        if (super.doHurtTarget(target)) {
            if (!this.isCasting()) {
                this.entityData.set(DATA_ATTACK_ANIM_TICKS, ATTACK_ANIMATION_TICKS);
            }
            return true;
        }
        return false;
    }

    /**
     * 前方矩形区域伤害结算（三段斧横扫/重劈共用）。
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

        AABB searchArea = this.getBoundingBox().inflate(6.0D);
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
                    && target.getBoundingBox().minY <= base.y + 3.2D;
            if (horizontalHit && verticalHit) {
                target.hurt(this.damageSources().mobAttack(this), damage);
                // 背离牛王的水平冲击，强化斧刃命中感
                double pushX = target.getX() - this.getX();
                double pushZ = target.getZ() - this.getZ();
                double pushLen = Math.sqrt(pushX * pushX + pushZ * pushZ);
                if (pushLen > 1.0E-4D) {
                    target.push(pushX / pushLen * push, 0.28D, pushZ / pushLen * push);
                }
            }
        }
    }

    /** 大斧一挥第 hitIndex 段（0/1/2）结算：横扫 10、反扫 20 走宽区，终结重劈 30 走纵深区。 */
    private void performComboHit(int hitIndex) {
        if (hitIndex == 2) {
            this.performFrontalStrike(CHOP_HALF_WIDTH, CHOP_DEPTH, COMBO_DAMAGE[2], 1.0D);
            this.playSound(SoundEvents.GENERIC_EXPLODE, 0.85F, 0.9F + this.random.nextFloat() * 0.15F);
        } else {
            this.performFrontalStrike(SWEEP_HALF_WIDTH, SWEEP_DEPTH,
                    COMBO_DAMAGE[hitIndex], hitIndex == 0 ? 0.45D : 0.7D);
            this.playSound(SoundEvents.RAVAGER_ATTACK, 1.0F,
                    (hitIndex == 0 ? 1.0F : 0.82F) + this.random.nextFloat() * 0.15F);
        }
        if (this.level() instanceof net.minecraft.server.level.ServerLevel serverLevel) {
            Vec3 base = this.position();
            float yaw = this.getYRot();
            Vec3 facing = Vec3.directionFromRotation(0.0F, yaw);
            Vec3 right = new Vec3(-facing.z, 0.0D, facing.x);
            // 横扫/重劈白色弧光粒子，终结段范围更大
            int count = hitIndex == 2 ? 14 : 10;
            double arc = hitIndex == 2 ? CHOP_HALF_WIDTH : SWEEP_HALF_WIDTH;
            double depth = hitIndex == 2 ? CHOP_DEPTH : SWEEP_DEPTH;
            for (int i = 0; i < count; i++) {
                double dist = 0.8D + this.random.nextDouble() * (depth - 0.4D);
                double side = (this.random.nextDouble() - 0.5D) * 2.0D * arc;
                double cx = base.x + facing.x * dist + right.x * side;
                double cz = base.z + facing.z * dist + right.z * side;
                serverLevel.sendParticles(ParticleTypes.SWEEP_ATTACK,
                        cx, base.y + 1.0D + this.random.nextDouble() * 1.4D, cz,
                        1, 0.1D, 0.1D, 0.1D, 0.0D);
            }
        }
    }

    /**
     * 冲撞每 tick 的沿途伤害：碰撞箱外扩 0.6 格内的活体各受 20 点伤害（同一次冲撞只结算一次），
     * 并沿冲撞方向强击退。
     */
    private void performChargeHit(Set<UUID> victims) {
        AABB hitArea = this.getBoundingBox().inflate(0.6D, 0.4D, 0.6D);
        AABB searchArea = this.getBoundingBox().inflate(2.0D);
        List<LivingEntity> targets = this.level().getEntitiesOfClass(LivingEntity.class, searchArea,
                target -> target != this && target.isAlive() && !target.isAlliedTo(this));
        boolean hitAny = false;
        for (LivingEntity target : targets) {
            if (victims.contains(target.getUUID()) || !target.getBoundingBox().intersects(hitArea)) {
                continue;
            }
            victims.add(target.getUUID());
            target.hurt(this.damageSources().mobAttack(this), CHARGE_DAMAGE);
            Vec3 knock = Vec3.directionFromRotation(0.0F, this.chargeYaw);
            target.push(knock.x * 1.1D, 0.42D, knock.z * 1.1D);
            hitAny = true;
            if (this.level() instanceof net.minecraft.server.level.ServerLevel serverLevel) {
                serverLevel.sendParticles(ParticleTypes.CRIT,
                        target.getX(), target.getBoundingBox().getCenter().y, target.getZ(),
                        10, 0.25D, 0.3D, 0.25D, 0.0D);
            }
        }
        if (hitAny) {
            this.playSound(SoundEvents.RAVAGER_ATTACK, 1.0F, 0.65F + this.random.nextFloat() * 0.15F);
        }
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (!this.level().isClientSide) {
            if (this.comboCooldown > 0) {
                this.comboCooldown--;
            }
            if (this.chargeCooldown > 0) {
                this.chargeCooldown--;
            }
            int attackTicks = this.entityData.get(DATA_ATTACK_ANIM_TICKS);
            if (attackTicks > 0) {
                this.entityData.set(DATA_ATTACK_ANIM_TICKS, attackTicks - 1);
            }
            int comboTicks = this.entityData.get(DATA_COMBO_ANIM_TICKS);
            if (comboTicks > 0) {
                this.entityData.set(DATA_COMBO_ANIM_TICKS, comboTicks - 1);
            }
            int windupTicks = this.entityData.get(DATA_CHARGE_WINDUP_TICKS);
            if (windupTicks > 0) {
                this.entityData.set(DATA_CHARGE_WINDUP_TICKS, windupTicks - 1);
            }
            int runTicks = this.entityData.get(DATA_CHARGE_RUN_TICKS);
            if (runTicks > 0) {
                this.entityData.set(DATA_CHARGE_RUN_TICKS, runTicks - 1);
            }
        }
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_ATTACK_ANIM_TICKS, 0);
        this.entityData.define(DATA_COMBO_ANIM_TICKS, 0);
        this.entityData.define(DATA_CHARGE_WINDUP_TICKS, 0);
        this.entityData.define(DATA_CHARGE_RUN_TICKS, 0);
    }

    // -------------------- 技能状态（仅服务端 Goal 使用） --------------------

    /** 本次冲撞锁定的朝向（度） */
    private float chargeYaw;

    // -------------------- 技能 Goal --------------------

    /**
     * 【猛牛冲撞】Goal：目标在 3~16 格内时有视线则释放。
     * 前 12 tick 低头蓄力（charge_start），随后沿锁定方向强制直线冲撞，
     * 累计移动满 10 格才结束（charge 循环）；沿途实体各结算一次 20 点伤害；
     * 连续 8 tick 无位移判定撞墙卡死，强制结束；冷却 600 tick。
     */
    private class ChargeGoal extends Goal {
        private int runTicks;
        private boolean charging;
        private boolean finished;
        private double traveled;
        private int stuckTicks;
        private final Set<UUID> victims = new HashSet<>();

        ChargeGoal() {
            this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            LivingEntity target = ChaojingniuwangEntity.this.getTarget();
            return ChaojingniuwangEntity.this.chargeCooldown <= 0
                    && !ChaojingniuwangEntity.this.isCasting()
                    && target != null && target.isAlive()
                    && ChaojingniuwangEntity.this.distanceToSqr(target) >= CHARGE_MIN_DISTANCE_SQR
                    && ChaojingniuwangEntity.this.distanceToSqr(target) <= CHARGE_MAX_DISTANCE_SQR
                    && ChaojingniuwangEntity.this.hasLineOfSight(target);
        }

        @Override
        public boolean canContinueToUse() {
            if (this.finished) {
                return false;
            }
            if (!this.charging) {
                LivingEntity target = ChaojingniuwangEntity.this.getTarget();
                return target != null && target.isAlive();
            }
            // 冲撞阶段：冲满 10 格才停；traveled/stuck/安全上限由 tick 判定
            return this.traveled < CHARGE_DISTANCE
                    && this.stuckTicks < CHARGE_STUCK_LIMIT
                    && this.runTicks - CHARGE_WINDUP_TICKS < CHARGE_RUN_SAFETY_TICKS;
        }

        @Override
        public boolean requiresUpdateEveryTick() {
            return true;
        }

        @Override
        public void start() {
            this.runTicks = 0;
            this.charging = false;
            this.finished = false;
            this.traveled = 0.0D;
            this.stuckTicks = 0;
            this.victims.clear();
            ChaojingniuwangEntity.this.getNavigation().stop();
            ChaojingniuwangEntity.this.entityData.set(DATA_CHARGE_WINDUP_TICKS, CHARGE_WINDUP_TICKS);
            ChaojingniuwangEntity.this.playSound(SoundEvents.RAVAGER_ROAR, 1.2F, 0.8F);
        }

        @Override
        public void tick() {
            this.runTicks++;
            ChaojingniuwangEntity.this.getNavigation().stop();

            if (!this.charging) {
                // —— 蓄力阶段：盯紧目标，原地低头 ——
                LivingEntity target = ChaojingniuwangEntity.this.getTarget();
                if (target != null) {
                    ChaojingniuwangEntity.this.getLookControl().setLookAt(target, 30.0F, 30.0F);
                }
                if (this.runTicks >= CHARGE_WINDUP_TICKS) {
                    // 起步瞬间硬锁朝向：取蓄力末刻身体朝向，之后冲撞绝不转弯
                    this.charging = true;
                    ChaojingniuwangEntity.this.chargeYaw = ChaojingniuwangEntity.this.getYRot();
                    LivingEntity lookTarget = ChaojingniuwangEntity.this.getTarget();
                    if (lookTarget != null) {
                        ChaojingniuwangEntity.this.chargeYaw = (float) Math.toDegrees(Math.atan2(
                                -(lookTarget.getX() - ChaojingniuwangEntity.this.getX()),
                                lookTarget.getZ() - ChaojingniuwangEntity.this.getZ()));
                    }
                    ChaojingniuwangEntity.this.setYRot(ChaojingniuwangEntity.this.chargeYaw);
                    ChaojingniuwangEntity.this.yBodyRot = ChaojingniuwangEntity.this.chargeYaw;
                    ChaojingniuwangEntity.this.setYHeadRot(ChaojingniuwangEntity.this.chargeYaw);
                    ChaojingniuwangEntity.this.entityData.set(DATA_CHARGE_WINDUP_TICKS, 0);
                    ChaojingniuwangEntity.this.entityData.set(DATA_CHARGE_RUN_TICKS, CHARGE_RUN_SAFETY_TICKS);
                    ChaojingniuwangEntity.this.playSound(SoundEvents.RAVAGER_ATTACK, 1.0F, 0.7F);
                }
                return;
            }

            // —— 冲撞阶段：锁定朝向，强制直线位移 ——
            ChaojingniuwangEntity.this.setYRot(ChaojingniuwangEntity.this.chargeYaw);
            ChaojingniuwangEntity.this.yBodyRot = ChaojingniuwangEntity.this.chargeYaw;
            ChaojingniuwangEntity.this.setYHeadRot(ChaojingniuwangEntity.this.chargeYaw);

            Vec3 before = ChaojingniuwangEntity.this.position();
            Vec3 facing = Vec3.directionFromRotation(0.0F, ChaojingniuwangEntity.this.chargeYaw);
            double verticalSpeed = ChaojingniuwangEntity.this.getDeltaMovement().y - 0.08D;
            if (ChaojingniuwangEntity.this.onGround() && verticalSpeed < 0.0D) {
                // 贴地疾跑：每 tick 不下沉，保留起跳/被击飞时的重力行为
                verticalSpeed = 0.0D;
            }
            Vec3 moveVec = new Vec3(facing.x * CHARGE_SPEED, verticalSpeed, facing.z * CHARGE_SPEED);
            ChaojingniuwangEntity.this.setDeltaMovement(moveVec);
            ChaojingniuwangEntity.this.move(MoverType.SELF, moveVec);

            Vec3 after = ChaojingniuwangEntity.this.position();
            double moved = Math.hypot(after.x - before.x, after.z - before.z);
            this.traveled += moved;
            if (moved < 0.06D) {
                this.stuckTicks++;
            } else {
                this.stuckTicks = 0;
            }

            ChaojingniuwangEntity.this.performChargeHit(this.victims);

            if (ChaojingniuwangEntity.this.level() instanceof net.minecraft.server.level.ServerLevel serverLevel) {
                // 蹄下扬尘
                serverLevel.sendParticles(ParticleTypes.CLOUD,
                        before.x - facing.x * 0.5D, before.y + 0.15D, before.z - facing.z * 0.5D,
                        5, 0.25D, 0.05D, 0.25D, 0.01D);
            }
            if (ChaojingniuwangEntity.this.random.nextInt(3) == 0) {
                ChaojingniuwangEntity.this.playSound(SoundEvents.RAVAGER_STEP, 0.7F, 0.85F);
            }

            if (this.traveled >= CHARGE_DISTANCE || this.stuckTicks >= CHARGE_STUCK_LIMIT
                    || this.runTicks - CHARGE_WINDUP_TICKS >= CHARGE_RUN_SAFETY_TICKS) {
                this.finished = true;
            }
        }

        @Override
        public void stop() {
            this.finished = true;
            this.charging = false;
            this.runTicks = 0;
            this.traveled = 0.0D;
            this.stuckTicks = 0;
            ChaojingniuwangEntity.this.entityData.set(DATA_CHARGE_WINDUP_TICKS, 0);
            ChaojingniuwangEntity.this.entityData.set(DATA_CHARGE_RUN_TICKS, 0);
            ChaojingniuwangEntity.this.chargeCooldown = CHARGE_COOLDOWN_TICKS;
        }
    }

    /**
     * 【大斧一挥】Goal：目标进入 4.5 格内且有视线时释放，54 tick 内左右挥斧三次，
     * 第 8/23/43 tick 分别结算 10/20/30 点伤害（横扫宽区两次 + 终结重劈纵深区）；
     * 释放期间锁住移动与朝向目标，结束后冷却 900 tick。
     */
    private class AxeComboGoal extends Goal {
        private int runTicks;

        AxeComboGoal() {
            this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            LivingEntity target = ChaojingniuwangEntity.this.getTarget();
            return ChaojingniuwangEntity.this.comboCooldown <= 0
                    && !ChaojingniuwangEntity.this.isCasting()
                    && target != null && target.isAlive()
                    && ChaojingniuwangEntity.this.distanceToSqr(target) <= 20.25D
                    && ChaojingniuwangEntity.this.hasLineOfSight(target);
        }

        @Override
        public boolean canContinueToUse() {
            LivingEntity target = ChaojingniuwangEntity.this.getTarget();
            return this.runTicks < COMBO_ANIMATION_TICKS && target != null && target.isAlive();
        }

        @Override
        public boolean requiresUpdateEveryTick() {
            return true;
        }

        @Override
        public void start() {
            this.runTicks = 0;
            ChaojingniuwangEntity.this.getNavigation().stop();
            ChaojingniuwangEntity.this.entityData.set(DATA_COMBO_ANIM_TICKS, COMBO_ANIMATION_TICKS);
            ChaojingniuwangEntity.this.playSound(SoundEvents.RAVAGER_ROAR, 0.9F, 1.05F);
        }

        @Override
        public void tick() {
            this.runTicks++;
            ChaojingniuwangEntity.this.getNavigation().stop();
            LivingEntity target = ChaojingniuwangEntity.this.getTarget();
            if (target != null) {
                ChaojingniuwangEntity.this.getLookControl().setLookAt(target, 30.0F, 30.0F);
            }
            for (int i = 0; i < COMBO_HIT_TICKS.length; i++) {
                if (this.runTicks == COMBO_HIT_TICKS[i]) {
                    ChaojingniuwangEntity.this.performComboHit(i);
                }
            }
        }

        @Override
        public void stop() {
            this.runTicks = 0;
            ChaojingniuwangEntity.this.entityData.set(DATA_COMBO_ANIM_TICKS, 0);
            ChaojingniuwangEntity.this.comboCooldown = COMBO_COOLDOWN_TICKS;
        }
    }

    // -------------------- 音效：劫兽（咆哮/受伤/死亡/重击）+ 铁傀儡（重步） --------------------

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.RAVAGER_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.RAVAGER_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.RAVAGER_DEATH;
    }

    @Override
    protected void playStepSound(BlockPos pos, BlockState state) {
        this.playSound(SoundEvents.RAVAGER_STEP, 0.35F, 0.85F);
    }
}
