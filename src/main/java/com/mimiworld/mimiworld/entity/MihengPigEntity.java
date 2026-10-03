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
 * 米哼猪（MiHeng Pig）——占位实体转正后的真实 GeckoLib 实体。
 *
 * <p>继承 {@link PlaceholderRiceMobEntity} 的友好生物 AI（水上漂浮、避水随机游荡、
 * 看向玩家与四处张望），音效使用原版猪叫声；暂不注册繁殖 AI。</p>
 *
 * <p>模型/贴图/动画资源（均由 Blockbench 导出）：
 * geo/mihengzhu.geo.json、textures/entity/mihengzhu.png、
 * animations/mihengzhu.animation.json（动画键为 animation.mihengzhu.idle /
 * animation.mihengzhu.walk，大米主题米白毛色+米粉腮红+稻芽绿卷尾）。</p>
 */
public class MihengPigEntity extends PlaceholderRiceMobEntity implements GeoEntity {

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    // 待机动画：4 秒循环（呼吸+东张西望+好奇歪头+卷尾抽动）
    private static final RawAnimation ANIM_IDLE =
            RawAnimation.begin().thenLoop("animation.mihengzhu.idle");
    // 行走动画：2 秒循环（对角步态+胖猪摇摆+点头+卷尾快摇）
    private static final RawAnimation ANIM_WALK =
            RawAnimation.begin().thenLoop("animation.mihengzhu.walk");

    public MihengPigEntity(EntityType<? extends net.minecraft.world.entity.PathfinderMob> type, Level level) {
        super(type, level);
    }

    /**
     * 实体属性：10 心（20 点血）、基础移速 0.25（对齐原版猪）。
     * 在 ModEntityAttributes 的 EntityAttributeCreationEvent 中注册。
     */
    public static AttributeSupplier.Builder createAttributes() {
        return PlaceholderRiceMobEntity.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 20.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.25D);
    }

    // —— 音效：直接使用原版猪的叫声 ——

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.PIG_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.PIG_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.PIG_DEATH;
    }

    /**
     * 注册 GeckoLib 动画控制器。
     * 控制器名 "main"，过渡时间 5 tick：移动时播 walk，站立时播 idle。
     */
    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "main", 5, this::handleAnimState));
    }

    private PlayState handleAnimState(AnimationState<MihengPigEntity> state) {
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
