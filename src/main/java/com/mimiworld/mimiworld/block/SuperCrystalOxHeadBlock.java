package com.mimiworld.mimiworld.block;

import com.mimiworld.mimiworld.block.entity.SuperCrystalOxHeadBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

/**
 * 超晶牛头（super_crystal_ox_head）：超晶牛王掉落的头颅摆件方块。
 *
 * <p>世界外观由 GeckoLib BlockEntityRenderer 渲染
 * （geo/block/super_crystal_ox_head.geo.json：牛头+双角+双耳），
 * 方块自身 JSON 模型不渲染（{@link RenderShape#ENTITYBLOCK_ANIMATED}），
 * 仅提供破坏粒子纹理；物品栏图标是同款 3D 元素模型。</p>
 *
 * <p>无 GUI、无动画、空手可破坏（硬度 1.0），破坏掉落本身。</p>
 */
public class SuperCrystalOxHeadBlock extends Block implements EntityBlock {

    /** BER 缩放 0.72 后：宽约 0.99、高 0.72、前后约 0.72，居中贴地。 */
    private static final VoxelShape SHAPE = Shapes.box(0.01, 0.0, 0.14, 0.99, 0.72, 0.86);

    public SuperCrystalOxHeadBlock(Properties properties) {
        super(properties);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.ENTITYBLOCK_ANIMATED;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new SuperCrystalOxHeadBlockEntity(pos, state);
    }
}
