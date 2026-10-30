package com.mimiworld.mimiworld.block;

import com.mimiworld.mimiworld.block.entity.ModBlockEntities;
import com.mimiworld.mimiworld.block.entity.CookingPotBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * 煮锅（cooking_pot）：2x2 共 4 个网格输入槽的熔炉式燃料煮锅。
 * 碰撞箱/选框按渲染后的模型高度（8px）生成，与堕落十字架做法一致。
 */
public class CookingPotMachineBlock extends GeckoCookingPotBlock {

    /** 渲染后模型高 8px，按 12.8px 基准模型等比缩放。 */
    private static final VoxelShape SHAPE = CookingPotShapes.forRenderedHeight(8.0);

    public CookingPotMachineBlock(Properties properties) {
        super(properties);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    protected BlockEntity createMachineBlockEntity(BlockPos pos, BlockState state) {
        return new CookingPotBlockEntity(pos, state);
    }

    @Override
    protected BlockEntityType blockEntityType() {
        return ModBlockEntities.COOKING_POT.get();
    }
}
