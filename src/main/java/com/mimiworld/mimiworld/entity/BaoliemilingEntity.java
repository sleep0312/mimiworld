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
import net.minecraft.world.entity.projectile.SmallFireball;
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
 * 爆烈米灵（注册 id = explosive_rice_spirit）——烈焰人式敌对飞行生物。
 *
 * <p>GeckoLib 资源（Blockbench 导出）：
 * geo/baoliemiling.geo.json（geometry.baoliemiling，白米怒脸+火焰冠+熔岩米心+12 根金米棒+4 颗火星）、
 * textures/entity/baoliemiling.png（64×64）、animations/baoliemiling.animation.json
 * （animation.baoliemiling.idle / .walk / .attack）。</p>
 *
 * <p>行为对齐原版烈焰人：常驻无重力悬浮飞行（{@link FlyingMoveControl} + {@link FlyingPathNavigation}）、
 * 火焰免疫；主动索敌玩家、受击反击；交战时贴近 2 格内近战，否则蓄势连喷三发火米弹（{@link SmallFireball}）；
 * 无目标时在空中随机漂浮。近战命中或开火的瞬间播放一次 attack 动画（1.2 秒），
 * 飞行位移时播 walk，悬停时播 idle。60 心（沿用原占位数值），近战 6 点、追击 48 格对齐烈焰人。</p>
 */
public class BaoliemilingEntity extends Monster implements GeoEntity {

    /**
     * 同步数据：攻击动画剩余 tick（服务端开火/命中时置 24 并逐 tick 递减，自动同步到客户端；
     * GeckoLib 动画在客户端播放，必须用 SynchedEntityData）。做法同糊米古尸。
     */
    private static final EntityDataAccessor<Integer> DATA_ATTACK_ANIM_TICKS =
            SynchedEntityData.defineId(BaoliemilingEntity.class, EntityDataSerializers.INT);

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    // baoliemiling.animation.json 中的三条动画
    private static final RawAnimation ANIM_IDLE = RawAnimation.begin().thenLoop("animation.baoliemiling.idle");
    private static final RawAnimation ANIM_WALK = RawAnimation.begin().thenLoop("animation.baoliemiling.walk");
    private static final RawAnimation ANIM_ATTACK = RawAnimation.begin().thenPlay("animation.baoliemiling.attack");

    /** attack 动画时长 1.2 秒 = 24 tick */
    private static final int ATTACK_ANIMATION_TICKS = 24;

    /** 位移速度平方阈值：高于它视为飞行移动播 walk，否则悬停播 idle */
    private static final double MOVE_ANIM_SPEED_SQR = 0.0025D;

    public BaoliemilingEntity(EntityType<? extends BaoliemilingEntity> type, Level level) {
        super(type, level);
        // 常驻飞行：无重力 + 烈焰人式悬停飞行控制器（第三参 true = 急停悬停）
        this.setNoGravity(true);
        this.moveControl = new FlyingMoveControl(this, 10, true);
        // 火焰免疫：不怕岩浆/火焰伤害方块，但避开水面
        this.setPathfindingMalus(BlockPathTypes.WATER, -1.0F);
        this.setPathfindingMalus(BlockPathTypes.LAVA, 8.0F);
        this.setPathfindingMalus(BlockPathTypes.DAMAGE_FIRE, 0.0F);
        this.setPathfindingMalus(BlockPathTypes.DANGER_FIRE, 0.0F);
    }

