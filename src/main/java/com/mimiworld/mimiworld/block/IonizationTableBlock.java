package com.mimiworld.mimiworld.block;

import com.mimiworld.mimiworld.block.entity.IonizationTableBlockEntity;
import com.mimiworld.mimiworld.block.entity.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

/**
 * 电离台（ionization_table）：绑定 IonizationTableBlockEntity 的机器方块，
 * 右键打开双输入定时加工 GUI（无燃料）。
 */
public class IonizationTableBlock extends DualInputProcessorBlock {

    public IonizationTableBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected BlockEntity createMachineBlockEntity(BlockPos pos, BlockState state) {
        return new IonizationTableBlockEntity(pos, state);
    }

    @Override
    @SuppressWarnings("rawtypes")
    protected BlockEntityType blockEntityType() {
        return ModBlockEntities.IONIZATION_TABLE.get();
    }
}
