package com.mimiworld.mimiworld.entity;

import com.mimiworld.mimiworld.effect.ModEffects;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
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
 * 复晶天青蟒（Compound Azure Python，注册 id = compound_azure_python）——低伏长身型敌对陆地 Boss。
 *
 * <p>模型/贴图/动画资源（均由 Blockbench 导出）：
 * geo/compound_azure_python.geo.json、textures/entity/compound_azure_python.png、
 * animations/compound_azure_python.animation.json
 * （animation.compound_azure_python.idle / walk / attack / bite / kiss / tail_sweep）。
 * 蛇身贴地、前半身昂首展兜（昂首高约 3.2 格），碰撞箱 1.4×3.2，火焰免疫。</p>
 *
 * <p>攻击方式：</p>
 * <ol>
 *   <li>普通撕咬：MeleeAttackGoal 驱动，10 点伤害，attack 动画 0.7 秒（14 tick）。</li>
 *   <li>【咬你命三千】（bite）：动画 1.6 秒（32 tick），第 13 tick（0.65 秒咬合帧）对
 *       4.5 格内最近的玩家造成 16 点撕咬伤害，并附加 30 秒中毒 I 与 30 秒蟒蛇缠绕 I；
 *       拥有蟒蛇缠绕的玩家对本 Boss 伤害 ×1.5（见 ModEffectEvents），冷却 180 秒（3600 tick）。</li>
 *   <li>【羽蛇神之吻】（kiss）：动画 2.2 秒（44 tick），第 11/20 tick（0.55/1.0 秒）
 *       对前方 2×2 范围猛烈撕咬两次，每咬 25 点（全吃两咬共 50 点），命中即附 60 秒中毒 II，
 *       冷却 15 秒（300 tick）。</li>
 *   <li>【扫尾】（tail_sweep）：动画 1.7 秒（34 tick），第 16 tick（0.8 秒）尾巴横扫至身后最远处，
 *       对身体周围 5.5 格、身后约 250° 扇形内的实体造成 70 点伤害与强击退，冷却 30 秒（600 tick）。</li>
 * </ol>
 *
 * <p>技能期间通过 Goal 互斥标志（MOVE+LOOK）锁定移动与普攻；动画优先级
 * bite / kiss / tail_sweep &gt; attack &gt; walk &gt; idle。
 * 320 血、移速 0.33、护甲 14、韧性 6、70% 击退抗性，音效以猫嘶（蛇信嘶声）+ 劫兽（撕咬/咆哮）为主。</p>
 */
public class CompoundAzurePythonEntity extends Monster implements GeoEntity {

    // -------------------- 同步动画状态（服务端递减，自动同步客户端） --------------------

