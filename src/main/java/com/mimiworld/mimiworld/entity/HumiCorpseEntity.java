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
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
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
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

/**
 * 糊米古尸（Humi Corpse，注册 id = humi_corpse）——中式跳尸（jiangshi）。
 *
 * <p>模型/贴图/动画资源（均由 Blockbench 导出）：
 * geo/humi_corpse.geo.json、textures/entity/humi_corpse.png、
 * animations/humi_corpse.animation.json（动画键 animation.idle / animation.walk / animation.attack）。
 * 模型为 84 方块 7 骨骼双足人形，双臂前伸 90° 烘焙在骨组默认旋转上；大米主题：
 * 陈米蜡黄绿尸肤、糊米痂肿块、青灰破烂官袍、麻绳腰带、额头黄纸朱符、稻绿鬼火眼。
 * 模型实测约 31.1px 高（≈1.94 格），脚底贴 y=0，碰撞箱 0.7×1.9。</p>
 *
 * <p>生物逻辑：敌对亡灵（吃治疗药水受伤、亡灵杀手加成），白天不自燃；
 * 近战扑击玩家，追击方式为跳步（walk 动画是双腿同步的僵尸跳）。
 * 30 心（60 点血，沿用占位数值），移速 0.23、攻击力 3 对齐原版僵尸，音效使用原版僵尸音效。</p>
 */
public class HumiCorpseEntity extends Monster implements GeoEntity {

    /**
     * 同步数据：攻击动画剩余 tick（服务端命中时置 18 并逐 tick 递减，自动同步到客户端；
     * GeckoLib 动画在客户端播放，必须用 SynchedEntityData 而不能用普通字段）。
     * 做法与黏米之蛛一致。
     */
    private static final EntityDataAccessor<Integer> DATA_ATTACK_ANIM_TICKS =
            SynchedEntityData.defineId(HumiCorpseEntity.class, EntityDataSerializers.INT);

    // GeckoLib：每个实体实例持有一个动画状态缓存
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    // 待机：humi_corpse.animation.json 中的 "animation.idle"（loop=true，4 秒，呼吸+左右张望）
    private static final RawAnimation ANIM_IDLE = RawAnimation.begin().thenLoop("animation.idle");
    // 行走："animation.walk"（loop=true，1 秒，双腿同步僵尸跳+前伸臂摆动）
    private static final RawAnimation ANIM_WALK = RawAnimation.begin().thenLoop("animation.walk");
    // 攻击："animation.attack"（不循环，单次播放 0.9 秒，蓄力后仰→双爪水平前扑→收回）
    private static final RawAnimation ANIM_ATTACK = RawAnimation.begin().thenPlay("animation.attack");

    /** attack 动画时长 0.9 秒 = 18 tick */
    private static final int ATTACK_ANIMATION_TICKS = 18;

    public HumiCorpseEntity(EntityType<? extends HumiCorpseEntity> type, Level level) {
        super(type, level);
    }

    /**
     * 属性：30 心 = 60 血（沿用占位数值）、移速 0.23、近战攻击力 3（后两者对齐原版僵尸）。
     * 在 ModEntityAttributes 的 EntityAttributeCreationEvent 中注册。
     */
    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 60.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.23D)
                .add(Attributes.ATTACK_DAMAGE, 3.0D);
    }

    /**
     * AI 目标：0 水上漂浮；2 近战追击（速度倍率 1.0，可见未到达目标时持续追击）；
     * 5 避水随机游荡；6 看向玩家；7 四处张望。
     * 仇恨：1 受击反击；2 主动索敌玩家。
     */
    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.0D, false));
        this.goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 0.8D));
        this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 8.0F));
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
     * 动画状态：命中后的 18 tick 内播单次 attack；否则移动播 walk、静止播 idle。
     * setAndContinue 保证同名动画不会被逐帧重启，attack 单次播完自然切回。
     */
    private PlayState handleAnimState(AnimationState<HumiCorpseEntity> state) {
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
        // 仅服务端倒计时并驱动同步，客户端每 tick 收到 18→0 的新值
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

    /** 亡灵类型：受「亡灵杀手」加成、治疗药水伤它、伤害药水奶它（与僵尸同类，但不做白天自燃） */
    @Override
    public MobType getMobType() {
        return MobType.UNDEAD;
    }

    // -------------------- 音效：沿用原版僵尸 --------------------

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.ZOMBIE_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.ZOMBIE_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.ZOMBIE_DEATH;
    }

    @Override
    protected void playStepSound(BlockPos pos, BlockState state) {
        this.playSound(SoundEvents.ZOMBIE_STEP, 0.15F, 0.9F);
    }
}
