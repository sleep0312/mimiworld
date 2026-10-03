package com.mimiworld.mimiworld.block.entity;

import software.bernie.geckolib.animatable.GeoBlockEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animatable.instance.SingletonAnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;

/**
 * 使用 GeckoLib geo 模型渲染的网格煮锅方块实体中间基类。
 *
 * <p>仅煮锅/大煮锅/超大煮锅/巨无霸大大大煮锅的 BlockEntity 继承本类；
 * 烤箱、铁锅的 BlockEntity 继续使用 {@link GridCookingPotBlockEntity}，不受影响。</p>
 *
 * <p>本系列煮锅无动画，geo 模型以静态姿态渲染。</p>
 */
public abstract class GeckoCookingPotBlockEntity extends GridCookingPotBlockEntity
        implements GeoBlockEntity {

    private final AnimatableInstanceCache geoCache = new SingletonAnimatableInstanceCache(this);

    protected GeckoCookingPotBlockEntity(net.minecraft.world.level.block.entity.BlockEntityType<?> type,
                                         net.minecraft.core.BlockPos pos,
                                         net.minecraft.world.level.block.state.BlockState state,
                                         int gridSize) {
        super(type, pos, state, gridSize);
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        // 不注册任何动画控制器：geo 模型保持静态
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return geoCache;
    }
}
