package com.mimiworld.mimiworld.block;

import com.mimiworld.mimiworld.block.entity.IronWokBlockEntity;
import com.mimiworld.mimiworld.block.entity.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

/**
 * 铁锅（iron_wok）：4 个输入槽呈菱形布置 + 右上单独输入 + 燃料 + 输出的熔炉式炒锅。
 * 燃料/进度逻辑与网格煮锅基类完全一致（4 输入即基类 2x2 的 4 格，仅 GUI 摆放为菱形）。
 */
public class IronWokBlock extends GridCookingPotBlock {

    public IronWokBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected BlockEntity createMachineBlockEntity(BlockPos pos, BlockState state) {
        return new IronWokBlockEntity(pos, state);
    }

    @Override
    protected BlockEntityType blockEntityType() {
        return ModBlockEntities.IRON_WOK.get();
    }
}
