package com.mimiworld.mimiworld.entity;

import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.FlyingMoveControl;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.pathfinder.BlockPathTypes;
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
 * 界天米（注册 id = jietian_rice）——空中独眼敌对生物。
 *
 * <p>GeckoLib 资源（Blockbench 导出）：
 * geo/jietianmi.geo.json（geometry.jietianmi，独眼米身+双翼+六根触须+可动眼睑）、
 * textures/entity/jietianmi.png（128×128）、animations/jietianmi.animation.json
 * （animation.jietianmi.idle / .walk / .attack）。</p>
 *
 * <p>行为对齐原版守卫者：常驻无重力悬浮飞行（{@link FlyingMoveControl} + {@link FlyingPathNavigation}）；
 * 发现玩家后凝视悬停蓄力，attack 动画第 13 tick（约 0.667 秒，动画发射帧）打出一道魔法光束，
 * 光束仅在目标存活、可见且在射程内时造成一次伤害，随后冷却再进入下一轮蓄力；
 * 无目标时在空中随机漂浮。攻击动画播放期间强制播单次 attack，飞行位移播 walk，悬停播 idle。
 * 200 心（沿用原占位数值）、光束攻击 200、追击 40 格。</p>
 */
public class JietianmiEntity extends Monster implements GeoEntity {

    /** 同步数据：攻击动画剩余 tick（服务端蓄力开始时置 45 并逐 tick 递减，自动同步到客户端）。 */
    private static final EntityDataAccessor<Integer> DATA_ATTACK_ANIM_TICKS =
            SynchedEntityData.defineId(JietianmiEntity.class, EntityDataSerializers.INT);

    /** 同步数据：光束目标实体 id（客户端据此从 ClientLevel 取目标并渲染光束）。 */
    private static final EntityDataAccessor<Integer> DATA_BEAM_TARGET_ID =
            SynchedEntityData.defineId(JietianmiEntity.class, EntityDataSerializers.INT);

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    // jietianmi.animation.json 中的三条动画
    private static final RawAnimation ANIM_IDLE = RawAnimation.begin().thenLoop("animation.jietianmi.idle");
    private static final RawAnimation ANIM_WALK = RawAnimation.begin().thenLoop("animation.jietianmi.walk");
    private static final RawAnimation ANIM_ATTACK = RawAnimation.begin().thenPlay("animation.jietianmi.attack");

    /** attack 动画时长 2.25 秒 = 45 tick */
    public static final int ATTACK_ANIMATION_TICKS = 45;

    /** 光束在动画中发射的时刻（动画 t=0.667s，约第 13 tick）：剩余 tick 数降到该值时结算伤害 */
    public static final int BEAM_FIRE_TICKS = ATTACK_ANIMATION_TICKS - 13;

    /** 光束最大射程（格），超出射程放弃蓄力、继续贴近 */
    public static final double BEAM_RANGE = 18.0D;

    /** 位移速度平方阈值：高于它视为飞行移动播 walk，否则悬停播 idle */
    private static final double MOVE_ANIM_SPEED_SQR = 0.0025D;

    public JietianmiEntity(EntityType<? extends JietianmiEntity> type, Level level) {
        super(type, level);
        // 常驻飞行：无重力 + 急停悬停式飞行控制器（同爆烈米灵）
        this.setNoGravity(true);
        this.moveControl = new FlyingMoveControl(this, 10, true);
        // 空中单位，避开熔岩/火焰方块与水面
        this.setPathfindingMalus(BlockPathTypes.WATER, -1.0F);
        this.setPathfindingMalus(BlockPathTypes.LAVA, -1.0F);
        this.setPathfindingMalus(BlockPathTypes.DAMAGE_FIRE, -1.0F);
        this.setPathfindingMalus(BlockPathTypes.DANGER_FIRE, -1.0F);
    }

