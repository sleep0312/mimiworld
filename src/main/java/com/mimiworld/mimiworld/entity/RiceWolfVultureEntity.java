package com.mimiworld.mimiworld.entity;

import net.minecraft.core.BlockPos;
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
 * 哮噬米狼鹫（Rice Wolf-Vulture，注册 id = rice_wolf_vulture）——大米主题的陆空敌对飞行狼鹫。
 *
 * <p>模型/贴图/动画资源（均由 Blockbench 导出）：
 * geo/rice_wolf_vulture.geo.json（181 方块 15 骨骼，geometry.xiaoshimilangjiu）、
 * textures/entity/rice_wolf_vulture.png、animations/rice_wolf_vulture.animation.json
 * （动画键 animation.xiaoshimilangjiu.idle / .walk / .attack_bite / .attack_dive）。
 * 模型为四足狼身+双翼+三折尾穗造型，脚底贴 y=0、实测约 22.15px 高（≈1.38 格），
 * 大米主题：米白狼毛+金绿稻羽+金竖瞳+暖棕鼻垫。碰撞箱 1.2×1.5（躯干宽，折叠翼尖超出箱体）。</p>
 *
 * <p>生物逻辑：敌对怪物，500 心（沿用占位数值）、近战攻击 500、移速 0.32、满击退抗性。
 * 两种攻击方式：①贴身时 {@link MeleeAttackGoal} 驱动的前扑啃咬（attack_bite）；
 * ②中距离（4~16 格）的招牌技 {@link DiveAttackGoal}：振翅升空盘旋一圈后收翅俯冲撞击，
 * 俯冲末段给一个朝目标的高速扑击速度并在贴身时结算伤害（attack_dive）。
 * 动画中的升空/盘旋位移烘焙在模型层（假位移），实体物理仍贴地。</p>
 */
public class RiceWolfVultureEntity extends Monster implements GeoEntity {

    /** 同步数据：啃咬动画剩余 tick（attack_bite 长 1.2s = 24 tick） */
    private static final EntityDataAccessor<Integer> DATA_BITE_ANIM_TICKS =
            SynchedEntityData.defineId(RiceWolfVultureEntity.class, EntityDataSerializers.INT);
    /** 同步数据：俯冲动画剩余 tick（attack_dive 长 2.6s = 52 tick） */
    private static final EntityDataAccessor<Integer> DATA_DIVE_ANIM_TICKS =
            SynchedEntityData.defineId(RiceWolfVultureEntity.class, EntityDataSerializers.INT);

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    private static final RawAnimation ANIM_IDLE =
            RawAnimation.begin().thenLoop("animation.xiaoshimilangjiu.idle");
    private static final RawAnimation ANIM_WALK =
            RawAnimation.begin().thenLoop("animation.xiaoshimilangjiu.walk");
    private static final RawAnimation ANIM_BITE =
            RawAnimation.begin().thenPlay("animation.xiaoshimilangjiu.attack_bite");
    private static final RawAnimation ANIM_DIVE =
            RawAnimation.begin().thenPlay("animation.xiaoshimilangjiu.attack_dive");

    private static final int BITE_ANIMATION_TICKS = 24;
    private static final int DIVE_ANIMATION_TICKS = 52;

    public RiceWolfVultureEntity(EntityType<? extends RiceWolfVultureEntity> type, Level level) {
        super(type, level);
    }