    /**
     * 属性：60 心 = 120 血（沿用原占位数值），移速 0.23 / 近战 6 / 追击 48 对齐原版烈焰人，
     * FLYING_SPEED 为飞行控制器的加速系数。
     */
    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 120.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.23D)
                .add(Attributes.FLYING_SPEED, 1.0D)
                .add(Attributes.ATTACK_DAMAGE, 6.0D)
                .add(Attributes.FOLLOW_RANGE, 48.0D);
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
        // 2：烈焰人式近战+远程火弹攻击循环；3：无目标空中漂浮；6/7：张望
        this.goalSelector.addGoal(2, new RiceSpiritAttackGoal());
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
     * 攻击动画播放的 24 tick 内强制播单次 attack；否则按飞行速度切 walk / idle。
     * setAndContinue 保证同名动画不被逐帧重启，attack 播完自然切回。
     */
    private PlayState handleAnimState(AnimationState<BaoliemilingEntity> state) {
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

    /** 近战命中成功时触发攻击动画 */
    @Override
    public boolean doHurtTarget(Entity target) {
        if (super.doHurtTarget(target)) {
            this.triggerAttackAnim();
            return true;
        }
        return false;
    }

    private void triggerAttackAnim() {
        this.entityData.set(DATA_ATTACK_ANIM_TICKS, ATTACK_ANIMATION_TICKS);
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
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_ATTACK_ANIM_TICKS, 0);
    }

    // -------------------- 杂项：火焰免疫/摔落/音效 --------------------

    @Override
    public boolean causeFallDamage(float distance, float multiplier, DamageSource source) {
        // 飞行生物，永不承受摔落伤害
        return false;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.BLAZE_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.BLAZE_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.BLAZE_DEATH;
    }

    @Override
    public int getAmbientSoundInterval() {
        return 120;
    }

    // -------------------- 烈焰人式攻击 AI --------------------

    /**
     * 烈焰人攻击节奏：近距离（2 格内）每 20 tick 一次近战并贴向目标；
     * 远距离且连续可见 20 tick 后先蓄势 60 tick，然后每隔 6 tick 喷一发小火弹、连发三发，
     * 之后冷却 100 tick。开火期间始终面朝目标并向其靠拢。
     */
    private class RiceSpiritAttackGoal extends Goal {
        private int attackStep;
        private int attackTime;
        private int lastSeen;

        RiceSpiritAttackGoal() {
            this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            LivingEntity target = BaoliemilingEntity.this.getTarget();
            return target != null && target.isAlive();
        }

        @Override
        public boolean canContinueToUse() {
            LivingEntity target = BaoliemilingEntity.this.getTarget();
            return target != null && target.isAlive();
        }

        @Override
        public void start() {
            this.attackStep = 0;
        }

        @Override
        public void stop() {
            this.lastSeen = 0;
        }

        @Override
        public boolean requiresUpdateEveryTick() {
            return true;
        }

        @Override
        public void tick() {
            --this.attackTime;
            LivingEntity target = BaoliemilingEntity.this.getTarget();
            if (target == null) {
                return;
            }
            boolean hasLineOfSight = BaoliemilingEntity.this.getSensing().hasLineOfSight(target);
            boolean wasVisible = this.lastSeen > 0;
            if (hasLineOfSight != wasVisible) {
                this.lastSeen = 0;
            }
            if (hasLineOfSight) {
                ++this.lastSeen;
            } else {
                --this.lastSeen;
            }

            double distSqr = BaoliemilingEntity.this.distanceToSqr(target);
            double followRange = BaoliemilingEntity.this.getAttributeValue(Attributes.FOLLOW_RANGE);
            if (distSqr < 4.0D) {
                // 近战距离：每 20 tick 扑击一次，身体持续压向目标
                if (this.attackTime <= 0 && hasLineOfSight) {
                    this.attackTime = 20;
                    BaoliemilingEntity.this.doHurtTarget(target);
                }
                BaoliemilingEntity.this.getMoveControl().setWantedPosition(
                        target.getX(), target.getY(), target.getZ(), 1.0D);
            } else if (distSqr < followRange * followRange && this.lastSeen >= 20) {
                // 远程：蓄势后连发三发火米弹
                if (this.attackTime <= 0) {
                    ++this.attackStep;
                    if (this.attackStep == 1) {
                        this.attackTime = 60;
                    } else if (this.attackStep <= 4) {
                        this.attackTime = 6;
                    } else {
                        this.attackTime = 100;
                        this.attackStep = 0;
                    }
                    // attackStep 2/3/4 为三次实际开火
                    if (this.attackStep > 1 && this.attackStep <= 4) {
                        this.shootFireRice(target);
                    }
                }
                BaoliemilingEntity.this.getLookControl().setLookAt(
                        target.getX(), target.getY(0.5D), target.getZ(), 10.0F, 10.0F);
                BaoliemilingEntity.this.getMoveControl().setWantedPosition(
                        target.getX(), target.getY(), target.getZ(), 1.0D);
            } else {
                // 丢失视野/超出追击距离时仍朝目标方向看
                BaoliemilingEntity.this.getLookControl().setLookAt(
                        target.getX(), target.getY(0.5D), target.getZ(), 10.0F, 10.0F);
            }
            super.tick();
        }

        /** 从身前沿视线方向发射一枚烈焰人小火弹（沿用原版实体，爆炸/点燃效果一致） */
        private void shootFireRice(LivingEntity target) {
            BaoliemilingEntity spirit = BaoliemilingEntity.this;
            Vec3 view = spirit.getViewVector(1.0F);
            double dx = target.getX() - (spirit.getX() + view.x * 4.0D);
            double dy = target.getY(0.5D) - (0.5D + spirit.getY(0.5D));
            double dz = target.getZ() - (spirit.getZ() + view.z * 4.0D);
            if (!spirit.isSilent()) {
                spirit.level().levelEvent(null, 1018, spirit.blockPosition(), 0);
                spirit.playSound(SoundEvents.BLAZE_SHOOT, 1.0F, 1.0F);
            }
            SmallFireball fireball = new SmallFireball(spirit.level(), spirit, dx, dy, dz);
            fireball.setPos(
                    spirit.getX() + view.x * 4.0D,
                    spirit.getY(0.5D) + 0.5D,
                    spirit.getZ() + view.z * 4.0D);
            spirit.level().addFreshEntity(fireball);
            spirit.triggerAttackAnim();
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
            BaoliemilingEntity spirit = BaoliemilingEntity.this;
            return spirit.getTarget() == null
                    && !spirit.getMoveControl().hasWanted()
                    && spirit.random.nextInt(40) == 0;
        }

        @Override
        public boolean canContinueToUse() {
            return false;
        }

        @Override
        public void tick() {
            if (BaoliemilingEntity.this.random.nextInt(20) == 0) {
                BaoliemilingEntity spirit = BaoliemilingEntity.this;
                double tx = spirit.getX() + (spirit.random.nextDouble() * 2.0D - 1.0D) * 16.0D;
                double ty = spirit.getY() + (spirit.random.nextDouble() * 2.0D - 1.0D) * 8.0D;
                double tz = spirit.getZ() + (spirit.random.nextDouble() * 2.0D - 1.0D) * 16.0D;
                ty = Math.max(ty, spirit.level().getMinBuildHeight() + 2.0D);
                spirit.getMoveControl().setWantedPosition(tx, ty, tz, 0.85D);
            }
        }
    }
}
