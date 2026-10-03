package com.mimiworld.mimiworld.entity;

import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
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
 * 米哞牛（MiMoo Cow）——占位实体转正后的真实 GeckoLib 实体。
 *
 * <p>继承 {@link PlaceholderRiceMobEntity} 的友好生物 AI（水上漂浮、避水随机游荡、
 * 看向玩家与四处张望），音效使用原版牛叫声；暂不注册繁殖/挤奶/被攻击 AI。</p>
 *
 * <p>模型/贴图/动画资源（均由 Blockbench 导出）：
 * geo/mimouniu.geo.json、textures/entity/mimouniu.png、
 * animations/mimouniu.animation.json（动画键为 animation.mimouniu.idle /
 * animation.mimouniu.walk，大米主题米白毛色+稻穗尾巴）。</p>
 */
public class MimouniuEntity extends PlaceholderRiceMobEntity implements GeoEntity {

    // GeckoLib 要求每个可动画对象持有一个动画实例缓存（按实体实例保存动画进度/状态）
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    // 待机动画：4 秒循环（呼吸+抬头张望+歪头+甩尾）
    private static final RawAnimation ANIM_IDLE =
            RawAnimation.begin().thenLoop("animation.mimouniu.idle");
    // 行走动画：2 秒循环（对角步态+身体起伏+头尾联动）
    private static final RawAnimation ANIM_WALK =
            RawAnimation.begin().thenLoop("animation.mimouniu.walk");

    public MimouniuEntity(EntityType<? extends net.minecraft.world.entity.PathfinderMob> type, Level level) {
        super(type, level);
    }

    /**
     * 实体属性：10 心（20 点血）、基础移速 0.2（对齐原版牛）。
     * 在 ModEntityAttributes 的 EntityAttributeCreationEvent 中注册。
     */
    public static AttributeSupplier.Builder createAttributes() {
        return PlaceholderRiceMobEntity.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 20.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.2D);
    }

    // —— 音效：直接使用原版牛的叫声 ——

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.COW_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.COW_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.COW_DEATH;
    }

    /**
     * 注册 GeckoLib 动画控制器。
     * 控制器名 "main"，过渡时间 5 tick：移动时播 walk，站立时播 idle。
     */
    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "main", 5, this::handleAnimState));
    }

    private PlayState handleAnimState(AnimationState<MimouniuEntity> state) {
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
