package com.mimiworld.mimiworld.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import software.bernie.geckolib.animatable.GeoBlockEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animatable.instance.SingletonAnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;

/**
 * 饭晶虎王头方块实体（rice_crystal_tiger_king_head）。
 *
 * <p>纯装饰 GeckoLib 方块：不注册任何动画控制器，geo 模型以静态姿态渲染
 * （geometry.fanjinghuwang_head：head + ear_left/right + crown + jaw 共 15 方块，
 * 贴图 textures/geckolib/rice_crystal_tiger_king_head.png）。无 GUI、不存数据、不 tick。</p>
 */
public class RiceCrystalTigerKingHeadBlockEntity extends BlockEntity implements GeoBlockEntity {

    private final AnimatableInstanceCache geoCache = new SingletonAnimatableInstanceCache(this);

    public RiceCrystalTigerKingHeadBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.RICE_CRYSTAL_TIGER_KING_HEAD.get(), pos, state);
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
