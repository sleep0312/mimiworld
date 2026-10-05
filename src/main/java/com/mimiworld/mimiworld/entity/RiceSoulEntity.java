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
import net.minecraft.world.entity.ai.control.FlyingMoveControl;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.pathfinder.BlockPathTypes;
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
 * 米魂（注册 id = rice_soul）——飘在空中的米之魂，敌对飞行近战生物。
 *
 * <p>GeckoLib 资源（Blockbench 导出）：
 * geo/mihun.geo.json（geometry.mihun，伞状魂体 body + 8 根下垂触须 t_nw..t_se + 两侧魂火飘带 wispL/wispR，
 * 模型宽约 18px、含触须总高约 24px）、textures/entity/mihun.png（128×128）、
 * animations/mihun.animation.json（animation.mihun.idle / .walk / .attack）。碰撞箱 1.1×1.6。</p>
 *
 * <p>行为：常驻无重力悬浮飞行（{@link FlyingMoveControl} + {@link FlyingPathNavigation}），
 * 主动索敌玩家、受击反击，贴近后用触须扑击（近战）；无目标时在空中随机漂浮。
 * 9 心（沿用占位数值）、近战 3 点；音效沿用原版恼鬼（尖细的魂鸣）。</p>
 */
public class RiceSoulEntity extends Monster implements GeoEntity {

    /** 同步数据：攻击动画剩余 tick（服务端命中时置 20 并逐 tick 递减，自动同步到客户端）。 */
    private static final EntityDataAccessor<Integer> DATA_ATTACK_ANIM_TICKS =
            SynchedEntityData.defineId(RiceSoulEntity.class, EntityDataSerializers.INT);

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    private static final RawAnimation ANIM_IDLE =
            RawAnimation.begin().thenLoop("animation.mihun.idle");
    private static final RawAnimation ANIM_WALK =
            RawAnimation.begin().thenLoop("animation.mihun.walk");
    private static final RawAnimation ANIM_ATTACK =
            RawAnimation.begin().thenPlay("animation.mihun.attack");

    /** attack 动画时长 1 秒 = 20 tick */
    private static final int ATTACK_ANIMATION_TICKS = 20;

    /** 位移速度平方阈值：高于它视为飞行移动播 walk，否则悬停播 idle */
    private static final double MOVE_ANIM_SPEED_SQR = 0.0025D;

    public RiceSoulEntity(EntityType<? extends RiceSoulEntity> type, Level level) {
        super(type, level);
        // 常驻飞行：无重力 + 急停悬停式飞行控制器
        this.setNoGravity(true);
        this.moveControl = new FlyingMoveControl(this, 10, true);
        this.setPathfindingMalus(BlockPathTypes.WATER, -1.0F);
        this.setPathfindingMalus(BlockPathTypes.LAVA, 8.0F);
        this.setPathfindingMalus(BlockPathTypes.DANGER_FIRE, 0.0F);
        this.setPathfindingMalus(BlockPathTypes.DAMAGE_FIRE, 0.0F);
    }

    /** 属性：9 心 = 18 血（沿用占位数值）、移速 0.25、飞行 1.0、近战 3、追击 32。 */
    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 18.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.25D)
                .add(Attributes.FLYING_SPEED, 1.0D)
                .add(Attributes.ATTACK_DAMAGE, 3.0D)
                .add(Attributes.FOLLOW_RANGE, 32.0D);
    }

    /** 飞行生物使用空中直线寻路（节点允许悬空点位，同恼鬼/烈焰人）。 */
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
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.0D, true));
        this.goalSelector.addGoal(3, new HoverWanderGoal());
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

    private PlayState handleAnimState(AnimationState<RiceSoulEntity> state) {
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

    @Override
    public boolean doHurtTarget(Entity target) {
        if (super.doHurtTarget(target)) {
            this.entityData.set(DATA_ATTACK_ANIM_TICKS, ATTACK_ANIMATION_TICKS);
            return true;
        }
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
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_ATTACK_ANIM_TICKS, 0);
    }

    // -------------------- 杂项：摔落免疫/音效 --------------------

    @Override
    public boolean causeFallDamage(float distance, float multiplier, DamageSource source) {
        // 飞行生物，永不承受摔落伤害
        return false;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.VEX_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.VEX_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.VEX_DEATH;
    }

    @Override
    public int getAmbientSoundInterval() {
        return 120;
    }

    /**
     * 无目标时的空中漂浮：随机选取身体周围 ±12 水平、±8 高度的空中点飘过去，
     * 移动控制器空闲时才有几率改点，避免原地乱转。
     */
    private class HoverWanderGoal extends Goal {
        HoverWanderGoal() {
            this.setFlags(EnumSet.of(Flag.MOVE));
        }

        @Override
        public boolean canUse() {
            return RiceSoulEntity.this.getTarget() == null
                    && !RiceSoulEntity.this.getMoveControl().hasWanted()
                    && RiceSoulEntity.this.random.nextInt(40) == 0;
        }

        @Override
        public boolean canContinueToUse() {
            return false;
        }

        @Override
        public void tick() {
            if (RiceSoulEntity.this.random.nextInt(20) == 0) {
                RiceSoulEntity soul = RiceSoulEntity.this;
                double tx = soul.getX() + (soul.random.nextDouble() * 2.0D - 1.0D) * 12.0D;
                double ty = soul.getY() + (soul.random.nextDouble() * 2.0D - 1.0D) * 8.0D + 2.0D;
                double tz = soul.getZ() + (soul.random.nextDouble() * 2.0D - 1.0D) * 12.0D;
                ty = Math.max(ty, soul.level().getMinBuildHeight() + 2.0D);
                soul.getMoveControl().setWantedPosition(tx, ty, tz, 0.85D);
            }
        }
    }
}
