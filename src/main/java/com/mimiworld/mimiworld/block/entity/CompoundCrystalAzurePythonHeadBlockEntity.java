package com.mimiworld.mimiworld.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import software.bernie.geckolib.animatable.GeoBlockEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animatable.instance.SingletonAnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;

/**
 * 复晶天青蟒头方块实体（compound_crystal_azure_python_head）。
 *
 * <p>纯装饰 GeckoLib 方块：不注册任何动画控制器，geo 模型以静态姿态渲染
 * （geometry.compound_crystal_azure_python_head：head + jaw + tongue + horn 共 17 方块，
 * 贴图 textures/geckolib/compound_crystal_azure_python_head.png）。无 GUI、不存数据、不 tick。</p>
 */
public class CompoundCrystalAzurePythonHeadBlockEntity extends BlockEntity implements GeoBlockEntity {

    private final AnimatableInstanceCache geoCache = new SingletonAnimatableInstanceCache(this);

    public CompoundCrystalAzurePythonHeadBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.COMPOUND_CRYSTAL_AZURE_PYTHON_HEAD.get(), pos, state);
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        // 静态摆件，无动画
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return geoCache;
    }
}