    private static final EntityDataAccessor<Integer> DATA_ATTACK_ANIM_TICKS =
            SynchedEntityData.defineId(CompoundAzurePythonEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_BITE_ANIM_TICKS =
            SynchedEntityData.defineId(CompoundAzurePythonEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_KISS_ANIM_TICKS =
            SynchedEntityData.defineId(CompoundAzurePythonEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_SWEEP_ANIM_TICKS =
            SynchedEntityData.defineId(CompoundAzurePythonEntity.class, EntityDataSerializers.INT);

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    private static final RawAnimation ANIM_IDLE = RawAnimation.begin().thenLoop("animation.compound_azure_python.idle");
    private static final RawAnimation ANIM_WALK = RawAnimation.begin().thenLoop("animation.compound_azure_python.walk");
    private static final RawAnimation ANIM_ATTACK = RawAnimation.begin().thenPlay("animation.compound_azure_python.attack");
    private static final RawAnimation ANIM_BITE = RawAnimation.begin().thenPlay("animation.compound_azure_python.bite");
    private static final RawAnimation ANIM_KISS = RawAnimation.begin().thenPlay("animation.compound_azure_python.kiss");
    private static final RawAnimation ANIM_SWEEP = RawAnimation.begin().thenPlay("animation.compound_azure_python.tail_sweep");

    /** attack 动画 0.7 秒 = 14 tick */
    private static final int ATTACK_ANIMATION_TICKS = 14;

    /** bite 动画 1.6 秒 = 32 tick；命中帧 0.65 秒 = 第 13 tick；冷却 180 秒 = 3600 tick */
    private static final int BITE_ANIMATION_TICKS = 32;
    private static final int BITE_HIT_TICK = 13;
    private static final int BITE_COOLDOWN_TICKS = 3600;
    private static final float BITE_DAMAGE = 16.0F;
    private static final double BITE_RANGE = 4.5D;
    private static final int BITE_POISON_DURATION = 600;
    private static final int BITE_CONSTRICTION_DURATION = 600;

    /** kiss 动画 2.2 秒 = 44 tick；两次命中 0.55/1.0 秒 = 第 11/20 tick；冷却 15 秒 = 300 tick */
    private static final int KISS_ANIMATION_TICKS = 44;
    private static final int KISS_HIT_TICK_1 = 11;
    private static final int KISS_HIT_TICK_2 = 20;
    private static final int KISS_COOLDOWN_TICKS = 300;
    /** 单次撕咬 25 点，两咬全吃共 50 点 */
    private static final float KISS_DAMAGE_PER_BITE = 25.0F;
    private static final int KISS_POISON_DURATION = 1200;
    /** 判定区半宽：左右各 1.3 格（总宽 2.6） */
    private static final double KISS_HALF_WIDTH = 1.3D;
    /** 判定区纵深：头前 4.4 格 + margin 0.6 = 恰好 5.0 格，与触发距离完全对齐 */
    private static final double KISS_DEPTH = 4.4D;

    /** sweep 动画 1.7 秒 = 34 tick；命中帧 0.8 秒 = 第 16 tick（尾巴横扫至身后最深处）；冷却 30 秒 = 600 tick */
    private static final int SWEEP_ANIMATION_TICKS = 34;
    private static final int SWEEP_HIT_TICK = 16;
    private static final int SWEEP_COOLDOWN_TICKS = 600;
    private static final float SWEEP_DAMAGE = 70.0F;
    /** 扫尾半径（格）与身后扇形余弦阈值（dot 小于 0.6 ≈ 身体两侧 53° 往后的约 250° 弧） */
    private static final double SWEEP_RADIUS = 5.5D;
    private static final double SWEEP_ARC_COS = 0.6D;

    /** 首次技能前的准备时间，避免出生瞬间立刻出招 */
    private static final int INITIAL_BITE_DELAY = 80;
    private static final int INITIAL_KISS_DELAY = 140;
    private static final int INITIAL_SWEEP_DELAY = 200;

    // 技能冷却（仅服务端计时）
    private int biteCooldown = INITIAL_BITE_DELAY;
    private int kissCooldown = INITIAL_KISS_DELAY;
    private int sweepCooldown = INITIAL_SWEEP_DELAY;

    public CompoundAzurePythonEntity(EntityType<? extends CompoundAzurePythonEntity> type, Level level) {
        super(type, level);
    }

    /**
     * 属性：320 血、移速 0.33、普攻 10、追击 40、护甲 14、护甲韧性 6、70% 击退抗性。
     */
    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 320.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.33D)
                .add(Attributes.ATTACK_DAMAGE, 10.0D)
                .add(Attributes.FOLLOW_RANGE, 40.0D)
                .add(Attributes.ARMOR, 14.0D)
                .add(Attributes.ARMOR_TOUGHNESS, 6.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.7D);
    }

    /**
     * AI：0 漂浮；1 咬你命三千（点名长 CD）；2 羽蛇神之吻（前方两连咬）；3 扫尾；4 普攻追击；
     * 6 避水游荡；7 看玩家；8 张望。四个攻击 Goal 共用 MOVE+LOOK 标志，天然互斥，
     * 技能释放期间移动与普攻都会被锁住。
     */
    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new BiteGoal());
        this.goalSelector.addGoal(2, new KissGoal());
        this.goalSelector.addGoal(3, new TailSweepGoal());
        this.goalSelector.addGoal(4, new MeleeAttackGoal(this, 1.0D, false));
        this.goalSelector.addGoal(6, new WaterAvoidingRandomStrollGoal(this, 0.85D));
        this.goalSelector.addGoal(7, new LookAtPlayerGoal(this, Player.class, 12.0F));
        this.goalSelector.addGoal(8, new RandomLookAroundGoal(this));

        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    // -------------------- GeckoLib 动画 --------------------

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "main", 5, this::handleAnimState));
    }

    private PlayState handleAnimState(AnimationState<CompoundAzurePythonEntity> state) {
        if (this.entityData.get(DATA_BITE_ANIM_TICKS) > 0) {
            state.setAndContinue(ANIM_BITE);
        } else if (this.entityData.get(DATA_KISS_ANIM_TICKS) > 0) {
            state.setAndContinue(ANIM_KISS);
        } else if (this.entityData.get(DATA_SWEEP_ANIM_TICKS) > 0) {
            state.setAndContinue(ANIM_SWEEP);
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

    /** 是否正在释放技能（咬你命三千 / 羽蛇神之吻 / 扫尾）。普攻动画不计入。 */
    private boolean isCasting() {
        return this.entityData.get(DATA_BITE_ANIM_TICKS) > 0
                || this.entityData.get(DATA_KISS_ANIM_TICKS) > 0
                || this.entityData.get(DATA_SWEEP_ANIM_TICKS) > 0;
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

    /** 把身体/头部朝向硬吸附到目标，返回吸附后的朝向角。 */
    private float faceTowards(LivingEntity target) {
        float yaw = (float) Math.toDegrees(Math.atan2(
                -(target.getX() - this.getX()),
                target.getZ() - this.getZ()));
        this.setYRot(yaw);
        this.yBodyRot = yaw;
        this.setYHeadRot(yaw);
        return yaw;
    }

    /**
     * 【咬你命三千】第 13 tick 结算：直接扑咬 4.5 格内最近的玩家（点名），
     * 16 点伤害 + 30 秒中毒 I + 30 秒蟒蛇缠绕 I。
     */
    private void performBite() {
        Player victim = this.level().getNearestPlayer(this, BITE_RANGE);
        if (victim == null || !victim.isAlive() || !this.hasLineOfSight(victim)) {
            return;
        }
        this.faceTowards(victim);
        victim.hurt(this.damageSources().mobAttack(this), BITE_DAMAGE);
        victim.addEffect(new MobEffectInstance(MobEffects.POISON, BITE_POISON_DURATION, 0));
        victim.addEffect(new MobEffectInstance(ModEffects.PYTHON_CONSTRICTION.get(),
                BITE_CONSTRICTION_DURATION, 0));
        double pushX = victim.getX() - this.getX();
        double pushZ = victim.getZ() - this.getZ();
        double pushLen = Math.sqrt(pushX * pushX + pushZ * pushZ);
        if (pushLen > 1.0E-4D) {
            victim.push(pushX / pushLen * 0.35D, 0.2D, pushZ / pushLen * 0.35D);
        }
        this.playSound(SoundEvents.RAVAGER_ATTACK, 1.0F, 0.7F + this.random.nextFloat() * 0.15F);
        this.spawnMouthCrit(16, 1.6D);
    }

    /**
     * 【羽蛇神之吻】单次撕咬结算：前方 2×2 矩形，25 点伤害并附 60 秒中毒 II。
     * 一次技能调用两次（第 11、20 tick），同一名玩家两咬全吃共受 50 点。
     */
    private void performKissBite() {
        LivingEntity lockedTarget = this.getTarget();
        float strikeYaw = this.getYRot();
        if (lockedTarget != null) {
            strikeYaw = this.faceTowards(lockedTarget);
        }

        Vec3 base = this.position();
        Vec3 facing = Vec3.directionFromRotation(0.0F, strikeYaw);
        Vec3 right = new Vec3(-facing.z, 0.0D, facing.x);
        final double margin = 0.6D;

        AABB searchArea = this.getBoundingBox().inflate(6.0D);
        List<LivingEntity> targets = this.level().getEntitiesOfClass(LivingEntity.class, searchArea,
                target -> target != this && target.isAlive() && !target.isAlliedTo(this));
        boolean anyHit = false;
        for (LivingEntity target : targets) {
            Vec3 center = target.getBoundingBox().getCenter();
            double relX = center.x - base.x;
            double relZ = center.z - base.z;
            double forward = relX * facing.x + relZ * facing.z;
            double side = relX * right.x + relZ * right.z;
            boolean horizontalHit = forward >= -margin && forward <= KISS_DEPTH + margin
                    && Math.abs(side) <= KISS_HALF_WIDTH + margin;
            boolean verticalHit = target.getBoundingBox().maxY >= base.y - 0.5D
                    && target.getBoundingBox().minY <= base.y + 3.0D;
            if (horizontalHit && verticalHit) {
                target.hurt(this.damageSources().mobAttack(this), KISS_DAMAGE_PER_BITE);
                target.addEffect(new MobEffectInstance(MobEffects.POISON, KISS_POISON_DURATION, 1));
                double pushX = target.getX() - this.getX();
                double pushZ = target.getZ() - this.getZ();
                double pushLen = Math.sqrt(pushX * pushX + pushZ * pushZ);
                if (pushLen > 1.0E-4D) {
                    target.push(pushX / pushLen * 0.45D, 0.25D, pushZ / pushLen * 0.45D);
                }
                anyHit = true;
            }
        }
        this.playSound(SoundEvents.RAVAGER_ATTACK, 1.05F, 0.6F + this.random.nextFloat() * 0.15F);
        this.spawnMouthCrit(18, 1.8D);
        if (anyHit) {
            this.playSound(SoundEvents.EVOKER_FANGS_ATTACK, 0.9F, 0.8F);
        }
    }

    /**
     * 【扫尾】第 12 tick 结算：原地横扫，身体周围 5.5 格、身后约 250° 扇形内的活物
     * 受到 70 点伤害与强击退；正前方锥形留给头部方向，不参与判定。
     */
    private void performTailSweep() {
        float sweepYaw = this.getYRot();
        LivingEntity lockedTarget = this.getTarget();
        if (lockedTarget != null) {
            sweepYaw = this.faceTowards(lockedTarget);
        }

        Vec3 base = this.position();
        Vec3 facing = Vec3.directionFromRotation(0.0F, sweepYaw);

        AABB searchArea = this.getBoundingBox().inflate(SWEEP_RADIUS + 1.0D);
        List<LivingEntity> targets = this.level().getEntitiesOfClass(LivingEntity.class, searchArea,
                target -> target != this && target.isAlive() && !target.isAlliedTo(this));
        boolean anyHit = false;
        for (LivingEntity target : targets) {
            Vec3 center = target.getBoundingBox().getCenter();
            double relX = center.x - base.x;
            double relZ = center.z - base.z;
            double dist = Math.sqrt(relX * relX + relZ * relZ);
            if (dist > SWEEP_RADIUS || dist < 1.0E-4D) {
                continue;
            }
            double dot = (relX / dist) * facing.x + (relZ / dist) * facing.z;
            // dot < 0.6：目标相对朝向角大于约 53°（两侧及身后），即身后约 250° 扫尾弧
            if (dot >= SWEEP_ARC_COS) {
                continue;
            }
            if (target.getBoundingBox().maxY < base.y - 0.5D
                    || target.getBoundingBox().minY > base.y + 3.0D) {
                continue;
            }
            target.hurt(this.damageSources().mobAttack(this), SWEEP_DAMAGE);
            target.push(relX / dist * 1.1D, 0.35D, relZ / dist * 1.1D);
            anyHit = true;
        }
        this.playSound(SoundEvents.PLAYER_ATTACK_SWEEP, 1.2F, 0.75F);
        if (anyHit) {
            this.playSound(SoundEvents.EVOKER_FANGS_ATTACK, 1.0F, 0.65F);
        }
        this.spawnSweepParticles(sweepYaw);
    }

    /** 吻部前暴击粒子（咬击）。 */
    private void spawnMouthCrit(int count, double distance) {
        if (!(this.level() instanceof net.minecraft.server.level.ServerLevel serverLevel)) {
            return;
        }
        Vec3 facing = Vec3.directionFromRotation(0.0F, this.getYRot());
        Vec3 mouth = this.getEyePosition().add(facing.scale(distance));
        for (int i = 0; i < count; i++) {
            serverLevel.sendParticles(ParticleTypes.CRIT,
                    mouth.x + (this.random.nextDouble() - 0.5) * 0.9D,
                    mouth.y + (this.random.nextDouble() - 0.5) * 0.9D,
                    mouth.z + (this.random.nextDouble() - 0.5) * 0.9D,
                    2, 0.15D, 0.15D, 0.15D, 0.0D);
        }
    }

    /** 扫尾弧光 + 地面尘云粒子。 */
    private void spawnSweepParticles(float yaw) {
        if (!(this.level() instanceof net.minecraft.server.level.ServerLevel serverLevel)) {
            return;
        }
        Vec3 facing = Vec3.directionFromRotation(0.0F, yaw);
        Vec3 right = new Vec3(-facing.z, 0.0D, facing.x);
        // 沿身后 250° 弧、半径 3.6 格撒扫击粒子与尘云
        for (int deg = 55; deg <= 305; deg += 25) {
            double rad = Math.toRadians(deg);
            double ox = Math.cos(rad) * facing.x - Math.sin(rad) * right.x;
            double oz = Math.cos(rad) * facing.z - Math.sin(rad) * right.z;
            double cx = this.getX() + ox * 3.6D;
            double cz = this.getZ() + oz * 3.6D;
            serverLevel.sendParticles(ParticleTypes.SWEEP_ATTACK, cx, this.getY() + 0.8D, cz,
                    1, 0.3D, 0.3D, 0.3D, 0.0D);
            serverLevel.sendParticles(ParticleTypes.CLOUD, cx, this.getY() + 0.2D, cz,
                    5, 0.25D, 0.08D, 0.25D, 0.02D);
        }
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (!this.level().isClientSide) {
            if (this.biteCooldown > 0) {
                this.biteCooldown--;
            }
            if (this.kissCooldown > 0) {
                this.kissCooldown--;
            }
            if (this.sweepCooldown > 0) {
                this.sweepCooldown--;
            }
            this.tickAnim(DATA_ATTACK_ANIM_TICKS);
            this.tickAnim(DATA_BITE_ANIM_TICKS);
            this.tickAnim(DATA_KISS_ANIM_TICKS);
            this.tickAnim(DATA_SWEEP_ANIM_TICKS);
        }
    }

    private void tickAnim(EntityDataAccessor<Integer> data) {
        int ticks = this.entityData.get(data);
        if (ticks > 0) {
            this.entityData.set(data, ticks - 1);
        }
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_ATTACK_ANIM_TICKS, 0);
        this.entityData.define(DATA_BITE_ANIM_TICKS, 0);
        this.entityData.define(DATA_KISS_ANIM_TICKS, 0);
        this.entityData.define(DATA_SWEEP_ANIM_TICKS, 0);
    }

    // -------------------- 技能 Goal --------------------

    /**
     * 【咬你命三千】Goal：4.5 格内存在最近玩家且有视线时释放，前 13 tick 昂首展兜蓄力，
     * 第 13 tick 扑咬点名结算，随后甩头回位；冷却 3600 tick（180 秒）。
     */
    private class BiteGoal extends Goal {
        private int runTicks;

        BiteGoal() {
            this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            Player nearest = CompoundAzurePythonEntity.this.level()
                    .getNearestPlayer(CompoundAzurePythonEntity.this, BITE_RANGE);
            return CompoundAzurePythonEntity.this.biteCooldown <= 0
                    && !CompoundAzurePythonEntity.this.isCasting()
                    && nearest != null
                    && CompoundAzurePythonEntity.this.hasLineOfSight(nearest);
        }

        @Override
        public boolean canContinueToUse() {
            // 点名技能：只要动画未播完就持续，最近玩家在 tick() 中每 tick 重新选取
            return this.runTicks < BITE_ANIMATION_TICKS;
        }

        @Override
        public boolean requiresUpdateEveryTick() {
            return true;
        }

        @Override
        public void start() {
            this.runTicks = 0;
            CompoundAzurePythonEntity.this.getNavigation().stop();
            CompoundAzurePythonEntity.this.entityData.set(DATA_BITE_ANIM_TICKS, BITE_ANIMATION_TICKS);
            CompoundAzurePythonEntity.this.playSound(SoundEvents.CAT_HISS, 1.3F, 0.55F);
        }

        @Override
        public void tick() {
            this.runTicks++;
            CompoundAzurePythonEntity.this.getNavigation().stop();
            Player nearest = CompoundAzurePythonEntity.this.level()
                    .getNearestPlayer(CompoundAzurePythonEntity.this, BITE_RANGE + 1.5D);
            if (nearest != null) {
                CompoundAzurePythonEntity.this.getLookControl().setLookAt(nearest, 30.0F, 30.0F);
            }
            if (this.runTicks == BITE_HIT_TICK) {
                CompoundAzurePythonEntity.this.performBite();
            }
        }

        @Override
        public void stop() {
            this.runTicks = 0;
            CompoundAzurePythonEntity.this.biteCooldown = BITE_COOLDOWN_TICKS;
        }
    }

    /**
     * 【羽蛇神之吻】Goal：目标进入 5 格内且有视线时释放，前 11 tick 缩颈张口，
     * 第 11 与第 20 tick 各对前方 2×2 猛烈撕咬一次，随后甩头回位；冷却 300 tick（15 秒）。
     */
    private class KissGoal extends Goal {
        private int runTicks;

        KissGoal() {
            this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            LivingEntity target = CompoundAzurePythonEntity.this.getTarget();
            return CompoundAzurePythonEntity.this.kissCooldown <= 0
                    && !CompoundAzurePythonEntity.this.isCasting()
                    && target != null && target.isAlive()
                    && CompoundAzurePythonEntity.this.distanceToSqr(target) <= 25.0D
                    && CompoundAzurePythonEntity.this.hasLineOfSight(target);
        }

        @Override
        public boolean canContinueToUse() {
            return this.runTicks < KISS_ANIMATION_TICKS
                    && CompoundAzurePythonEntity.this.getTarget() != null
                    && CompoundAzurePythonEntity.this.getTarget().isAlive();
        }

        @Override
        public boolean requiresUpdateEveryTick() {
            return true;
        }

        @Override
        public void start() {
            this.runTicks = 0;
            CompoundAzurePythonEntity.this.getNavigation().stop();
            CompoundAzurePythonEntity.this.entityData.set(DATA_KISS_ANIM_TICKS, KISS_ANIMATION_TICKS);
            CompoundAzurePythonEntity.this.playSound(SoundEvents.RAVAGER_ROAR, 1.1F, 0.85F);
        }

        @Override
        public void tick() {
            this.runTicks++;
            CompoundAzurePythonEntity.this.getNavigation().stop();
            LivingEntity target = CompoundAzurePythonEntity.this.getTarget();
            if (target != null) {
                CompoundAzurePythonEntity.this.getLookControl().setLookAt(target, 30.0F, 30.0F);
            }
            if (this.runTicks == KISS_HIT_TICK_1 || this.runTicks == KISS_HIT_TICK_2) {
                CompoundAzurePythonEntity.this.performKissBite();
            }
        }

        @Override
        public void stop() {
            this.runTicks = 0;
            CompoundAzurePythonEntity.this.kissCooldown = KISS_COOLDOWN_TICKS;
        }
    }

    /**
     * 【扫尾】Goal：目标进入 6 格内时原地拧身（无需视线），前 12 tick 尾部向一侧盘起，
     * 第 12 tick 横扫结算身后 250°、半径 5.5 格范围 70 点伤害，随后余摆回位；冷却 600 tick（30 秒）。
     */
    private class TailSweepGoal extends Goal {
        private int runTicks;

        TailSweepGoal() {
            this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            LivingEntity target = CompoundAzurePythonEntity.this.getTarget();
            return CompoundAzurePythonEntity.this.sweepCooldown <= 0
                    && !CompoundAzurePythonEntity.this.isCasting()
                    && target != null && target.isAlive()
                    && CompoundAzurePythonEntity.this.distanceToSqr(target) <= 36.0D;
        }

        @Override
        public boolean canContinueToUse() {
            return this.runTicks < SWEEP_ANIMATION_TICKS
                    && CompoundAzurePythonEntity.this.getTarget() != null
                    && CompoundAzurePythonEntity.this.getTarget().isAlive();
        }

        @Override
        public boolean requiresUpdateEveryTick() {
            return true;
        }

        @Override
        public void start() {
            this.runTicks = 0;
            CompoundAzurePythonEntity.this.getNavigation().stop();
            CompoundAzurePythonEntity.this.entityData.set(DATA_SWEEP_ANIM_TICKS, SWEEP_ANIMATION_TICKS);
            CompoundAzurePythonEntity.this.playSound(SoundEvents.CAT_HISS, 1.2F, 0.5F);
        }

        @Override
        public void tick() {
            this.runTicks++;
            CompoundAzurePythonEntity.this.getNavigation().stop();
            LivingEntity target = CompoundAzurePythonEntity.this.getTarget();
            if (target != null) {
                CompoundAzurePythonEntity.this.getLookControl().setLookAt(target, 20.0F, 20.0F);
            }
            if (this.runTicks == SWEEP_HIT_TICK) {
                CompoundAzurePythonEntity.this.performTailSweep();
            }
        }

        @Override
        public void stop() {
            this.runTicks = 0;
            CompoundAzurePythonEntity.this.sweepCooldown = SWEEP_COOLDOWN_TICKS;
        }
    }

    // -------------------- 音效：猫嘶（蛇信嘶声）+ 劫兽（重扑/撕咬） --------------------

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.CAT_HISS;
    }

    @Override
    protected float getSoundVolume() {
        return 0.9F;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.SLIME_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.WOLF_DEATH;
    }

    @Override
    protected void playStepSound(BlockPos pos, BlockState state) {
        this.playSound(SoundEvents.IRON_GOLEM_STEP, 0.12F, 0.7F);
    }
}
