package com.mimiworld.mimiworld.block;

import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.DirectionProperty;

/**
 * 炉灶（stove）：煮锅系列的加热源方块。
 *
 * <p>本身没有 GUI、方块实体或燃料逻辑：只要它被 {@code #mimiworld:stoves} 标签收录，
 * 放在煮锅/铁锅正下方就会被 {@link CookingHeaters#hasStoveBelow} 识别为无限热源。
 * 方块正面朝向放置玩家，仅用于外观。</p>
 */
public class StoveBlock extends HorizontalDirectionalBlock {

    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;

    public StoveBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }
}
