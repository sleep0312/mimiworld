package com.mimiworld.mimiworld.entity;

import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.PanicGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

/**
 * 米羊羊（Mimi Sheep）——占位实体转正后的真实 GeckoLib 实体。
 *
 * <p>模型/贴图/动画资源：
 * geo/mimi_sheep.geo.json、textures/entity/mimi_sheep.png、
 * animations/mimi_sheep.animation.json（动画键为裸名 idle / walk / scared_run）。</p>
 *
 * <p>生物逻辑对齐原版羊（Sheep）的友好生物部分：
 * 水上漂浮、受击惊慌逃跑（PanicGoal，加速 1.25）、避水随机游荡、看向玩家与四处张望；
 * 音效同样使用原版羊叫声。按需求【不注册吃草 AI】（EatBlockGoal），
 * 因此米羊羊不会低头啃食草方块，也没有繁殖/引诱相关 AI。</p>
 */
public class MimiSheepEntity extends PathfinderMob implements GeoEntity {

    // GeckoLib 要求每个可动画对象持有一个动画实例缓存（按实体实例保存动画进度/状态）
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    // 待机动画：对应 mimi_sheep.animation.json 里的 "idle"（loop=true）
    private static final RawAnimation ANIM_IDLE = RawAnimation.begin().thenLoop("idle");
    // 行走动画：对应 "walk"（loop=true，四条腿交替摆动）
    private static final RawAnimation ANIM_WALK = RawAnimation.begin().thenLoop("walk");
    // 受击逃跑动画：对应 "scared_run"（单次播放，惊慌逃跑期间控制器会持续重新触发）
    private static final RawAnimation ANIM_SCARED_RUN = RawAnimation.begin().thenPlay("scared_run");

    public MimiSheepEntity(EntityType<? extends PathfinderMob> type, Level level) {
        super(type, level);
    }

    /**
     * 实体属性：与原版羊一致——8 心（16 点血）、基础移速 0.23。
     * 在 ModEntityAttributes 的 EntityAttributeCreationEvent 中注册。
     */
    public static AttributeSupplier.Builder createAttributes() {
        return PathfinderMob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 16.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.23D);
    }

    /**
     * AI 目标（优先级数值沿用原版羊的排布，便于以后补繁殖/引诱时直接插回 2~5 位）：
     * 0 水上漂浮；1 受击/着火惊慌逃跑；6 避水随机游荡；7 看向玩家；8 四处张望。
     * 故意不注册原版羊的 EatBlockGoal（吃草）。
     */
    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new PanicGoal(this, 1.25D));
        this.goalSelector.addGoal(6, new WaterAvoidingRandomStrollGoal(this, 1.0D));
        this.goalSelector.addGoal(7, new LookAtPlayerGoal(this, Player.class, 6.0F));
        this.goalSelector.addGoal(8, new RandomLookAroundGoal(this));
    }

    // —— 音效：直接使用原版羊的叫声（受击/死亡也是同组音效）——

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.SHEEP_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.SHEEP_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.SHEEP_DEATH;
    }

    /**
     * 注册 GeckoLib 动画控制器。
     * 控制器名 "main"，过渡时间 5 tick：受击惊慌 → scared_run，普通移动 → walk，站立 → idle。
     */
    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "main", 5, this::handleAnimState));
    }

    /**
     * 动画状态判断：
     * PanicGoal 运行中（被攻击或着火逃跑）播 scared_run；
     * 否则在地面移动播 walk；其余情况播 idle。
     *
     * 注意：1.20.1 的 Mob 还没有 isPanicking()（1.20.2 才加入），
     * 这里直接扫描正在运行的目标里是否含 PanicGoal，与原版惊慌状态判定等价。
     */
    private boolean panicGoalRunning() {
        return this.goalSelector.getRunningGoals().anyMatch(wrapped -> wrapped.getGoal() instanceof PanicGoal);
    }

    private PlayState handleAnimState(AnimationState<MimiSheepEntity> state) {
        if (panicGoalRunning()) {
            state.setAndContinue(ANIM_SCARED_RUN);
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
}
