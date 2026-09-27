package com.mimiworld.mimiworld.block;

import com.mimiworld.mimiworld.block.entity.ModBlockEntities;
import com.mimiworld.mimiworld.block.entity.OvenBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

/**
 * 烤箱（编码 373 = oven）：GUI 与大煮锅完全相同（3x3 共 9 个网格输入槽 + 右上输入 + 燃料 + 输出），
 * 但配方表独立、与大煮锅互不通用。
 */
public class OvenBlock extends GridCookingPotBlock {

    public OvenBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected BlockEntity createMachineBlockEntity(BlockPos pos, BlockState state) {
        return new OvenBlockEntity(pos, state);
    }

    @Override
    protected BlockEntityType blockEntityType() {
        return ModBlockEntities.OVEN.get();
    }
}
