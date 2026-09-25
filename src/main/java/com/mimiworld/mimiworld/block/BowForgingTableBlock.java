package com.mimiworld.mimiworld.block;

import com.mimiworld.mimiworld.block.entity.BowForgingTableBlockEntity;
import com.mimiworld.mimiworld.block.entity.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

/**
 * 铸弓台（编码 132 = bow_forging_table）：绑定 BowForgingTableBlockEntity，
 * 右键打开与原版工作台外观一致的 3x3 即时合成 GUI，配方表独立。
 */
public class BowForgingTableBlock extends ForgingTableBlock {

    public BowForgingTableBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected BlockEntity createMachineBlockEntity(BlockPos pos, BlockState state) {
        return new BowForgingTableBlockEntity(pos, state);
    }

    @Override
    @SuppressWarnings("rawtypes")
    protected BlockEntityType blockEntityType() {
        return ModBlockEntities.BOW_FORGING_TABLE.get();
    }
}
