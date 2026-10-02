package com.mimiworld.mimiworld.block;

import com.mimiworld.mimiworld.block.entity.ModBlockEntities;
import com.mimiworld.mimiworld.block.entity.PestleRealmTableBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

/**
 * 杵界台（pestle_realm_table）：绑定 PestleRealmTableBlockEntity，
 * 右键打开 2x2 网格（4 输入）+ 右下输出槽的工作台式即时合成 GUI，配方表独立。
 */
public class PestleRealmTableBlock extends GridCraftingTableBlock {

    public PestleRealmTableBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected BlockEntity createMachineBlockEntity(BlockPos pos, BlockState state) {
        return new PestleRealmTableBlockEntity(pos, state);
    }

    @Override
    @SuppressWarnings("rawtypes")
    protected BlockEntityType blockEntityType() {
        return ModBlockEntities.PESTLE_REALM_TABLE.get();
    }
}
