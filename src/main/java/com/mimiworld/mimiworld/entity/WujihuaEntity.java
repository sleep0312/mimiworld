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
 * 污棘花（注册 id = blighted_thorn_flower）——扎根原地的敌对食肉植物，类似植物大战僵尸大嘴花：
 * 不会移动，猎物进入咬合范围就猛咬一口然后原地咀嚼。
 *
 * <p>GeckoLib 资源（Blockbench 导出）：
 * geo/wujihua.geo.json（geometry.wujihua，root/body/head/upper_jaw/lower_jaw，
 * 米白鳞茎+稻叶+污褐厚唇+米牙+暗红口腔）、textures/entity/wujihua.png（128×128）、
 * animations/wujihua.animation.json（animation.wujihua.idle / .attack，无 walk——它不会走）。
 * 碰撞箱 0.9×1.8。</p>
 *
 * <p>扎根实现：不注册任何移动 AI，{@link #travel} 清零水平速度，免疫击退且不会被实体挤开；
 * 500 心、攻击力 50（均沿用占位数值）；咬合音沿用原版史莱姆的湿软音效。</p>
 */
public class WujihuaEntity extends Monster implements GeoEntity {

    /** 同步数据：攻击动画剩余 tick（服务端咬中时置 24 并逐 tick 递减，自动同步到客户端）。 */
    private static final EntityDataAccessor<Integer> DATA_ATTACK_ANIM_TICKS =
            SynchedEntityData.defineId(WujihuaEntity.class, EntityDataSerializers.INT);

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    private static final RawAnimation ANIM_IDLE =
            RawAnimation.begin().thenLoop("animation.wujihua.idle");
    private static final RawAnimation ANIM_ATTACK =
            RawAnimation.begin().thenPlay("animation.wujihua.attack");

    /** attack 动画时长 1.2 秒 = 24 tick */
    private static final int ATTACK_ANIMATION_TICKS = 24;

    /** 咬合触发距离平方：中心距 2 格内（花自身半宽 + 玩家半宽 + 嘴部前伸） */
    private static final double CHOMP_RANGE_SQR = 4.0D;

    public WujihuaEntity(EntityType<? extends WujihuaEntity> type, Level level) {
        super(type, level);
    }

    /** 属性：500 心 = 1000 血、攻击力 50（沿用占位数值）；移速 0、满击退抗性（扎根）。 */
    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 1000.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.0D)
                .add(Attributes.ATTACK_DAMAGE, 50.0D)
                .add(Attributes.FOLLOW_RANGE, 32.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 1.0D);
    }

    @Override
    protected void registerGoals() {
        // 注意：不注册任何移动类 Goal（不游荡、不追击），只有原地啃咬 + 张望
        this.goalSelector.addGoal(2, new ChompGoal());
        this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(7, new RandomLookAroundGoal(this));

        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    /**
     * 扎根：永久清零水平速度，只让父类结算重力与落地碰撞，因此水流/冲击都无法把它带走。
     */
    @Override
    public void travel(Vec3 travelVector) {
        Vec3 movement = this.getDeltaMovement();
        if (movement.x != 0.0D || movement.z != 0.0D) {
            this.setDeltaMovement(0.0D, movement.y, 0.0D);
        }
        super.travel(Vec3.ZERO);
    }

    /** 植物不可被其他实体挤动。 */
    @Override
    public boolean isPushable() {
        return false;
    }

    // -------------------- GeckoLib 动画 --------------------

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "main", 5, this::handleAnimState));
    }

    private PlayState handleAnimState(AnimationState<WujihuaEntity> state) {
        if (this.entityData.get(DATA_ATTACK_ANIM_TICKS) > 0) {
            state.setAndContinue(ANIM_ATTACK);
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

    // -------------------- 音效：湿软的植物咬合声 --------------------

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.SLIME_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.SLIME_DEATH;
    }

    /**
     * 原地啃咬 AI：面朝进入范围的目标，每 30 tick 咬一次（配合 24 tick 咬合+咀嚼动画），
     * 不做任何位移。
     */
    private class ChompGoal extends Goal {
        private int cooldown;

        ChompGoal() {
            this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            LivingEntity target = WujihuaEntity.this.getTarget();
            return target != null && target.isAlive();
        }

        @Override
        public boolean canContinueToUse() {
            LivingEntity target = WujihuaEntity.this.getTarget();
            return target != null && target.isAlive();
        }

        @Override
        public void tick() {
            --this.cooldown;
            LivingEntity target = WujihuaEntity.this.getTarget();
            if (target == null) {
                return;
            }
            WujihuaEntity.this.getLookControl().setLookAt(
                    target.getX(), target.getY(0.5D), target.getZ(), 10.0F, 10.0F);

            if (this.cooldown <= 0
                    && WujihuaEntity.this.distanceToSqr(target) <= CHOMP_RANGE_SQR
                    && WujihuaEntity.this.getSensing().hasLineOfSight(target)) {
                if (WujihuaEntity.this.doHurtTarget(target)) {
                    // 一口咬实：进入咬合+咀嚼节奏
                    this.cooldown = 30;
                    WujihuaEntity.this.playSound(SoundEvents.SLIME_ATTACK, 1.0F, 0.7F);
                } else {
                    this.cooldown = 10;
                }
            }
        }
    }
}
