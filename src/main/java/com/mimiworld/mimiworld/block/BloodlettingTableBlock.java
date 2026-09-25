package com.mimiworld.mimiworld.block;

import com.mimiworld.mimiworld.block.entity.BloodlettingTableBlockEntity;
import com.mimiworld.mimiworld.block.entity.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

/**
 * 汲血台（编码 166 = bloodletting_table）：绑定 BloodlettingTableBlockEntity，
 * 右键打开双输入熔炉式加工 GUI（左列两输入槽 → 右侧输出槽）。
 */
public class BloodlettingTableBlock extends MultiInputProcessorBlock {

    public BloodlettingTableBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected BlockEntity createMachineBlockEntity(BlockPos pos, BlockState state) {
        return new BloodlettingTableBlockEntity(pos, state);
    }

    @Override
    @SuppressWarnings("rawtypes")
    protected BlockEntityType blockEntityType() {
        return ModBlockEntities.BLOODLETTING_TABLE.get();
    }
}
