package com.mimiworld.mimiworld.entity;

import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
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
 * 米傀儡（Rice Golem）——占位实体转正后的真实 GeckoLib 实体。
 *
 * <p>继承 {@link PlaceholderRiceMobEntity} 的友好生物 AI（水上漂浮、避水随机游荡、
 * 看向玩家与四处张望）；体型/血量对齐原版铁傀儡（碰撞箱 1.4×2.7、100 心），
 * 音效沿用原版铁傀儡；作为友好生物暂不挂载攻击 AI。</p>
 *
 * <p>模型/贴图/动画资源（均由 Blockbench 导出）：
 * geo/rice_golem.geo.json、textures/entity/rice_golem.png、
 * animations/rice_golem.animation.json（动画键为 animation.rice_golem.idle /
 * animation.rice_golem.walk，稻草躯干+麻绳束带+米白拳套的大米主题造型）。</p>
 */
public class RiceGolemEntity extends PlaceholderRiceMobEntity implements GeoEntity {

    // GeckoLib 要求每个可动画对象持有一个动画实例缓存（按实体实例保存动画进度/状态）
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    // 待机动画：4 秒循环（呼吸+环顾）
    private static final RawAnimation ANIM_IDLE =
            RawAnimation.begin().thenLoop("animation.rice_golem.idle");
    // 行走动画：2 秒循环（双腿前后摆动 ±25°、双臂摆动 ±12°、身体起伏）
    private static final RawAnimation ANIM_WALK =
            RawAnimation.begin().thenLoop("animation.rice_golem.walk");

    public RiceGolemEntity(EntityType<? extends PathfinderMob> type, Level level) {
        super(type, level);
    }

    /**
     * 实体属性：100 心（200 点血）、基础移速 0.25、满击退抗性，均对齐原版铁傀儡。
     * 在 ModEntityAttributes 的 EntityAttributeCreationEvent 中注册。
     */
    public static AttributeSupplier.Builder createAttributes() {
        return PlaceholderRiceMobEntity.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 200.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.25D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 1.0D);
    }

    // —— 音效：使用原版铁傀儡的受击/死亡声；铁傀儡原版无待机叫声，ambient 沿用基类 null ——

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.IRON_GOLEM_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.IRON_GOLEM_DEATH;
    }

    /**
     * 注册 GeckoLib 动画控制器。
     * 控制器名 "main"，过渡时间 5 tick：移动时播 walk，站立时播 idle。
     */
    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "main", 5, this::handleAnimState));
    }

    private PlayState handleAnimState(AnimationState<RiceGolemEntity> state) {
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
