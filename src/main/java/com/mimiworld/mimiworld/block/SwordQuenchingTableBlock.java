package com.mimiworld.mimiworld.block;

import com.mimiworld.mimiworld.block.entity.ModBlockEntities;
import com.mimiworld.mimiworld.block.entity.SwordQuenchingTableBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

/**
 * 淬剑台（sword_quenching_table）：绑定 SwordQuenchingTableBlockEntity，
 * 右键打开三输入加工 GUI（上排两个 + 中间一个输入、最下方输出），
 * 右侧"开始"按钮按下后才加工一轮。
 */
public class SwordQuenchingTableBlock extends MultiInputProcessorBlock {

    public SwordQuenchingTableBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected BlockEntity createMachineBlockEntity(BlockPos pos, BlockState state) {
        return new SwordQuenchingTableBlockEntity(pos, state);
    }

    @Override
    @SuppressWarnings("rawtypes")
    protected BlockEntityType blockEntityType() {
        return ModBlockEntities.SWORD_QUENCHING_TABLE.get();
    }
}
