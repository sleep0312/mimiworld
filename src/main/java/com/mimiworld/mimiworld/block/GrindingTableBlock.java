package com.mimiworld.mimiworld.block;

import com.mimiworld.mimiworld.block.entity.GrindingTableBlockEntity;
import com.mimiworld.mimiworld.block.entity.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

/**
 * 碾磨台（grinding_table）：绑定 GrindingTableBlockEntity，
 * 右键打开六输入熔炉式加工 GUI（左右两列各三个输入槽，底部中央输出；面板加高）。
 */
public class GrindingTableBlock extends MultiInputProcessorBlock {

    public GrindingTableBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected BlockEntity createMachineBlockEntity(BlockPos pos, BlockState state) {
        return new GrindingTableBlockEntity(pos, state);
    }

    @Override
    @SuppressWarnings("rawtypes")
    protected BlockEntityType blockEntityType() {
        return ModBlockEntities.GRINDING_TABLE.get();
    }
}
