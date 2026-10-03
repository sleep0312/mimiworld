package com.mimiworld.mimiworld.block;

import com.mimiworld.mimiworld.block.entity.ModBlockEntities;
import com.mimiworld.mimiworld.block.entity.JumboMegaCookingPotBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

/**
 * 巨无霸大大大煮锅（jumbo_mega_cooking_pot）：5x5 共 25 个网格输入槽的熔炉式燃料煮锅。
 */
public class JumboMegaCookingPotBlock extends GeckoCookingPotBlock {

    public JumboMegaCookingPotBlock(Properties properties) {
        super(properties);
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
