package com.mimiworld.mimiworld.entity;

import com.mimiworld.mimiworld.ModItems;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.AbstractFish;
import net.minecraft.world.item.ItemStack;
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
 * 喷米鱼（Penmi Fish）：水生生物。
 * <p>
 * 与陆地占位生物不同，继承原版 {@link AbstractFish}，因此自带：
 * 水下随机游泳 AI、离水扑腾、用水桶可装鱼/倒出鱼的交互。
 * 生命 4 心（8 HP），游泳速度与原版鳕鱼一致（1.0）。
 */
public class PenmiFishEntity extends AbstractFish implements GeoEntity {
    private static final RawAnimation ANIM_SWIM = RawAnimation.begin().thenLoop("animation.penmiyu.swim");
    private static final RawAnimation ANIM_IDLE = RawAnimation.begin().thenLoop("animation.penmiyu.idle");

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    public PenmiFishEntity(EntityType<? extends AbstractFish> entityType, Level level) {
        super(entityType, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return AbstractFish.createAttributes()
                .add(Attributes.MAX_HEALTH, 8.0D)
                .add(Attributes.MOVEMENT_SPEED, 1.0D);
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.COD_AMBIENT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.COD_DEATH;
    }

    @Override
    protected SoundEvent getHurtSound(net.minecraft.world.damagesource.DamageSource source) {
        return SoundEvents.COD_HURT;
    }

    @Override
    protected SoundEvent getFlopSound() {
        return SoundEvents.COD_FLOP;
    }

    @Override
    public ItemStack getBucketItemStack() {
        return new ItemStack(ModItems.PENMI_FISH_BUCKET.get());
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "main", 5, this::handleAnimState));
    }

    private PlayState handleAnimState(AnimationState<PenmiFishEntity> state) {
        // 水中游动时播放游泳动画，静止（悬浮/离水不动）时播放 idle
        boolean swimming = state.isMoving() || this.getDeltaMovement().horizontalDistanceSqr() > 1.0E-4D;
        state.setAndContinue(swimming ? ANIM_SWIM : ANIM_IDLE);
        return PlayState.CONTINUE;
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }
}