    /**
     * 属性：500 心 = 1000 血、近战 500（沿用占位数值），移速 0.32、追击范围 40（俯冲要在中距离发动）、
     * 满击退抗性（大体型 Boss 级怪物不被打飞）。
     */
    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 1000.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.32D)
                .add(Attributes.FOLLOW_RANGE, 40.0D)
                .add(Attributes.ATTACK_DAMAGE, 500.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 1.0D);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        // 招牌俯冲撞击：优先级高于普通近战，中距离冷却发动
        this.goalSelector.addGoal(1, new DiveAttackGoal());
        this.goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.15D, false));
        this.goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 0.8D));
        this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 10.0F));
        this.goalSelector.addGoal(7, new RandomLookAroundGoal(this));

        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    // -------------------- GeckoLib 动画 --------------------

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        // 过渡时间 5 tick（与项目内其他 GeckoLib 实体一致）
        controllers.add(new AnimationController<>(this, "main", 5, this::handleAnimState));
    }

    /**
     * 动画优先级：俯冲（2.6s 全程不可打断）＞ 啃咬 ＞ 移动 walk ＞ 静止 idle。
     * setAndContinue 保证同名动画不被逐帧重启，单次动画播完自然切回。
     */
    private PlayState handleAnimState(AnimationState<RiceWolfVultureEntity> state) {
        if (this.entityData.get(DATA_DIVE_ANIM_TICKS) > 0) {
            state.setAndContinue(ANIM_DIVE);
        } else if (this.entityData.get(DATA_BITE_ANIM_TICKS) > 0) {
            state.setAndContinue(ANIM_BITE);
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

    /** 啃咬命中时触发一次 attack_bite；俯冲期间命中不覆盖俯冲动画（俯冲优先级更高） */
    @Override
    public boolean doHurtTarget(Entity target) {
        if (super.doHurtTarget(target)) {
            if (this.entityData.get(DATA_DIVE_ANIM_TICKS) == 0) {
                this.entityData.set(DATA_BITE_ANIM_TICKS, BITE_ANIMATION_TICKS);
            }
            return true;
        }
        return false;
    }

    @Override
    public void aiStep() {
        super.aiStep();
        // 服务端倒计时，数据自动同步到客户端驱动动画
        if (!this.level().isClientSide) {
            int dive = this.entityData.get(DATA_DIVE_ANIM_TICKS);
            if (dive > 0) {
                this.entityData.set(DATA_DIVE_ANIM_TICKS, dive - 1);
            }
            int bite = this.entityData.get(DATA_BITE_ANIM_TICKS);
            if (bite > 0) {
                this.entityData.set(DATA_BITE_ANIM_TICKS, bite - 1);
            }
        }
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_BITE_ANIM_TICKS, 0);
        this.entityData.define(DATA_DIVE_ANIM_TICKS, 0);
    }

    // -------------------- 音效：狼嗥 + 幻翼俯冲声 --------------------

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.WOLF_GROWL;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.WOLF_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.WOLF_DEATH;
    }

    @Override
    protected void playStepSound(BlockPos pos, BlockState state) {
        this.playSound(SoundEvents.WOLF_STEP, 0.2F, 0.85F);
    }

    /**
     * 招牌攻击：盘旋升空一圈后俯冲撞击。
     *
     * <p>时间线对齐动画：t=0 起播 attack_dive（52 tick）；t≈1.9s（38 tick，收翅俯冲段）
     * 给一个朝目标的高速扑冲速度；t=2.0~2.5s 贴身时结算一次近战伤害（猛禽撞击）。
     * 视觉上的升空/盘旋是烘焙动画，物理上用速度冲量表现撞击段。冷却 8 秒。</p>
     */
    private class DiveAttackGoal extends Goal {
        /** 俯冲内已进行 tick（0..52） */
        private int diveTime;
        /** 冷却 tick，俯冲结束后进入冷却 */
        private int cooldown;
        /** 本次俯冲是否已经结算过撞击伤害 */
        private boolean hitDone;

        DiveAttackGoal() {
            this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            LivingEntity target = RiceWolfVultureEntity.this.getTarget();
            if (this.cooldown > 0) {
                this.cooldown--;
                return false;
            }
            if (target == null || !target.isAlive()) {
                return false;
            }
            if (RiceWolfVultureEntity.this.entityData.get(DATA_DIVE_ANIM_TICKS) > 0) {
                return false;
            }
            double distSq = RiceWolfVultureEntity.this.distanceToSqr(target);
            // 中距离窗口 4~16 格、需要视线、必须站在实地上才能蹬地起飞
            return distSq >= 16.0D && distSq <= 256.0D
                    && RiceWolfVultureEntity.this.onGround()
                    && RiceWolfVultureEntity.this.hasLineOfSight(target);
        }

        @Override
        public boolean canContinueToUse() {
            return this.diveTime < DIVE_ANIMATION_TICKS;
        }

        @Override
        public void start() {
            this.diveTime = 0;
            this.hitDone = false;
            RiceWolfVultureEntity.this.entityData.set(DATA_DIVE_ANIM_TICKS, DIVE_ANIMATION_TICKS);
            RiceWolfVultureEntity.this.playSound(SoundEvents.PHANTOM_FLAP, 1.2F, 0.7F);
        }

        @Override
        public void tick() {
            this.diveTime++;
            LivingEntity target = RiceWolfVultureEntity.this.getTarget();
            if (target == null) {
                return;
            }
            // 全程面向目标
            RiceWolfVultureEntity.this.getLookControl().setLookAt(target, 30.0F, 30.0F);

            if (this.diveTime == 38) {
                // 收翅俯冲瞬间：朝目标方向高速扑出（略带仰角越过地形）
                Vec3 dir = new Vec3(
                        target.getX() - RiceWolfVultureEntity.this.getX(),
                        Math.max(-1.0D, target.getY() - RiceWolfVultureEntity.this.getY()),
                        target.getZ() - RiceWolfVultureEntity.this.getZ());
                if (dir.lengthSqr() > 1.0E-4D) {
                    dir = dir.normalize();
                    RiceWolfVultureEntity.this.setDeltaMovement(dir.x * 1.7D, 0.22D, dir.z * 1.7D);
                    RiceWolfVultureEntity.this.hasImpulse = true;
                }
                RiceWolfVultureEntity.this.playSound(SoundEvents.PHANTOM_SWOOP, 1.4F, 0.8F);
            }

            // 撞击窗口：动画急刹撞脸的 2.0~2.5s
            if (!this.hitDone && this.diveTime >= 40 && this.diveTime <= 50) {
                double verticalGap = Math.abs(target.getY() - RiceWolfVultureEntity.this.getY());
                if (verticalGap < 2.5D
                        && RiceWolfVultureEntity.this.distanceToSqr(target) < 7.0D) {
                    this.hitDone = RiceWolfVultureEntity.this.doHurtTarget(target);
                }
            }
        }

        @Override
        public void stop() {
            this.diveTime = 0;
            this.cooldown = 160;
        }
    }
}
