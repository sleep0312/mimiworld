package com.mimiworld.mimiworld.block;

import com.mimiworld.mimiworld.block.entity.DibazongzuiHeadBlockEntity;
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
 * 第八宗罪龙头（dibazongzui_head）：第八宗罪龙掉落的头颅摆件方块。
 *
 * <p>世界外观由 GeckoLib BlockEntityRenderer 渲染
 * （geo/block/dibazongzui_head.geo.json：龙头+下颌共 99 方块，红晶龙首/巨角/獠牙），
 * 方块自身 JSON 模型不渲染（{@link RenderShape#ENTITYBLOCK_ANIMATED}），
 * 仅提供破坏粒子纹理；物品栏图标是同款 3D 元素模型。</p>
 *
 * <p>无 GUI、无动画、空手可破坏（硬度 1.0），破坏掉落本身。</p>
 */
public class DibazongzuiHeadBlock extends Block implements EntityBlock {

    /** 龙头包围盒 72×97.2×150px，BER 缩放 16/150 后：宽约 0.48、高 0.65、前后占满 1.0。 */
    private static final VoxelShape SHAPE = Shapes.box(0.26, 0.0, 0.0, 0.74, 0.65, 1.0);

    public DibazongzuiHeadBlock(Properties properties) {
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
        return new DibazongzuiHeadBlockEntity(pos, state);
    }
}
