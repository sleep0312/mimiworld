package com.mimiworld.mimiworld.block;

import com.mimiworld.mimiworld.block.entity.ModBlockEntities;
import com.mimiworld.mimiworld.block.entity.LargeCookingPotBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * 大煮锅（large_cooking_pot）：3x3 共 9 个网格输入槽的熔炉式燃料煮锅。
 * 碰撞箱/选框按渲染后的模型高度（10.5px）生成，与堕落十字架做法一致。
 */
public class LargeCookingPotBlock extends GeckoCookingPotBlock {

    /** 渲染后模型高 10.5px，按 12.8px 基准模型等比缩放。 */
    private static final VoxelShape SHAPE = CookingPotShapes.forRenderedHeight(10.5);

    public LargeCookingPotBlock(Properties properties) {
        super(properties);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    protected BlockEntity createMachineBlockEntity(BlockPos pos, BlockState state) {
        return new LargeCookingPotBlockEntity(pos, state);
    }

    @Override
    protected BlockEntityType blockEntityType() {
        return ModBlockEntities.LARGE_COOKING_POT.get();
    }
}
