package com.mimiworld.mimiworld.block;

import com.mimiworld.mimiworld.block.entity.ModBlockEntities;
import com.mimiworld.mimiworld.block.entity.ExtraLargeCookingPotBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

/**
 * 超大煮锅（extra_large_cooking_pot）：4x4 共 16 个网格输入槽的熔炉式燃料煮锅。
 */
public class ExtraLargeCookingPotBlock extends GeckoCookingPotBlock {

    public ExtraLargeCookingPotBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected BlockEntity createMachineBlockEntity(BlockPos pos, BlockState state) {
        return new ExtraLargeCookingPotBlockEntity(pos, state);
    }

    @Override
    protected BlockEntityType blockEntityType() {
        return ModBlockEntities.EXTRA_LARGE_COOKING_POT.get();
    }
}
