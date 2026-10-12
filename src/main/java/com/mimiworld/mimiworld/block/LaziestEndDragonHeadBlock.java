package com.mimiworld.mimiworld.block;

import com.mimiworld.mimiworld.block.entity.LaziestEndDragonHeadBlockEntity;
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
 * 最懒终末龙头（laziest_end_dragon_head）：最懒终末龙掉落的头颅摆件方块。
 *
 * <p>世界外观由 GeckoLib BlockEntityRenderer 渲染
 * （geo/block/laziest_end_dragon_head.geo.json：Promax 龙头+下颌，双角/獠牙/紫眼），
 * 方块自身 JSON 模型不渲染（{@link RenderShape#ENTITYBLOCK_ANIMATED}），
 * 仅提供破坏粒子纹理；物品栏图标是同款 3D 元素模型。</p>
 *
 * <p>无 GUI、无动画、空手可破坏（硬度 1.0），破坏掉落本身。</p>
 */
public class LaziestEndDragonHeadBlock extends Block implements EntityBlock {

    /** 龙头实体子树极长（100px 吻部），BER 缩放 0.16 后：宽约 0.28、高 0.54、前后占满 1.0。 */
    private static final VoxelShape SHAPE = Shapes.box(0.36, 0.0, 0.0, 0.64, 0.54, 1.0);

    public LaziestEndDragonHeadBlock(Properties properties) {
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
        return new LaziestEndDragonHeadBlockEntity(pos, state);
    }
}
