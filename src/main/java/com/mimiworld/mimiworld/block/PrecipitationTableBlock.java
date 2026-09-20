package com.mimiworld.mimiworld.block;

import com.mimiworld.mimiworld.block.entity.ModBlockEntities;
import com.mimiworld.mimiworld.block.entity.PrecipitationTableBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

/**
 * 沉淀台（编码 57 = precipitation_table）：绑定 PrecipitationTableBlockEntity 的机器方块，
 * 右键打开双输入定时加工 GUI（无燃料）。
 */
public class PrecipitationTableBlock extends DualInputProcessorBlock {

    public PrecipitationTableBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected BlockEntity createMachineBlockEntity(BlockPos pos, BlockState state) {
        return new PrecipitationTableBlockEntity(pos, state);
    }

    @Override
    @SuppressWarnings("rawtypes")
    protected BlockEntityType blockEntityType() {
        return ModBlockEntities.PRECIPITATION_TABLE.get();
    }
}
