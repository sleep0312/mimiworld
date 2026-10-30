package com.mimiworld.mimiworld.block;

import com.mimiworld.mimiworld.block.entity.ModBlockEntities;
import com.mimiworld.mimiworld.block.entity.JumboMegaCookingPotBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * 巨无霸大大大煮锅（jumbo_mega_cooking_pot）：5x5 共 25 个网格输入槽的熔炉式燃料煮锅。
 * 碰撞箱/选框按渲染后的模型高度（16px，正好占满一格）生成，与堕落十字架做法一致。
 */
public class JumboMegaCookingPotBlock extends GeckoCookingPotBlock {

    /** 渲染后模型高 16px，按 12.8px 基准模型等比缩放。 */
    private static final VoxelShape SHAPE = CookingPotShapes.forRenderedHeight(16.0);

    public JumboMegaCookingPotBlock(Properties properties) {
        super(properties);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    protected BlockEntity createMachineBlockEntity(BlockPos pos, BlockState state) {
        return new JumboMegaCookingPotBlockEntity(pos, state);
    }

    @Override
    protected BlockEntityType blockEntityType() {
        return ModBlockEntities.JUMBO_MEGA_COOKING_POT.get();
    }
}
