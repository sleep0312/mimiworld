package com.mimiworld.mimiworld.block;

import com.mimiworld.mimiworld.block.entity.IngotCondensationTableBlockEntity;
import com.mimiworld.mimiworld.block.entity.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

/**
 * 凝锭台（编码 292 = ingot_condensation_table）：绑定 IngotCondensationTableBlockEntity，
 * 右键打开 5x5 网格（25 输入）+ 下方中央输出槽的工作台式即时合成 GUI（面板加高），
 * 配方表独立。
 */
public class IngotCondensationTableBlock extends GridCraftingTableBlock {

    public IngotCondensationTableBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected BlockEntity createMachineBlockEntity(BlockPos pos, BlockState state) {
        return new IngotCondensationTableBlockEntity(pos, state);
    }

    @Override
    @SuppressWarnings("rawtypes")
    protected BlockEntityType blockEntityType() {
        return ModBlockEntities.INGOT_CONDENSATION_TABLE.get();
    }
}