    /** 属性：200 心 = 400 血（沿用原占位数值），光束攻击 200、追击 40，飞行速度系数 1.0。 */
    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 400.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.25D)
                .add(Attributes.FLYING_SPEED, 1.0D)
                .add(Attributes.ATTACK_DAMAGE, 200.0D)
                .add(Attributes.FOLLOW_RANGE, 40.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.5D);
    }

    /** 飞行生物使用空中直线寻路（节点允许悬空点位，同烈焰人/恶魂） */
    @Override
    protected PathNavigation createNavigation(Level level) {
        FlyingPathNavigation navigation = new FlyingPathNavigation(this, level);
        navigation.setCanOpenDoors(false);
        navigation.setCanFloat(true);
        navigation.setCanPassDoors(true);
        return navigation;
    }

    @Override
    protected void registerGoals() {
        // 2：守卫者式凝视蓄力光束；3：无目标空中漂浮；6/7：张望
        this.goalSelector.addGoal(2, new BeamAttackGoal());
        this.goalSelector.addGoal(3, new HoverWanderGoal());
        this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(7, new RandomLookAroundGoal(this));

        // 全天敌对：受击必反击 + 主动索敌玩家
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    // -------------------- GeckoLib 动画 --------------------

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "main", 5, this::handleAnimState));
    }

    /**
     * 攻击动画播放的 45 tick 内强制播单次 attack；否则按飞行速度切 walk / idle。
     * setAndContinue 保证同名动画不被逐帧重启，attack 播完自然切回。
     */
    private PlayState handleAnimState(AnimationState<JietianmiEntity> state) {
        if (this.entityData.get(DATA_ATTACK_ANIM_TICKS) > 0) {
            state.setAndContinue(ANIM_ATTACK);
            return PlayState.CONTINUE;
        }
        if (this.getDeltaMovement().lengthSqr() > MOVE_ANIM_SPEED_SQR) {
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

    // -------------------- 光束蓄力状态 --------------------

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_ATTACK_ANIM_TICKS, 0);
        this.entityData.define(DATA_BEAM_TARGET_ID, 0);
    }

    /** 是否正在凝视蓄力（光束可见） */
    public boolean isBeaming() {
        return this.entityData.get(DATA_ATTACK_ANIM_TICKS) > 0;
    }

    /** 蓄力剩余 tick（45 → 0） */
    public int getBeamTicks() {
        return this.entityData.get(DATA_ATTACK_ANIM_TICKS);
    }

    /** 光束目标实体 id（客户端渲染用，0 = 无目标） */
    public int getBeamTargetId() {
        return this.entityData.get(DATA_BEAM_TARGET_ID);
    }

    /** 蓄力进度 0 → 1（带帧间插值），供渲染端控制光束粗细/亮度。 */
    public float getBeamProgress(float partialTicks) {
        int ticks = this.entityData.get(DATA_ATTACK_ANIM_TICKS);
        if (ticks <= 0) {
            return 0.0F;
        }
        float remain = ticks - partialTicks;
        return Mth.clamp(1.0F - remain / (float) ATTACK_ANIMATION_TICKS, 0.0F, 1.0F);
    }

    /** 开始一轮凝视蓄力（仅服务端调用）。 */
    public void startBeamAttack(LivingEntity target) {
        this.entityData.set(DATA_ATTACK_ANIM_TICKS, ATTACK_ANIMATION_TICKS);
        this.entityData.set(DATA_BEAM_TARGET_ID, target.getId());
    }

    /** 目标死亡/消失时中断蓄力。 */
    public void abortBeamAttack() {
        this.entityData.set(DATA_ATTACK_ANIM_TICKS, 0);
        this.entityData.set(DATA_BEAM_TARGET_ID, 0);
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (!this.level().isClientSide) {
            int ticks = this.entityData.get(DATA_ATTACK_ANIM_TICKS);
            if (ticks > 0) {
                ticks--;
                this.entityData.set(DATA_ATTACK_ANIM_TICKS, ticks);
                // 对齐动画发射帧（第 13 tick，剩余 32 tick）：结算一次光束伤害
                if (ticks == BEAM_FIRE_TICKS) {
                    this.fireBeam();
                }
                if (ticks == 0) {
                    this.entityData.set(DATA_BEAM_TARGET_ID, 0);
                }
            }
        }
    }

    /** 光束命中结算：目标存活、仍在射程且可见时造成一次无视护甲的魔法伤害（同守卫者激光）。 */
    private void fireBeam() {
        Entity target = this.level().getEntity(this.entityData.get(DATA_BEAM_TARGET_ID));
        if (target instanceof LivingEntity living && living.isAlive()
                && this.distanceToSqr(living) <= BEAM_RANGE * BEAM_RANGE
                && this.getSensing().hasLineOfSight(living)) {
            float damage = (float) this.getAttributeValue(Attributes.ATTACK_DAMAGE);
            living.hurt(this.damageSources().indirectMagic(this, this), damage);
            // 沿视线给一个很小的击退，模拟光束冲击
            Vec3 knock = this.getViewVector(1.0F);
            living.push(knock.x * 0.4D, 0.1D, knock.z * 0.4D);
        }
        // 守卫者激光命中音效（双方/附近玩家都能听到）
        this.playSound(SoundEvents.GUARDIAN_ATTACK, 1.6F, 0.85F);
    }

    // -------------------- 杂项：摔落/音效 --------------------

    @Override
    public boolean causeFallDamage(float distance, float multiplier, DamageSource source) {
        // 飞行生物，永不承受摔落伤害
        return false;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.ELDER_GUARDIAN_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.ELDER_GUARDIAN_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.ELDER_GUARDIAN_DEATH;
    }

    @Override
    public int getAmbientSoundInterval() {
        return 200;
    }

    // -------------------- 守卫者式凝视光束 AI --------------------

    /**
     * 攻击节奏：
     * 无蓄力时，目标在 {@link #BEAM_RANGE} 内且可见 → 悬停凝视并短暂稳定后开始 45 tick 蓄力；
     * 目标过近则后撤、过远或丢失视野则贴近；蓄力期间几乎不位移并始终面朝目标，
     * 伤害由 {@link JietianmiEntity#fireBeam()} 在动画发射帧统一结算；
     * 蓄力结束后冷却 30~60 tick。目标死亡/消失立即中断。
     */
    private class BeamAttackGoal extends Goal {
        private int settleTime;
        private int cooldown;

        BeamAttackGoal() {
            this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            LivingEntity target = JietianmiEntity.this.getTarget();
            return target != null && target.isAlive();
        }

        @Override
        public boolean canContinueToUse() {
            LivingEntity target = JietianmiEntity.this.getTarget();
            return (target != null && target.isAlive()) || JietianmiEntity.this.isBeaming();
        }

        @Override
        public boolean requiresUpdateEveryTick() {
            return true;
        }

        @Override
        public void start() {
            this.settleTime = 0;
            this.cooldown = 0;
        }

        @Override
        public void tick() {
            JietianmiEntity mob = JietianmiEntity.this;
            LivingEntity target = mob.getTarget();

            // 蓄力中：凝视锁定、几乎悬停；目标失效则立刻中断
            if (mob.isBeaming()) {
                if (target == null || !target.isAlive()) {
                    mob.abortBeamAttack();
                    this.cooldown = 20;
                    return;
                }
                mob.getLookControl().setLookAt(target, 30.0F, 30.0F);
                this.brake();
                return;
            }

            if (this.cooldown > 0) {
                this.cooldown--;
            }
            if (target == null) {
                return;
            }

            mob.getLookControl().setLookAt(
                    target.getX(), target.getY(0.5D), target.getZ(), 30.0F, 30.0F);

            double distSqr = mob.distanceToSqr(target);
            boolean lineOfSight = mob.getSensing().hasLineOfSight(target);

            if (!lineOfSight || distSqr > BEAM_RANGE * BEAM_RANGE) {
                // 太远/看不见：贴近目标，重新稳定
                mob.getMoveControl().setWantedPosition(
                        target.getX(), target.getY(0.5D), target.getZ(), 1.0D);
                this.settleTime = 10;
            } else if (distSqr < 5.0D * 5.0D) {
                // 太近：向后拉开距离
                Vec3 away = mob.position().subtract(target.position()).normalize();
                mob.getMoveControl().setWantedPosition(
                        mob.getX() + away.x * 6.0D,
                        mob.getY() + away.y * 3.0D + 1.0D,
                        mob.getZ() + away.z * 6.0D, 1.0D);
                this.settleTime = 10;
            } else {
                // 射程内且可见：悬停凝视，稳定后开始蓄力
                this.brake();
                if (this.cooldown <= 0) {
                    if (this.settleTime > 0) {
                        this.settleTime--;
                    } else {
                        mob.startBeamAttack(target);
                        this.cooldown = 30 + mob.random.nextInt(31);
                    }
                }
            }
        }

        /** 急停悬停：快速衰减速度，蓄力期间基本钉在空中（保留极慢下坠修正）。 */
        private void brake() {
            JietianmiEntity mob = JietianmiEntity.this;
            Vec3 delta = mob.getDeltaMovement();
            double y = Math.abs(delta.y) < 0.02D ? 0.0D : delta.y * 0.6D;
            mob.setDeltaMovement(delta.x * 0.6D, y, delta.z * 0.6D);
        }
    }

    /**
     * 无目标时的空中漂浮：恶魂式随机选取身体周围 ±16 水平、±8 高度的空中点飞过去，
     * 移动控制器空闲时才有几率改点，避免贴地乱转。
     */
    private class HoverWanderGoal extends Goal {
        HoverWanderGoal() {
            this.setFlags(EnumSet.of(Flag.MOVE));
        }

        @Override
        public boolean canUse() {
            JietianmiEntity mob = JietianmiEntity.this;
            return mob.getTarget() == null
                    && !mob.isBeaming()
                    && !mob.getMoveControl().hasWanted()
                    && mob.random.nextInt(40) == 0;
        }

        @Override
        public boolean canContinueToUse() {
            return false;
        }

        @Override
        public void tick() {
            if (JietianmiEntity.this.random.nextInt(20) == 0) {
                JietianmiEntity mob = JietianmiEntity.this;
                double tx = mob.getX() + (mob.random.nextDouble() * 2.0D - 1.0D) * 16.0D;
                double ty = mob.getY() + (mob.random.nextDouble() * 2.0D - 1.0D) * 8.0D;
                double tz = mob.getZ() + (mob.random.nextDouble() * 2.0D - 1.0D) * 16.0D;
                ty = Mth.clamp(ty, mob.level().getMinBuildHeight() + 2.0D, mob.level().getMaxBuildHeight() - 2.0D);
                mob.getMoveControl().setWantedPosition(tx, ty, tz, 0.85D);
            }
        }
    }
}
