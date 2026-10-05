package com.mimiworld.mimiworld.entity;

import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.EnderMan;
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
 * 没米入（Meimiru，注册 id = momi_entrant）——大米主题的末影系生物，比原版末影人更优雅。
 *
 * <p>模型/贴图/动画资源（均由 Blockbench 导出）：
 * geo/meimiru.geo.json、textures/entity/meimiru.png、
 * animations/meimiru.animation.json（动画键 animation.idle / animation.walk / animation.attack）。
 * 模型为 51 方块 7 骨骼的修长末影人形（实测约 34.5px 高），末影暗紫本体配米白骨角/爪/发须/飘带、
 * 淡金稻穗装饰、发光淡紫眼；渲染时整体放大 1.15 倍撑满 2.5 格碰撞箱。</p>
 *
 * <p>生物逻辑：直接继承原版 {@link EnderMan}——平时中立游荡，会随机瞬移；被玩家盯着看或受击后激怒
 * （universal anger），近战扑击玩家；接触水/雨会受伤并瞬移逃离；死亡掉落沿用末影珍珠（Yanmi 米珠）。
 * 原版末影人的音效（ambient/stare/scream/hurt/death/teleport）、搬方块行为一并继承。
 * 75 心（150 点血，沿用占位数值），攻击力 7、移速 0.3 等其余属性与原版末影人一致。</p>
 */
public class MeimiruEntity extends EnderMan implements GeoEntity {

    /**
     * 同步数据：攻击动画剩余 tick（服务端命中时置 18 并逐 tick 递减，自动同步到客户端；
     * GeckoLib 动画在客户端播放，必须用 SynchedEntityData 而不能用普通字段）。
     * 做法与黏米之蛛/糊米古尸一致。
     */
    private static final EntityDataAccessor<Integer> DATA_ATTACK_ANIM_TICKS =
            SynchedEntityData.defineId(MeimiruEntity.class, EntityDataSerializers.INT);

    // GeckoLib：每个实体实例持有一个动画状态缓存
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    // 待机：meimiru.animation.json 中的 "animation.idle"（loop=true，4 秒，呼吸+歪头张望+双臂微浮）
    private static final RawAnimation ANIM_IDLE = RawAnimation.begin().thenLoop("animation.idle");
    // 行走："animation.walk"（loop=true，1 秒，高个长腿大迈步+手臂克制摆动）
    private static final RawAnimation ANIM_WALK = RawAnimation.begin().thenLoop("animation.walk");
    // 攻击："animation.attack"（不循环，单次播放 0.9 秒，缩身扬爪→双爪水平前刺扑击→撕拉→收回）
    private static final RawAnimation ANIM_ATTACK = RawAnimation.begin().thenPlay("animation.attack");

    /** attack 动画时长 0.9 秒 = 18 tick */
    private static final int ATTACK_ANIMATION_TICKS = 18;

    public MeimiruEntity(EntityType<? extends MeimiruEntity> type, Level level) {
        super(type, level);
    }

    /**
     * 属性：在原版末影人基础上把血量提到 75 心 = 150 血（沿用占位数值）。
     * 攻击力 7、移速 0.3、最大追踪距离等其余属性与原版末影人完全一致。
     * 在 ModEntityAttributes 的 EntityAttributeCreationEvent 中注册。
     */
    public static AttributeSupplier.Builder createAttributes() {
        return EnderMan.createAttributes()
                .add(Attributes.MAX_HEALTH, 150.0D);
    }

    // -------------------- GeckoLib 动画 --------------------
    //
    // AI（盯视索敌、受击反击、近战扑击、随机/仇恨瞬移、避水游荡、张望）、
    // 愤怒持久化、怕水受伤、搬方块、音效全部继承 EnderMan，这里不覆写 registerGoals。

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        // 过渡时间 5 tick（与项目内其他 GeckoLib 实体一致）
        controllers.add(new AnimationController<>(this, "main", 5, this::handleAnimState));
    }

    /**
     * 动画状态：命中后的 18 tick 内播单次 attack；否则移动播 walk、静止播 idle。
     * setAndContinue 保证同名动画不会被逐帧重启，attack 单次播完自然切回。
     */
    private PlayState handleAnimState(AnimationState<MeimiruEntity> state) {
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
}
