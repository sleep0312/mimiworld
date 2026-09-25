package com.mimiworld.mimiworld.block;

import com.mimiworld.mimiworld.block.entity.ModBlockEntities;
import com.mimiworld.mimiworld.block.entity.SwordForgingTableBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

/**
 * 铸剑台（编码 130 = sword_forging_table）：绑定 SwordForgingTableBlockEntity，
 * 右键打开与原版工作台外观一致的 3x3 即时合成 GUI，配方表独立。
 */
public class SwordForgingTableBlock extends ForgingTableBlock {

    public SwordForgingTableBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected BlockEntity createMachineBlockEntity(BlockPos pos, BlockState state) {
        return new SwordForgingTableBlockEntity(pos, state);
    }

    @Override
    @SuppressWarnings("rawtypes")
    protected BlockEntityType blockEntityType() {
        return ModBlockEntities.SWORD_FORGING_TABLE.get();
    }
}
