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
import net.minecraft.world.entity.MobType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LeapAtTargetGoal;
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

/**
 * 黏米之蛛（Sticky Rice Spider，注册 id = sticky_rice_spider）。
 *
 * <p>生物逻辑与原版 {@link net.minecraft.world.entity.monster.Spider} 完全一致：
 * 近战扑击（LeapAtTargetGoal）、避水游荡、攀爬垂直墙面（爬墙状态位 + onClimbable）、
 * 进入蜘蛛网不被减速、节肢动物类型（吃「节肢杀手」附魔加成）、原版蜘蛛音效，
 * 属性同为 16 血 / 0.3 移速 / 2 攻击力，碰撞箱 1.4×0.9。</p>
 *
 * <p>与原版蜘蛛的唯一区别：<b>全天敌对</b>。原版蜘蛛的索敌目标带 {@code isSunBurnTick}
 * 条件（白天不主动攻击玩家），这里直接使用无条件的 NearestAttackableTargetGoal，
 * 因此无论昼夜都会追击玩家。</p>
 *
 * <p>模型/贴图/动画由 GeckoLib 资源驱动：
 * geo/spider.geo.json、animations/spider.animation.json、textures/entity/spider.png；
 * 动画文件含 idle（待机循环）、walk（行走循环）、attack（攻击，单次播放 1 秒）。</p>
 */
public class StickyRiceSpiderEntity extends Monster implements GeoEntity {

    /** 同步数据：爬墙标志位（与原版 Spider 相同的字节位 0x1） */
    private static final EntityDataAccessor<Byte> DATA_FLAGS_ID =
            SynchedEntityData.defineId(StickyRiceSpiderEntity.class, EntityDataSerializers.BYTE);
    /**
     * 同步数据：攻击动画剩余 tick（服务端命中时置 20 并逐 tick 递减，自动同步到客户端；
     * GeckoLib 动画在客户端播放，必须用 SynchedEntityData 而不能用普通字段）
     */
    private static final EntityDataAccessor<Integer> DATA_ATTACK_ANIM_TICKS =
            SynchedEntityData.defineId(StickyRiceSpiderEntity.class, EntityDataSerializers.INT);

    // GeckoLib：每个实体实例持有一个动画状态缓存
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    // 待机：spider.animation.json 中的 "idle"（loop=true，2 秒）
    private static final RawAnimation ANIM_IDLE = RawAnimation.begin().thenLoop("idle");
    // 行走："walk"（loop=true，1 秒）
    private static final RawAnimation ANIM_WALK = RawAnimation.begin().thenLoop("walk");
    // 攻击："attack"（不循环，单次播放 1 秒）
    private static final RawAnimation ANIM_ATTACK = RawAnimation.begin().thenPlay("attack");

    /** attack 动画时长 20 tick（动画文件里 attack 为 1 秒单次播放） */
    private static final int ATTACK_ANIMATION_TICKS = 20;

    public StickyRiceSpiderEntity(EntityType<? extends StickyRiceSpiderEntity> type, Level level) {
        super(type, level);
    }

