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
 * 嗥米狼（Haomi Wolf）——占位实体转正后的真实 GeckoLib 实体。
 *
 * <p>模型/贴图/动画资源（均由 Blockbench 导出）：
 * geo/haomi_wolf.geo.json、textures/entity/haomi_wolf.png、
 * animations/haomi_wolf.animation.json（动画键为 animation.idle / animation.walk）。
 * 模型为四足狼形（8 根骨骼：root/body/head/四条腿/tail），大米主题：
 * 狼灰体毛、米白胸腹、稻草金颈鬃与稻穗尾、稻绿双眼。</p>
 *
 * <p>生物逻辑为友好生物（对齐原版狼的温和部分）：水上漂浮、受击惊慌逃跑、
 * 避水随机游荡、看向玩家与四处张望；音效使用原版狼叫声。
 * 暂不注册驯服/繁殖/捕食/攻击玩家相关 AI。</p>
 */
public class HaomiWolfEntity extends PathfinderMob implements GeoEntity {

    // GeckoLib 要求每个可动画对象持有一个动画实例缓存（按实体实例保存动画进度/状态）
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    // 待机动画：对应 haomi_wolf.animation.json 里的 "animation.idle"（loop=true，4 秒，呼吸+张望+摇尾）
    private static final RawAnimation ANIM_IDLE = RawAnimation.begin().thenLoop("animation.idle");
    // 行走动画：对应 "animation.walk"（loop=true，1 秒，四足对角步态+身体起伏+头尾随动）
    private static final RawAnimation ANIM_WALK = RawAnimation.begin().thenLoop("animation.walk");

    public HaomiWolfEntity(EntityType<? extends PathfinderMob> type, Level level) {
        super(type, level);
    }

    /**
     * 实体属性：20 心（40 点血，用户设定）、基础移速 0.3（对齐原版狼）。
     * 在 ModEntityAttributes 的 EntityAttributeCreationEvent 中注册。
     */
    public static AttributeSupplier.Builder createAttributes() {
        return PathfinderMob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 40.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.3D);
    }

    /**
     * AI 目标：0 水上漂浮；1 受击/着火惊慌逃跑；6 避水随机游荡；7 看向玩家；8 四处张望。
     */
    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new PanicGoal(this, 1.5D));
        this.goalSelector.addGoal(6, new WaterAvoidingRandomStrollGoal(this, 1.0D));
        this.goalSelector.addGoal(7, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(8, new RandomLookAroundGoal(this));
    }

    // —— 音效：直接使用原版狼的叫声 ——

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.WOLF_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.WOLF_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.WOLF_DEATH;
    }

    /**
     * 注册 GeckoLib 动画控制器。
     * 控制器名 "main"，过渡时间 4 tick：移动（含惊慌逃跑）播 walk，站立播 idle。
     */
    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "main", 4, this::handleAnimState));
    }

    private PlayState handleAnimState(AnimationState<HaomiWolfEntity> state) {
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
}