    /**
     * 属性与原版蜘蛛完全相同：16 血、0.3 移速、2 近战攻击力。
     * 在 ModEntityAttributes 的 EntityAttributeCreationEvent 中注册。
     */
    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 16.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.30000001192092896D)
                .add(Attributes.ATTACK_DAMAGE, 2.0D);
    }

    /**
     * AI 目标（优先级照搬原版 Spider）：
     * 1 漂浮避溺；3 扑向目标；4 近战追击；5 避水随机游荡；6/6 看玩家/四处张望。
     */
    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new FloatGoal(this));
        this.goalSelector.addGoal(3, new LeapAtTargetGoal(this, 0.4F));
        this.goalSelector.addGoal(4, new MeleeAttackGoal(this, 1.0D, true));
        this.goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 0.8D));
        this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(6, new RandomLookAroundGoal(this));

        // 仇恨目标：受击反击 + 主动索敌玩家。
        // 原版 Spider 的索敌带 isSunBurnTick（白天中立），本实体全天敌对故不加该条件。
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    // -------------------- GeckoLib 动画 --------------------

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        // 过渡时间 5 tick（与项目内 MicroDuck 一致）
        controllers.add(new AnimationController<>(this, "main", 5, this::handleAnimState));
    }

    /**
     * 动画状态：攻击命中后的 20 tick 内播单次 attack；否则移动播 walk、静止播 idle。
     * setAndContinue 保证同名动画不会被逐帧重启，attack 单次播完自然切回。
     */
    private PlayState handleAnimState(AnimationState<StickyRiceSpiderEntity> state) {
        if (this.entityData.get(DATA_ATTACK_ANIM_TICKS) > 0) {
            state.setAndContinue(ANIM_ATTACK);
            return PlayState.CONTINUE;
        }
        if (state.isMoving()) {
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

    /** 命中目标时触发一次攻击动画（doHurtTarget 在服务端结算伤害成功时调用一次，数据自动同步） */
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
        // 仅服务端倒计时并驱动同步，客户端每 tick 收到 20→0 的新值
        if (!this.level().isClientSide) {
            int ticks = this.entityData.get(DATA_ATTACK_ANIM_TICKS);
            if (ticks > 0) {
                this.entityData.set(DATA_ATTACK_ANIM_TICKS, ticks - 1);
            }
        }
    }

    // -------------------- 原版蜘蛛行为复刻 --------------------

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_FLAGS_ID, (byte) 0);
        this.entityData.define(DATA_ATTACK_ANIM_TICKS, 0);
    }

    /** 蜘蛛网不减速（原版蜘蛛特性），其他方块正常处理黏性 */
    @Override
    public void makeStuckInBlock(BlockState state, Vec3 motionMultiplier) {
        if (!state.is(net.minecraft.world.level.block.Blocks.COBWEB)) {
            super.makeStuckInBlock(state, motionMultiplier);
        }
    }

    /** 节肢动物：受「节肢杀手」附魔额外伤害（原版蜘蛛同为 ARTHROPOD） */
    @Override
    public MobType getMobType() {
        return MobType.ARTHROPOD;
    }

    /** LivingEntity 判定能否攀爬时调用：爬墙状态位为 true 时即可贴墙移动 */
    @Override
    public boolean onClimbable() {
        return this.isClimbing();
    }

    public boolean isClimbing() {
        return (this.entityData.get(DATA_FLAGS_ID) & 1) != 0;
    }

    public void setClimbing(boolean climbing) {
        byte flag = this.entityData.get(DATA_FLAGS_ID);
        if (climbing) {
            flag = (byte) (flag | 1);
        } else {
            flag = (byte) (flag & -2);
        }
        this.entityData.set(DATA_FLAGS_ID, flag);
    }

    @Override
    public void tick() {
        super.tick();
        // 与原版 Spider 一致：贴墙水平碰撞时进入爬墙状态（仅服务端驱动并同步给客户端）
        if (!this.level().isClientSide) {
            this.setClimbing(this.horizontalCollision);
        }
    }

    /** 眼睛高度与原版蜘蛛一致（0.65） */
    @Override
    protected float getStandingEyeHeight(Pose pose, net.minecraft.world.entity.EntityDimensions dimensions) {
        return 0.65F;
    }

    @Override
    public double getPassengersRidingOffset() {
        return this.getBbHeight() * 0.5F;
    }

    // -------------------- 音效：全部沿用原版蜘蛛 --------------------

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.SPIDER_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.SPIDER_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.SPIDER_DEATH;
    }

    @Override
    protected void playStepSound(BlockPos pos, BlockState state) {
        this.playSound(SoundEvents.SPIDER_STEP, 0.15F, 0.9F);
    }
}
